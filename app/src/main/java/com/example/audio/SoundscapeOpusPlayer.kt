package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
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

    // ANTI-POP : Handler dédié aux rampes de volume (fade-in / fade-out).
    // MediaPlayer n'a pas de rampe native, on simule donc une rampe en
    // ajustant le volume par petits pas toutes les ~20ms.
    private val fadeHandler = Handler(Looper.getMainLooper())
    private var fadeRunnable: Runnable? = null
    private val fadeSteps = 12
    private val fadeStepDelayMs = 20L

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
                    // ANTI-POP : on démarre à volume 0, le fade-in s'occupe de monter.
                    setVolume(0f, 0f)
                    prepare() // Executed off UI main thread
                }
                withContext(Dispatchers.Main) {
                    mediaPlayer = mp
                    // ANTI-POP : on démarre d'abord, PUIS on seek.
                    // Un seekTo() avant start() peut laisser le décodeur dans un
                    // état transitoire (première frame incomplète) => pop.
                    mp.start()
                    if (savedPosition > 0) {
                        try {
                            mp.seekTo(savedPosition)
                        } catch (e: Exception) {
                            Log.w("SoundscapeOpusPlayer", "seekTo($savedPosition) échoué: ${e.message}")
                        }
                    }
                    _isPlaying.value = true
                    // ANTI-POP : fade-in progressif de 0 vers le volume cible.
                    fadeVolumeTo(vol, fadeIn = true)
                }
            } catch (e: Exception) {
                Log.e("SoundscapeOpusPlayer", "Erreur lors de la lecture de ${track.fileName}: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isPlaying.value = false
                }
            }
        }
    }

    /**
     * ANTI-POP : rampe de volume progressive.
     * MediaPlayer n'a pas de rampe native, on simule donc une rampe linéaire
     * en ajustant le volume par petits pas toutes les [fadeStepDelayMs] ms.
     * Cela évite les clics de démarrage/arrêt brutal.
     */
    private fun fadeVolumeTo(target: Float, fadeIn: Boolean) {
        // Annule toute rampe en cours
        fadeRunnable?.let { fadeHandler.removeCallbacks(it) }
        fadeRunnable = null

        val mp = mediaPlayer ?: return
        val clampedTarget = target.coerceIn(0f, 1f)
        val startVol = if (fadeIn) 0f else {
            try { _volume.value } catch (e: Exception) { 0f }
        }

        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                val player = mediaPlayer ?: return
                step++
                val progress = step.toFloat() / fadeSteps
                val current = startVol + (clampedTarget - startVol) * progress
                try {
                    player.setVolume(current, current)
                } catch (e: Exception) {
                    return
                }
                if (step < fadeSteps) {
                    fadeHandler.postDelayed(this, fadeStepDelayMs)
                } else {
                    fadeRunnable = null
                }
            }
        }
        fadeRunnable = runnable
        fadeHandler.post(runnable)
    }

    /**
     * ANTI-POP : fade-out puis exécution d'une action (pause/stop/release).
     * On laisse le fade-out se terminer avant de couper réellement le son.
     */
    private fun fadeOutThen(action: () -> Unit) {
        fadeRunnable?.let { fadeHandler.removeCallbacks(it) }
        fadeRunnable = null

        val mp = mediaPlayer
        if (mp == null) {
            action()
            return
        }

        val startVol = try { _volume.value } catch (e: Exception) { 0f }
        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                val player = mediaPlayer
                if (player == null) {
                    action()
                    return
                }
                step++
                val progress = step.toFloat() / fadeSteps
                val current = startVol * (1f - progress)
                try {
                    player.setVolume(current, current)
                } catch (e: Exception) {
                    action()
                    return
                }
                if (step < fadeSteps) {
                    fadeHandler.postDelayed(this, fadeStepDelayMs)
                } else {
                    fadeRunnable = null
                    action()
                }
            }
        }
        fadeRunnable = runnable
        fadeHandler.post(runnable)
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
                // ANTI-POP : on démarre à volume 0 puis on fade-in.
                mp.setVolume(0f, 0f)
                mp.start()
                _isPlaying.value = true
                fadeVolumeTo(_volume.value, fadeIn = true)
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
        // ANTI-POP : fade-out avant de mettre en pause pour éviter le clic.
        fadeOutThen {
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
    }

    fun stop() {
        _isPlaying.value = false
        prepareJob?.cancel()
        // ANTI-POP : fade-out avant de stopper/release.
        fadeOutThen {
            stopCurrentPlayer()
        }
    }

    private fun stopCurrentPlayer() {
        // Annule toute rampe en cours avant de libérer le player.
        fadeRunnable?.let { fadeHandler.removeCallbacks(it) }
        fadeRunnable = null
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
        // ANTI-POP : si une rampe est en cours, on la laisse finir (elle cible
        // déjà le bon volume). Sinon on applique directement.
        if (fadeRunnable == null) {
            try {
                mediaPlayer?.setVolume(clamped, clamped)
            } catch (ignored: Exception) {
            }
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
        // ANTI-POP : on annule toute rampe en cours avant de libérer.
        fadeRunnable?.let { fadeHandler.removeCallbacks(it) }
        fadeRunnable = null
        stop()
    }
}
