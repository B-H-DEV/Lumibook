package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * MeditativeSoundEngine manages an infinite playlist of pre-recorded ambient music tracks
 * with real studio-grade cross-fades between consecutive tracks and from the final track back to track 1.
 */
class MeditativeSoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val trackCount = 4
    private var currentTrackIndex = 0

    private var activePlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null
    private var isCrossFading = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.55f)
    val volume = _volume.asStateFlow()

    private var crossFadeJob: Job? = null

    // Cross-fade window in milliseconds (7 seconds)
    private val crossFadeDurationMs = 7000L

    private fun getAssetPath(trackIndex: Int): String {
        val number = (trackIndex % trackCount) + 1
        return "ambiance/track_$number.opus"
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        if (!isCrossFading) {
            try {
                activePlayer?.setVolume(clamped, clamped)
            } catch (e: Exception) {
                Log.w("MeditativeSoundEngine", "Error setting volume: ${e.message}")
            }
        }
    }

    fun getCurrentMusicPosition(): Int {
        return try {
            activePlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getCurrentTrackIndex(): Int {
        return currentTrackIndex
    }

    fun restoreState(trackIndex: Int, positionMs: Int, initialVolume: Float) {
        val vol = initialVolume.coerceIn(0f, 1f)
        _volume.value = vol
        _isPlaying.value = false
        currentTrackIndex = trackIndex % trackCount
        try {
            activePlayer?.release()
            activePlayer = null
        } catch (ignored: Exception) {}

        activePlayer = createMediaPlayerForTrack(currentTrackIndex)?.apply {
            setVolume(vol, vol)
            if (positionMs > 0) {
                seekTo(positionMs)
            }
            setOnCompletionListener {
                onTrackFinished()
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true

        if (activePlayer != null) {
            try {
                val vol = _volume.value
                activePlayer?.setVolume(vol, vol)
                activePlayer?.start()
            } catch (e: Exception) {
                Log.e("MeditativeSoundEngine", "Error resuming player: ${e.message}")
                startTrack(currentTrackIndex)
            }
        } else {
            startTrack(currentTrackIndex)
        }

        startCrossFadeMonitor()
    }

    fun pause() {
        _isPlaying.value = false
        stopCrossFadeMonitor()
        try {
            activePlayer?.pause()
        } catch (e: Exception) {
            Log.w("MeditativeSoundEngine", "Pause error: ${e.message}")
        }
        try {
            nextPlayer?.pause()
        } catch (ignored: Exception) {}
    }

    private fun createMediaPlayerForTrack(trackIndex: Int): MediaPlayer? {
        val assetPath = getAssetPath(trackIndex)
        // 1. Try direct asset file descriptor first
        try {
            val afd: AssetFileDescriptor = context.assets.openFd(assetPath)
            return MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
            }
        } catch (e: Exception) {
            Log.w("MeditativeSoundEngine", "openFd failed for $assetPath (${e.message}), trying cache fallback")
        }

        // 2. Fallback: extract to cache file (works for any audio codec / compressed assets)
        return try {
            val number = (trackIndex % trackCount) + 1
            val cacheDir = File(context.cacheDir, "ambiance").apply { mkdirs() }
            val cacheFile = File(cacheDir, "track_$number.opus")
            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                context.assets.open(assetPath).use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(cacheFile.absolutePath)
                prepare()
            }
        } catch (e: Exception) {
            Log.e("MeditativeSoundEngine", "Error creating player for $assetPath: ${e.message}", e)
            null
        }
    }

    private fun startTrack(trackIndex: Int) {
        try {
            activePlayer?.release()
        } catch (ignored: Exception) {}

        currentTrackIndex = trackIndex % trackCount
        activePlayer = createMediaPlayerForTrack(currentTrackIndex)?.apply {
            val vol = _volume.value
            setVolume(vol, vol)
            start()
            setOnCompletionListener {
                onTrackFinished()
            }
        }
    }

    private fun startCrossFadeMonitor() {
        crossFadeJob?.cancel()
        crossFadeJob = scope.launch {
            while (isActive && _isPlaying.value) {
                delay(200)

                val player = activePlayer ?: continue
                if (!player.isPlaying) continue

                val currentPos: Int
                val duration: Int
                try {
                    currentPos = player.currentPosition
                    duration = player.duration
                } catch (e: Exception) {
                    continue
                }

                if (duration <= 0) continue
                val remainingMs = duration - currentPos

                if (remainingMs <= crossFadeDurationMs && remainingMs > 250) {
                    // Start next track if not already prepared
                    if (nextPlayer == null) {
                        val nextIndex = (currentTrackIndex + 1) % trackCount
                        nextPlayer = createMediaPlayerForTrack(nextIndex)?.apply {
                            setVolume(0f, 0f)
                            start()
                        }
                        isCrossFading = true
                    }

                    // Equal power sinusoidal cross-fade
                    val progress = (1f - (remainingMs.toFloat() / crossFadeDurationMs.toFloat())).coerceIn(0f, 1f)
                    val fadeOut = cos(progress * (PI / 2)).toFloat()
                    val fadeIn = sin(progress * (PI / 2)).toFloat()
                    val masterVol = _volume.value

                    try {
                        player.setVolume(masterVol * fadeOut, masterVol * fadeOut)
                        nextPlayer?.setVolume(masterVol * fadeIn, masterVol * fadeIn)
                    } catch (ignored: Exception) {}
                } else if (remainingMs <= 250 && isCrossFading) {
                    // Transition to next player complete
                    onCrossFadeFinished()
                }
            }
        }
    }

    private fun onCrossFadeFinished() {
        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (ignored: Exception) {}

        activePlayer = nextPlayer
        nextPlayer = null
        isCrossFading = false
        currentTrackIndex = (currentTrackIndex + 1) % trackCount

        val masterVol = _volume.value
        try {
            activePlayer?.setVolume(masterVol, masterVol)
            activePlayer?.setOnCompletionListener {
                onTrackFinished()
            }
        } catch (ignored: Exception) {}
    }

    private fun onTrackFinished() {
        if (isCrossFading && nextPlayer != null) {
            onCrossFadeFinished()
        } else {
            val nextIndex = (currentTrackIndex + 1) % trackCount
            startTrack(nextIndex)
        }
    }

    private fun stopCrossFadeMonitor() {
        crossFadeJob?.cancel()
        crossFadeJob = null
    }

    fun release() {
        pause()
        try {
            activePlayer?.release()
            activePlayer = null
        } catch (ignored: Exception) {}
        try {
            nextPlayer?.release()
            nextPlayer = null
        } catch (ignored: Exception) {}
    }
}
