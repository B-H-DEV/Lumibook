package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.BookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

data class ActiveHighlight(
    val pageNumber: Int = 1,
    val paragraphIndex: Int = 0,
    val charStart: Int = 0,
    val charEnd: Int = 0,
    val currentWord: String = ""
)

data class WordTiming(
    val p: Int,
    val s: Int,
    val e: Int,
    val w: String,
    val t0: Int,
    val t1: Int
)

enum class AiVoice(val id: String, val displayName: String, val description: String) {
    AOEDE("Aoede", "Vivienne (Naturelle & Chaleureuse)", "Voix studio haute fidélité, calme et humaine"),
    CHARON("Charon", "Rémy (Posé & Solennel)", "Voix masculine profonde et posée"),
    KORE("Kore", "Denise (Douce & Claire)", "Voix féminine claire et apaisante"),
    PUCK("Puck", "Henri (Classique)", "Voix narrative posée")
}

class AudiobookPlayer(private val context: Context) : TextToSpeech.OnInitListener {
    private val scope = CoroutineScope(Dispatchers.Main)

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _highlight = MutableStateFlow(ActiveHighlight())
    val highlight = _highlight.asStateFlow()

    // 1.0f is strictly the default natural reading speed
    private val _speechRateMultiplier = MutableStateFlow(1.0f)
    val speechRateMultiplier = _speechRateMultiplier.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll = _autoScroll.asStateFlow()

    private val _selectedVoice = MutableStateFlow(AiVoice.AOEDE)
    val selectedVoice = _selectedVoice.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    private val _isOfflineTrack = MutableStateFlow(true)
    val isOfflineTrack = _isOfflineTrack.asStateFlow()

    private val _isUsingGeminiAudio = MutableStateFlow(true)
    val isUsingGeminiAudio = _isUsingGeminiAudio.asStateFlow()

    private var progressTrackingJob: Job? = null
    private val timingsCache = mutableMapOf<Int, List<WordTiming>>()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.FRENCH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setPitch(0.98f)
            tts?.setSpeechRate(_speechRateMultiplier.value)
            isTtsReady = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    scope.launch {
                        _isPlaying.value = false
                        advanceToNextPage()
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                }
            })
        }
    }

    private fun getEffectiveSpeechRate(): Float {
        return _speechRateMultiplier.value.coerceIn(0.7f, 2.0f)
    }

    fun setSpeechRate(rate: Float) {
        _speechRateMultiplier.value = rate
        val effSpeed = getEffectiveSpeechRate()

        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply { speed = effSpeed }
            } catch (e: Exception) {
                Log.w("AudiobookPlayer", "Cannot update MediaPlayer speed", e)
            }
        }
        tts?.setSpeechRate(effSpeed)
    }

    fun setAiVoice(voice: AiVoice) {
        _selectedVoice.value = voice
        if (_isPlaying.value) {
            playPage(_currentPage.value)
        }
    }

    fun toggleAutoScroll() {
        _autoScroll.value = !_autoScroll.value
    }

    fun togglePlayPause(pageNumber: Int) {
        if (_isPlaying.value && _currentPage.value == pageNumber) {
            pause()
        } else if (!_isPlaying.value && _currentPage.value == pageNumber && mediaPlayer != null) {
            resume()
        } else {
            playPage(pageNumber)
        }
    }

    fun resume() {
        val pageNum = _currentPage.value
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply { speed = getEffectiveSpeechRate() }
                mediaPlayer?.start()
                _isPlaying.value = true
                val timings = loadWordTimings(pageNum)
                startSynchronizedWordTracker(pageNum, timings)
                return
            } catch (e: Exception) {
                Log.w("AudiobookPlayer", "Error resuming MediaPlayer: ${e.message}")
            }
        }
        playPage(pageNum)
    }

    fun getCurrentAudioPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getCurrentAudioPage(): Int {
        return _currentPage.value
    }

    fun restoreState(pageNumber: Int, positionMs: Int) {
        val page = BookRepository.pages.find { it.pageNumber == pageNumber } ?: return
        if (page.paragraphs.isEmpty() || page.paragraphs.all { it.isBlank() }) {
            _currentPage.value = pageNumber
            return
        }

        _currentPage.value = pageNumber
        stopCurrentAudioPlayback()

        try {
            val assetPath = "audio/page_$pageNumber.mp3"
            val afd: AssetFileDescriptor = context.assets.openFd(assetPath)
            val timings = loadWordTimings(pageNumber)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                if (positionMs > 0) {
                    seekTo(positionMs)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    advanceToNextPage()
                }
            }
            _isPlaying.value = false

            // Restore highlight immediately at the exact matching word
            val match = timings.find { positionMs in it.t0..it.t1 }
                ?: timings.findLast { it.t0 <= positionMs }
                ?: timings.firstOrNull()

            if (match != null) {
                _highlight.value = ActiveHighlight(
                    pageNumber = pageNumber,
                    paragraphIndex = match.p,
                    charStart = match.s,
                    charEnd = match.e,
                    currentWord = match.w
                )
            }
        } catch (e: Exception) {
            Log.w("AudiobookPlayer", "Could not restore audio state for page $pageNumber: ${e.message}")
        }
    }

    /**
     * Start audio playback:
     * 100% OFFLINE pre-generated neural MP3 bundled in APK assets/audio/page_X.mp3
     * Synchronized word-for-word using assets/audio/page_X.json
     */
    fun playPage(pageNumber: Int) {
        val page = BookRepository.pages.find { it.pageNumber == pageNumber } ?: return

        // If it is an illustration or blank page with no text, advance to next
        if (page.paragraphs.isEmpty() || page.paragraphs.all { it.isBlank() }) {
            if (pageNumber + 1 <= BookRepository.pages.size) {
                playPage(pageNumber + 1)
            }
            return
        }

        _currentPage.value = pageNumber
        _isOfflineTrack.value = true
        _isUsingGeminiAudio.value = true
        _isAiGenerating.value = false

        stopCurrentAudioPlayback()

        // 1. Play bundled high-fidelity asset
        if (playBundledAsset(pageNumber)) {
            return
        }

        // 2. Fallback to local Android TTS if asset missing
        playWithNativeTts(pageNumber, page.paragraphs.joinToString("\n\n"))
    }

    private fun loadWordTimings(pageNumber: Int): List<WordTiming> {
        timingsCache[pageNumber]?.let { return it }

        return try {
            val jsonStr = context.assets.open("audio/page_$pageNumber.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            val list = ArrayList<WordTiming>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WordTiming(
                        p = obj.getInt("p"),
                        s = obj.getInt("s"),
                        e = obj.getInt("e"),
                        w = obj.getString("w"),
                        t0 = obj.getInt("t0"),
                        t1 = obj.getInt("t1")
                    )
                )
            }
            timingsCache[pageNumber] = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun playBundledAsset(pageNumber: Int): Boolean {
        return try {
            val assetPath = "audio/page_$pageNumber.mp3"
            val afd: AssetFileDescriptor = context.assets.openFd(assetPath)
            val timings = loadWordTimings(pageNumber)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                playbackParams = PlaybackParams().apply { speed = getEffectiveSpeechRate() }
                setOnCompletionListener {
                    _isPlaying.value = false
                    advanceToNextPage()
                }
                start()
            }
            _isPlaying.value = true

            // Lock highlight immediately onto the very first word of the page so it never drifts before speech begins
            if (timings.isNotEmpty()) {
                val first = timings.first()
                _highlight.value = ActiveHighlight(
                    pageNumber = pageNumber,
                    paragraphIndex = first.p,
                    charStart = first.s,
                    charEnd = first.e,
                    currentWord = first.w
                )
            }

            startSynchronizedWordTracker(pageNumber, timings)
            true
        } catch (e: Exception) {
            Log.w("AudiobookPlayer", "Asset audio/page_$pageNumber.mp3 unavailable: ${e.message}")
            false
        }
    }

    private fun startSynchronizedWordTracker(pageNumber: Int, timings: List<WordTiming>) {
        progressTrackingJob?.cancel()
        if (timings.isEmpty()) return

        progressTrackingJob = scope.launch {
            while (isActive && _isPlaying.value && _currentPage.value == pageNumber) {
                val currentPos = mediaPlayer?.currentPosition ?: 0
                // Match word that covers current audio position, or fallback to first word during initial silence
                val match = timings.find { currentPos in it.t0..it.t1 }
                    ?: timings.findLast { it.t0 <= currentPos }
                    ?: timings.firstOrNull()

                if (match != null) {
                    _highlight.value = ActiveHighlight(
                        pageNumber = pageNumber,
                        paragraphIndex = match.p,
                        charStart = match.s,
                        charEnd = match.e,
                        currentWord = match.w
                    )
                }
                delay(35) // High responsiveness (~30 FPS) without battery drain
            }
        }
    }

    private fun playWithNativeTts(pageNumber: Int, text: String) {
        if (!isTtsReady || tts == null) {
            _isPlaying.value = false
            return
        }

        val utteranceId = "lumibook_page_$pageNumber"
        val params = Bundle()
        tts?.setSpeechRate(getEffectiveSpeechRate())
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        _isPlaying.value = true
    }

    private fun advanceToNextPage() {
        val next = _currentPage.value + 1
        if (next <= BookRepository.pages.size) {
            playPage(next)
        }
    }

    private fun stopCurrentAudioPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (ignored: Exception) {}

        try {
            tts?.stop()
        } catch (ignored: Exception) {}

        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    fun pause() {
        _isPlaying.value = false
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (ignored: Exception) {}

        try {
            tts?.stop()
        } catch (ignored: Exception) {}

        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    fun stop() {
        pause()
        stopCurrentAudioPlayback()
        _highlight.value = ActiveHighlight()
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
