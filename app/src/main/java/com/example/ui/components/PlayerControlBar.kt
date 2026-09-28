package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AiVoice
import com.example.audio.soundscapeTracks
import com.example.data.AmbianceMode
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookNavyDark
import kotlin.math.abs

@Composable
fun PlayerControlBar(
    isAudiobookPlaying: Boolean,
    onAudiobookToggle: () -> Unit,
    onAudiobookPrev: () -> Unit,
    onAudiobookNext: () -> Unit,
    currentPage: Int,
    totalPages: Int,
    speechRateMultiplier: Float,
    onSpeechRateChange: (Float) -> Unit,
    autoScrollEnabled: Boolean,
    onToggleAutoScroll: () -> Unit,
    selectedVoice: AiVoice,
    onVoiceSelect: (AiVoice) -> Unit,
    isAiGenerating: Boolean,
    isUsingGeminiAudio: Boolean,
    isOfflineTrack: Boolean,
    isMusicPlaying: Boolean,
    onMusicToggle: () -> Unit,
    onRelaxBoxClick: () -> Unit = {},
    onSoundscapeClick: () -> Unit = {},
    musicVolume: Float,
    onMusicVolumeChange: (Float) -> Unit,
    currentPreset: String = "sleep-deep",
    onSelectPreset: (String) -> Unit = {},
    onOpenRelaxBoxFull: () -> Unit = {},
    ambianceMode: AmbianceMode = AmbianceMode.RELAX_BOX,
    onAmbianceModeSelect: (AmbianceMode) -> Unit = {},
    soundscapeTrackIndex: Int = 0,
    onSoundscapeTrackSelect: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val readerColors = LocalReaderColors.current
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = readerColors.surface,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Main control row: Audiobook controls & Ambient Music
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Playback controls + Compact Page Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Previous Page Button
                    IconButton(
                        onClick = onAudiobookPrev,
                        enabled = currentPage > 1,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("audio_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Page précédente",
                            tint = if (currentPage > 1) readerColors.textPrimary else readerColors.textSecondary.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Main Audiobook Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(readerColors.accent)
                            .clickable { onAudiobookToggle() }
                            .testTag("audio_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAiGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = if (readerColors.isDark) LumibookNavyDark else Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (isAudiobookPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isAudiobookPlaying) "Pause livre audio" else "Lire livre audio",
                                tint = if (readerColors.isDark) LumibookNavyDark else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Next Page Button
                    IconButton(
                        onClick = onAudiobookNext,
                        enabled = currentPage < totalPages,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("audio_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Page suivante",
                            tint = if (currentPage < totalPages) readerColors.textPrimary else readerColors.textSecondary.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Audio Status Text
                    Column(
                        modifier = Modifier.widthIn(max = 100.dp)
                    ) {
                        Text(
                            text = if (isAudiobookPlaying) "Narration" else "Livre audio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = readerColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "p. $currentPage / $totalPages",
                            fontSize = 10.5.sp,
                            color = readerColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Ambiance Mode Controls (Relax-Box & Soundscape side-by-side)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // 1. Relax-Box Button
                    // L'état actif reflète le mode sélectionné (pas l'état de lecture)
                    // pour rester synchronisé avec le sélecteur du panneau dépliable.
                    val isRelaxActive = ambianceMode == AmbianceMode.RELAX_BOX
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isRelaxActive) readerColors.accent.copy(alpha = 0.22f) else readerColors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isRelaxActive) readerColors.accent else readerColors.accent.copy(alpha = 0.35f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onRelaxBoxClick() }
                            .padding(horizontal = 7.dp, vertical = 6.dp)
                            .testTag("relax_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Relax-Box",
                                tint = if (isRelaxActive) readerColors.accent else readerColors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Relax-Box",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRelaxActive) readerColors.accent else readerColors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 2. Soundscape Button
                    // L'état actif reflète le mode sélectionné (pas l'état de lecture)
                    // pour rester synchronisé avec le sélecteur du panneau dépliable.
                    val isSoundscapeActive = ambianceMode == AmbianceMode.SOUNDSCAPE
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSoundscapeActive) readerColors.accent.copy(alpha = 0.22f) else readerColors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSoundscapeActive) readerColors.accent else readerColors.accent.copy(alpha = 0.35f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSoundscapeClick() }
                            .padding(horizontal = 7.dp, vertical = 6.dp)
                            .testTag("soundscape_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "Soundscape Opus",
                                tint = if (isSoundscapeActive) readerColors.accent else readerColors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Soundscape",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSoundscapeActive) readerColors.accent else readerColors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // 3. Expand / Collapse options drawer button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("player_expand_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = "Options audio & modes",
                            tint = readerColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded Controls Drawer: Audio options & Ambiance Mode switcher
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(readerColors.surfaceVariant)
                        .padding(12.dp)
                ) {
                    // Offline info banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = readerColors.accent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Livre Audio 100% Hors-Ligne (Sans compte ni API)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = readerColors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto-scroll & Speed settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (autoScrollEnabled) readerColors.accent else readerColors.surface)
                                .clickable { onToggleAutoScroll() }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("auto_scroll_toggle")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerticalAlignBottom,
                                    contentDescription = null,
                                    tint = if (autoScrollEnabled) (if (readerColors.isDark) LumibookNavyDark else Color.White) else readerColors.textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (autoScrollEnabled) "Auto-scroll : Oui" else "Auto-scroll : Non",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (autoScrollEnabled) (if (readerColors.isDark) LumibookNavyDark else Color.White) else readerColors.textPrimary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                0.85f to "0.85x",
                                1.0f to "1.0x (Naturel)",
                                1.15f to "1.15x",
                                1.3f to "1.3x"
                            ).forEach { (rate, label) ->
                                val selected = abs(speechRateMultiplier - rate) < 0.05f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) readerColors.accent else readerColors.surface)
                                        .clickable { onSpeechRateChange(rate) }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) (if (readerColors.isDark) LumibookNavyDark else Color.White) else readerColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- SECTION AMBIANCE MUSICALE ---
                    Text(
                        text = "Ambiance Musicale & Sonore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = readerColors.accent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Ambiance Switcher (3 Buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AmbianceMode.entries.forEach { mode ->
                            val isSelected = ambianceMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) readerColors.accent else readerColors.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) readerColors.accent else readerColors.accent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onAmbianceModeSelect(mode) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) (if (readerColors.isDark) LumibookNavyDark else Color.White) else readerColors.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (ambianceMode) {
                        AmbianceMode.NONE -> {
                            Text(
                                text = "Ambiance sonore désactivée pour une lecture en silence.",
                                fontSize = 11.sp,
                                color = readerColors.textSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        AmbianceMode.RELAX_BOX -> {
                            // Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                    contentDescription = null,
                                    tint = readerColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Slider(
                                    value = musicVolume,
                                    onValueChange = onMusicVolumeChange,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .testTag("music_volume_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = readerColors.accent,
                                        activeTrackColor = readerColors.accent,
                                        inactiveTrackColor = readerColors.surface
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = readerColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Presets Relax-Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "global" to "Global",
                                    "sleep" to "Sleep",
                                    "focus" to "Focus",
                                    "zen" to "Zen"
                                ).forEach { (modeKey, label) ->
                                    val isSelected = currentPreset.equals(modeKey, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) readerColors.accent.copy(alpha = 0.25f) else readerColors.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) readerColors.accent else readerColors.textSecondary.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectPreset(modeKey) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) readerColors.accent else readerColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(readerColors.accent.copy(alpha = 0.15f))
                                    .border(1.dp, readerColors.accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable { onOpenRelaxBoxFull() }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                                    .testTag("open_relaxbox_full_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = readerColors.accent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Afficher la Relax-Box en plein écran",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = readerColors.accent
                                )
                            }
                        }

                        AmbianceMode.SOUNDSCAPE -> {
                            // Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                    contentDescription = null,
                                    tint = readerColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Slider(
                                    value = musicVolume,
                                    onValueChange = onMusicVolumeChange,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .testTag("music_volume_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = readerColors.accent,
                                        activeTrackColor = readerColors.accent,
                                        inactiveTrackColor = readerColors.surface
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = readerColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Track Selector Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                soundscapeTracks.forEach { track ->
                                    val isSelected = soundscapeTrackIndex == track.index
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) readerColors.accent.copy(alpha = 0.25f) else readerColors.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) readerColors.accent else readerColors.textSecondary.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSoundscapeTrackSelect(track.index) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = track.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) readerColors.accent else readerColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val currentTrack = soundscapeTracks.getOrNull(soundscapeTrackIndex)
                            if (currentTrack != null) {
                                Text(
                                    text = "${currentTrack.title} : ${currentTrack.description} (lecture en boucle)",
                                    fontSize = 11.sp,
                                    color = readerColors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
