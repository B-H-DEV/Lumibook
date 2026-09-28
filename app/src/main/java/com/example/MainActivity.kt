package com.example

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.audio.AudiobookPlayer
import com.example.audio.RelaxBoxEngine
import com.example.audio.SoundscapeOpusPlayer
import com.example.data.AmbianceMode
import com.example.data.BookRepository
import com.example.data.ReaderPreferences
import com.example.ui.components.PlayerControlBar
import com.example.ui.components.ReaderPageView
import com.example.ui.components.ReaderTopBar
import com.example.ui.components.TableOfContentsSheet
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookGold
import com.example.ui.theme.LumibookTheme
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var relaxEngine: RelaxBoxEngine
    private lateinit var soundscapePlayer: SoundscapeOpusPlayer
    private lateinit var audiobookPlayer: AudiobookPlayer
    private lateinit var prefs: ReaderPreferences
    private var activeCurrentPage: Int = 1
    private var currentAmbianceMode: AmbianceMode = AmbianceMode.RELAX_BOX

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = ReaderPreferences(this)
        relaxEngine = RelaxBoxEngine(this)
        soundscapePlayer = SoundscapeOpusPlayer(this)
        audiobookPlayer = AudiobookPlayer(this)

        val savedPage = prefs.viewedPage.coerceIn(1, BookRepository.pages.size)
        val savedAudioPage = prefs.audiobookPage.coerceIn(1, BookRepository.pages.size)
        val savedAudioPos = prefs.audiobookPositionMs
        val savedVolume = prefs.musicVolume
        val savedPreset = prefs.relaxPreset
        val savedMode = prefs.ambianceMode
        val savedTrackIdx = prefs.musicTrackIndex
        val savedTrackPos = prefs.getSoundscapePosition(savedTrackIdx.coerceIn(0, 2))
        val savedMusicPlaying = prefs.musicWasPlaying

        activeCurrentPage = savedPage
        // Forcer NONE au démarrage pour qu'aucun bouton ne soit allumé
        currentAmbianceMode = AmbianceMode.NONE

        // Restore engine states
        relaxEngine.restoreState(savedVolume, false, savedPreset) // Toujours initialiser sans relancer
        soundscapePlayer.restoreState(savedTrackIdx, savedTrackPos, false, savedVolume) // Restaurer la position mais sans lecture
        audiobookPlayer.restoreState(savedAudioPage, savedAudioPos)

        setContent {
            var readerTheme by remember { mutableStateOf(ReaderThemeMode.DARK) }
            var currentPageNumber by remember { mutableIntStateOf(savedPage) }
            var fontSizeMultiplier by remember { mutableFloatStateOf(1.0f) }
            var showTocSheet by remember { mutableStateOf(false) }

            var ambianceModeState by remember { mutableStateOf(AmbianceMode.NONE) } // Commencer avec aucun mode actif
            var soundscapeTrackIdxState by remember { mutableIntStateOf(savedTrackIdx) }

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

            val isRelaxPlaying by relaxEngine.isPlaying.collectAsState()
            val isOpusPlaying by soundscapePlayer.isPlaying.collectAsState()
            val musicVolume by relaxEngine.volume.collectAsState()
            val currentPreset by relaxEngine.currentPreset.collectAsState()
            val isRelaxBoxFullScreen by relaxEngine.isFullScreenOpen.collectAsState()
            val isImmersiveMode by relaxEngine.isImmersiveMode.collectAsState()

            val isMusicPlaying = when (ambianceModeState) {
                AmbianceMode.NONE -> false
                AmbianceMode.RELAX_BOX -> isRelaxPlaying
                AmbianceMode.SOUNDSCAPE -> isOpusPlaying
            }

            // Handle back button when Relax-Box is in full screen mode
            BackHandler(enabled = isRelaxBoxFullScreen) {
                if (isImmersiveMode) {
                    relaxEngine.toggleImmersiveMode()
                } else {
                    relaxEngine.closeFullScreen()
                }
            }

            // Synchronize Android system status/nav bars with Relax-Box immersive full-screen mode
            LaunchedEffect(isRelaxBoxFullScreen, isImmersiveMode) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (isRelaxBoxFullScreen && isImmersiveMode) {
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }

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

                Box(modifier = Modifier.fillMaxSize()) {
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
                                onMusicToggle = {
                                    when (ambianceModeState) {
                                        AmbianceMode.NONE -> {
                                            // Ne rien faire - l'utilisateur doit choisir Relax-Box ou Soundscape
                                        }

                                        AmbianceMode.RELAX_BOX -> relaxEngine.togglePlayPause()
                                        AmbianceMode.SOUNDSCAPE -> soundscapePlayer.togglePlayPause()
                                    }
                                },
                                onRelaxBoxClick = {
                                    // Comportement identique au sélecteur du bas :
                                    // - si Relax-Box est déjà actif -> stop total (NONE)
                                    // - sinon -> activer Relax-Box en mode aléatoire global
                                    if (ambianceModeState == AmbianceMode.RELAX_BOX) {
                                        ambianceModeState = AmbianceMode.NONE
                                        currentAmbianceMode = AmbianceMode.NONE
                                        prefs.ambianceMode = AmbianceMode.NONE
                                        relaxEngine.pause()
                                        soundscapePlayer.pause()
                                    } else {
                                        soundscapePlayer.pause()
                                        ambianceModeState = AmbianceMode.RELAX_BOX
                                        currentAmbianceMode = AmbianceMode.RELAX_BOX
                                        prefs.ambianceMode = AmbianceMode.RELAX_BOX
                                        // Lancer le mode Global aléatoire
                                        relaxEngine.setAutoMode("global")
                                    }
                                },
                                onSoundscapeClick = {
                                    // Comportement identique au sélecteur du bas :
                                    // - si Soundscape est déjà actif -> stop total (NONE)
                                    // - sinon -> activer Soundscape sur la piste sélectionnée
                                    if (ambianceModeState == AmbianceMode.SOUNDSCAPE) {
                                        ambianceModeState = AmbianceMode.NONE
                                        currentAmbianceMode = AmbianceMode.NONE
                                        prefs.ambianceMode = AmbianceMode.NONE
                                        relaxEngine.pause()
                                        soundscapePlayer.pause()
                                    } else {
                                        relaxEngine.pause()
                                        ambianceModeState = AmbianceMode.SOUNDSCAPE
                                        currentAmbianceMode = AmbianceMode.SOUNDSCAPE
                                        prefs.ambianceMode = AmbianceMode.SOUNDSCAPE
                                        // Lancer la piste sélectionnée
                                        soundscapePlayer.playTrack(soundscapeTrackIdxState)
                                    }
                                },
                                musicVolume = musicVolume,
                                onMusicVolumeChange = { vol ->
                                    relaxEngine.setVolume(vol)
                                    soundscapePlayer.setVolume(vol)
                                },
                                currentPreset = currentPreset,
                                onSelectPreset = { preset ->
                                    if (ambianceModeState != AmbianceMode.RELAX_BOX) {
                                        ambianceModeState = AmbianceMode.RELAX_BOX
                                        currentAmbianceMode = AmbianceMode.RELAX_BOX
                                        prefs.ambianceMode = AmbianceMode.RELAX_BOX
                                        soundscapePlayer.pause()
                                    }
                                    relaxEngine.loadPreset(preset)
                                },
                                onOpenRelaxBoxFull = {
                                    if (ambianceModeState != AmbianceMode.RELAX_BOX) {
                                        ambianceModeState = AmbianceMode.RELAX_BOX
                                        currentAmbianceMode = AmbianceMode.RELAX_BOX
                                        prefs.ambianceMode = AmbianceMode.RELAX_BOX
                                        soundscapePlayer.pause()
                                        relaxEngine.setAutoMode("global")
                                    }
                                    relaxEngine.openFullScreen()
                                },
                                ambianceMode = ambianceModeState,
                                onAmbianceModeSelect = { newMode ->
                                    // Re-cliquer sur le mode déjà actif l'éteint (stop total)
                                    val effectiveMode =
                                        if (newMode == ambianceModeState && newMode != AmbianceMode.NONE) {
                                            AmbianceMode.NONE
                                        } else {
                                            newMode
                                        }

                                    ambianceModeState = effectiveMode
                                    currentAmbianceMode = effectiveMode
                                    prefs.ambianceMode = effectiveMode

                                    when (effectiveMode) {
                                        AmbianceMode.NONE -> {
                                            relaxEngine.pause()
                                            soundscapePlayer.pause()
                                        }

                                        AmbianceMode.RELAX_BOX -> {
                                            soundscapePlayer.pause()
                                            // Toujours démarrer en mode aléatoire global
                                            relaxEngine.setAutoMode("global")
                                        }

                                        AmbianceMode.SOUNDSCAPE -> {
                                            relaxEngine.pause()
                                            soundscapePlayer.playTrack(soundscapeTrackIdxState)
                                        }
                                    }
                                },
                                soundscapeTrackIndex = soundscapeTrackIdxState,
                                onSoundscapeTrackSelect = { trackIdx ->
                                    soundscapeTrackIdxState = trackIdx
                                    prefs.musicTrackIndex = trackIdx
                                    if (ambianceModeState != AmbianceMode.SOUNDSCAPE) {
                                        ambianceModeState = AmbianceMode.SOUNDSCAPE
                                        currentAmbianceMode = AmbianceMode.SOUNDSCAPE
                                        prefs.ambianceMode = AmbianceMode.SOUNDSCAPE
                                        relaxEngine.pause()
                                    }
                                    soundscapePlayer.playTrack(trackIdx)
                                }
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

                    // Relax-Box WebView Container
                    // Kept continuously attached to preserve audio playback state in background!
                    Box(
                        modifier = if (isRelaxBoxFullScreen) {
                            Modifier
                                .fillMaxSize()
                                .zIndex(100f)
                                .background(Color(0xFF0F172A))
                        } else {
                            Modifier
                                .size(1.dp)
                                .alpha(0.001f)
                        }
                    ) {
                        val relaxWv by relaxEngine.webViewFlow.collectAsState()
                        relaxWv?.let { wv ->
                            AndroidView(
                                factory = {
                                    (wv.parent as? ViewGroup)?.removeView(wv)
                                    wv
                                },
                                update = { currentView ->
                                    if (currentView != wv) {
                                        (wv.parent as? ViewGroup)?.removeView(wv)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Overlay button when Relax-Box is in full screen mode (hidden in immersive mode)
                        if (isRelaxBoxFullScreen && !isImmersiveMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xDD0F172A),
                                    border = BorderStroke(1.dp, LumibookGold.copy(alpha = 0.6f)),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { relaxEngine.closeFullScreen() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Retour au livre",
                                            tint = LumibookGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Retour au livre",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
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
        if (::prefs.isInitialized && ::audiobookPlayer.isInitialized && ::relaxEngine.isInitialized && ::soundscapePlayer.isInitialized) {
            val isPlayingMusic = when (currentAmbianceMode) {
                AmbianceMode.NONE -> false
                AmbianceMode.RELAX_BOX -> relaxEngine.isPlaying.value
                AmbianceMode.SOUNDSCAPE -> soundscapePlayer.isPlaying.value
            }
            // Sauvegarder la position de la piste Soundscape actuelle
            val currentTrackIdx = soundscapePlayer.currentTrackIndex.value
            val currentTrackPos = soundscapePlayer.getCurrentPosition()
            prefs.setSoundscapePosition(currentTrackIdx, currentTrackPos)
            
            prefs.saveFullState(
                currentReadingPage = activeCurrentPage,
                audioPage = audiobookPlayer.getCurrentAudioPage(),
                audioPosMs = audiobookPlayer.getCurrentAudioPosition(),
                audioPlaying = audiobookPlayer.isPlaying.value,
                trackIdx = currentTrackIdx,
                trackPosMs = currentTrackPos,
                musicPlaying = isPlayingMusic,
                volume = relaxEngine.volume.value,
                mode = currentAmbianceMode
            )
            prefs.relaxPreset = relaxEngine.currentPreset.value
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveCurrentState()
        relaxEngine.release()
        soundscapePlayer.release()
        audiobookPlayer.release()
    }
}
