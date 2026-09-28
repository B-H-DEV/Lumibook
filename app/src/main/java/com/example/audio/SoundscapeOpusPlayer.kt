package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SoundscapeTrack(
    val index: Int,
    val title: String,
    val fileName: String,
    val description: String
)

val soundscapeTracks = listOf(
    SoundscapeTrack(
        index = 0,
        title = "Piste 1",
        fileName = "track_1.opus",
        description = "Voyage sonore d'1h aux vibrations cosmiques douces"
    ),
    SoundscapeTrack(
        index = 1,
        title = "Piste 2",
        fileName = "track_2.opus",
        description = "Ondes méditatives et paysages acoustiques apaisants"
    ),
    SoundscapeTrack(
        index = 2,
        title = "Piste 3",
        fileName = "track_3.opus",
        description = "Nappes contemplatives et résonances harmoniques"
    )
)

class SoundscapeOpusPlayer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var prepareJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex = _currentTrackIndex.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume = _volume.asStateFlow()

    // Stocker la position de chaque piste individuellement
    private val trackPositions = mutableMapOf(0 to 0, 1 to 0, 2 to 0)

    private fun openAssetFd(fileName: String): AssetFileDescriptor? {
        val pathsToTry = listOf(
            "soundscape/$fileName",
            "audio/soundscape/$fileName",
            fileName
        )
        for (path in pathsToTry) {
            try {
                return context.assets.openFd(path)
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    fun playTrack(index: Int) {
        val targetIndex = index.coerceIn(0, soundscapeTracks.size - 1)
        _currentTrackIndex.value = targetIndex
        val track = soundscapeTracks[targetIndex]

        prepareJob?.cancel()
        stopCurrentPlayer()

        val afd = openAssetFd(track.fileName)
        if (afd == null) {
            Log.w("SoundscapeOpusPlayer", "Fichier ${track.fileName} introuvable dans assets/soundscape/")
            _isPlaying.value = false
            return
        }

        // Asynchronous preparation on Dispatchers.IO background thread to keep UI 100% responsive without any lag
        prepareJob = scope.launch(Dispatchers.IO) {
            try {
                val vol = _volume.value
                val savedPosition = trackPositions[targetIndex] ?: 0
                val mp = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true // Boucle infinie continue
                    setVolume(vol, vol)
                    prepare() // Executed off UI main thread
                    // Restaurer la position sauvegardée
                    if (savedPosition > 0) {
                        seekTo(savedPosition)
                    }
                }
                withContext(Dispatchers.Main) {
                    mediaPlayer = mp
                    mp.start()
                    _isPlaying.value = true
                }
            } catch (e: Exception) {
                Log.e("SoundscapeOpusPlayer", "Erreur lors de la lecture de ${track.fileName}: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isPlaying.value = false
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            if (mediaPlayer != null) {
                resume()
            } else {
                playTrack(_currentTrackIndex.value)
            }
        }
    }

    fun play() {
        if (mediaPlayer != null) {
            resume()
        } else {
            playTrack(_currentTrackIndex.value)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let { mp ->
                val vol = _volume.value
                mp.setVolume(vol, vol)
                mp.start()
                _isPlaying.value = true
                return
            }
        } catch (e: Exception) {
            Log.w("SoundscapeOpusPlayer", "Erreur lors de la reprise de lecture Opus: ${e.message}")
        }
        playTrack(_currentTrackIndex.value)
    }

    fun pause() {
        _isPlaying.value = false
        prepareJob?.cancel()
        try {
            if (mediaPlayer?.isPlaying == true) {
                // Sauvegarder la position actuelle avant de mettre en pause
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                trackPositions[_currentTrackIndex.value] = currentPosition
                mediaPlayer?.pause()
            }
        } catch (ignored: Exception) {
        }
    }

    fun stop() {
        _isPlaying.value = false
        prepareJob?.cancel()
        stopCurrentPlayer()
    }

    private fun stopCurrentPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (ignored: Exception) {
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (ignored: Exception) {
        }
    }

    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun restoreState(trackIndex: Int, positionMs: Int, wasPlaying: Boolean, savedVolume: Float) {
        _volume.value = savedVolume.coerceIn(0f, 1f)
        // Restaurer l'index de la piste sauvegardée
        _currentTrackIndex.value = trackIndex.coerceIn(0, soundscapeTracks.size - 1)
        // Restaurer la position sauvegardée
        trackPositions[_currentTrackIndex.value] = positionMs

        // Ne jamais relancer automatiquement - toujours en attente
        _isPlaying.value = false
        mediaPlayer = null
    }

    fun release() {
        stop()
    }
}
