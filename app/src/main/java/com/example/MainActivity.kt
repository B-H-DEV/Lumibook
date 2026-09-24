package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.AudiobookPlayer
import com.example.audio.MeditativeSoundEngine
import com.example.data.BookRepository
import com.example.data.ReaderPreferences
import com.example.ui.components.PlayerControlBar
import com.example.ui.components.ReaderPageView
import com.example.ui.components.ReaderTopBar
import com.example.ui.components.TableOfContentsSheet
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookTheme
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var soundEngine: MeditativeSoundEngine
    private lateinit var audiobookPlayer: AudiobookPlayer
    private lateinit var prefs: ReaderPreferences
    private var activeCurrentPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = ReaderPreferences(this)
        soundEngine = MeditativeSoundEngine(this)
        audiobookPlayer = AudiobookPlayer(this)

        val savedPage = prefs.viewedPage.coerceIn(1, BookRepository.pages.size)
        val savedAudioPage = prefs.audiobookPage.coerceIn(1, BookRepository.pages.size)
        val savedAudioPos = prefs.audiobookPositionMs
        val savedTrack = prefs.musicTrackIndex
        val savedMusicPos = prefs.musicPositionMs
        val savedVolume = prefs.musicVolume

        activeCurrentPage = savedPage

        // Restore engine state
        soundEngine.restoreState(savedTrack, savedMusicPos, savedVolume)
        audiobookPlayer.restoreState(savedAudioPage, savedAudioPos)

        setContent {
            var readerTheme by remember { mutableStateOf(ReaderThemeMode.DARK) }
            var currentPageNumber by remember { mutableIntStateOf(savedPage) }
            var fontSizeMultiplier by remember { mutableFloatStateOf(1.0f) }
            var showTocSheet by remember { mutableStateOf(false) }

            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            val isAudiobookPlaying by audiobookPlayer.isPlaying.collectAsState()
            val spokenPage by audiobookPlayer.currentPage.collectAsState()
            val activeHighlight by audiobookPlayer.highlight.collectAsState()
            val speechRateMultiplier by audiobookPlayer.speechRateMultiplier.collectAsState()
            val autoScrollEnabled by audiobookPlayer.autoScroll.collectAsState()
            val selectedVoice by audiobookPlayer.selectedVoice.collectAsState()
            val isAiGenerating by audiobookPlayer.isAiGenerating.collectAsState()
            val isUsingGeminiAudio by audiobookPlayer.isUsingGeminiAudio.collectAsState()
            val isOfflineTrack by audiobookPlayer.isOfflineTrack.collectAsState()

            val isMusicPlaying by soundEngine.isPlaying.collectAsState()
            val musicVolume by soundEngine.volume.collectAsState()

            // Synchronize displayed page with audiobook if audio is actively playing
            LaunchedEffect(spokenPage) {
                if (isAudiobookPlaying) {
                    currentPageNumber = spokenPage
                }
            }

            val displayedPageNumber = if (isAudiobookPlaying) spokenPage else currentPageNumber

            LaunchedEffect(displayedPageNumber) {
                activeCurrentPage = displayedPageNumber
                prefs.viewedPage = displayedPageNumber
            }

            LaunchedEffect(isAudiobookPlaying) {
                if (!isAudiobookPlaying) {
                    saveCurrentState()
                }
            }

            LaunchedEffect(isMusicPlaying) {
                if (!isMusicPlaying) {
                    saveCurrentState()
                }
            }

            val currentPage = remember(displayedPageNumber) {
                BookRepository.pages.find { it.pageNumber == displayedPageNumber }
                    ?: BookRepository.pages.first()
            }

            val totalPages = BookRepository.pages.size

            val navigateToPage: (Int) -> Unit = { targetPage ->
                val clamped = targetPage.coerceIn(1, totalPages)
                currentPageNumber = clamped
                activeCurrentPage = clamped
                prefs.viewedPage = clamped
                if (isAudiobookPlaying) {
                    audiobookPlayer.playPage(clamped)
                }
                coroutineScope.launch { listState.scrollToItem(0) }
            }

            LumibookTheme(readerThemeMode = readerTheme) {
                val readerColors = LocalReaderColors.current

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = readerColors.background,
                    topBar = {
                        ReaderTopBar(
                            currentPage = displayedPageNumber,
                            totalPages = totalPages,
                            onPrevPage = { navigateToPage(displayedPageNumber - 1) },
                            onNextPage = { navigateToPage(displayedPageNumber + 1) },
                            currentTheme = readerTheme,
                            onThemeSelected = { readerTheme = it },
                            fontSizeMultiplier = fontSizeMultiplier,
                            onFontSizeChange = { fontSizeMultiplier = it },
                            onOpenTableOfContents = { showTocSheet = true }
                        )
                    },
                    bottomBar = {
                        PlayerControlBar(
                            isAudiobookPlaying = isAudiobookPlaying,
                            onAudiobookToggle = {
                                audiobookPlayer.togglePlayPause(displayedPageNumber)
                            },
                            onAudiobookPrev = { navigateToPage(displayedPageNumber - 1) },
                            onAudiobookNext = { navigateToPage(displayedPageNumber + 1) },
                            currentPage = displayedPageNumber,
                            totalPages = totalPages,
                            speechRateMultiplier = speechRateMultiplier,
                            onSpeechRateChange = { audiobookPlayer.setSpeechRate(it) },
                            autoScrollEnabled = autoScrollEnabled,
                            onToggleAutoScroll = { audiobookPlayer.toggleAutoScroll() },
                            selectedVoice = selectedVoice,
                            onVoiceSelect = { audiobookPlayer.setAiVoice(it) },
                            isAiGenerating = isAiGenerating,
                            isUsingGeminiAudio = isUsingGeminiAudio,
                            isOfflineTrack = isOfflineTrack,
                            isMusicPlaying = isMusicPlaying,
                            onMusicToggle = { soundEngine.togglePlayPause() },
                            musicVolume = musicVolume,
                            onMusicVolumeChange = { soundEngine.setVolume(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(readerColors.background)
                    ) {
                        ReaderPageView(
                            page = currentPage,
                            totalPages = totalPages,
                            activeHighlight = activeHighlight,
                            isAudioPlaying = isAudiobookPlaying,
                            isAiGenerating = isAiGenerating,
                            fontSizeMultiplier = fontSizeMultiplier,
                            autoScrollEnabled = autoScrollEnabled,
                            onStartAudioAtPage = { pageNum ->
                                currentPageNumber = pageNum
                                audiobookPlayer.playPage(pageNum)
                            },
                            onPrevPage = { navigateToPage(displayedPageNumber - 1) },
                            onNextPage = { navigateToPage(displayedPageNumber + 1) },
                            listState = listState
                        )

                        if (showTocSheet) {
                            TableOfContentsSheet(
                                currentPage = displayedPageNumber,
                                onPageSelected = { selectedPage ->
                                    navigateToPage(selectedPage)
                                    showTocSheet = false
                                },
                                onDismiss = { showTocSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveCurrentState()
    }

    override fun onStop() {
        super.onStop()
        saveCurrentState()
    }

    private fun saveCurrentState() {
        if (::prefs.isInitialized && ::audiobookPlayer.isInitialized && ::soundEngine.isInitialized) {
            prefs.saveFullState(
                currentReadingPage = activeCurrentPage,
                audioPage = audiobookPlayer.getCurrentAudioPage(),
                audioPosMs = audiobookPlayer.getCurrentAudioPosition(),
                audioPlaying = audiobookPlayer.isPlaying.value,
                trackIdx = soundEngine.getCurrentTrackIndex(),
                trackPosMs = soundEngine.getCurrentMusicPosition(),
                musicPlaying = soundEngine.isPlaying.value,
                volume = soundEngine.volume.value
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveCurrentState()
        soundEngine.release()
        audiobookPlayer.release()
    }
}
