package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ActiveHighlight
import com.example.data.BookPage
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookGold
import com.example.ui.theme.LumibookNavyDark

@Composable
fun ReaderPageView(
    page: BookPage,
    totalPages: Int,
    activeHighlight: ActiveHighlight,
    isAudioPlaying: Boolean,
    isAiGenerating: Boolean,
    fontSizeMultiplier: Float,
    autoScrollEnabled: Boolean,
    onStartAudioAtPage: (Int) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val readerColors = LocalReaderColors.current

    // Auto-scroll logic: keeps highlighted text centered on screen
    LaunchedEffect(activeHighlight.charStart, isAudioPlaying, autoScrollEnabled) {
        if (autoScrollEnabled && isAudioPlaying && activeHighlight.pageNumber == page.pageNumber) {
            val targetIndex = (activeHighlight.paragraphIndex + 1).coerceAtMost(page.paragraphs.size)
            if (targetIndex >= 0) {
                listState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = -180
                )
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(readerColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Page Header Card
            PageHeaderCard(
                page = page,
                totalPages = totalPages
            )
        }

        // Special Illustration Pages
        if (page.isIllustration) {
            item {
                when (page.illustrationType) {
                    "FORGE" -> ForgeIllustrationCard()
                    "SPIRAL" -> SpiralIllustrationCard()
                }
            }
        }

        // Paragraphs of the page
        if (page.paragraphs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "— Page blanche intentionnelle —",
                        fontStyle = FontStyle.Italic,
                        color = readerColors.textSecondary.copy(alpha = 0.5f),
                        fontSize = (13 * fontSizeMultiplier).sp
                    )
                }
            }
        } else {
            itemsIndexed(page.paragraphs) { pIndex, paragraphText ->
                val isCurrentParagraphSpoken = (isAudioPlaying &&
                        activeHighlight.pageNumber == page.pageNumber &&
                        activeHighlight.paragraphIndex == pIndex)

                ParagraphCard(
                    text = paragraphText,
                    activeHighlight = if (isCurrentParagraphSpoken) activeHighlight else null,
                    fontSizeMultiplier = fontSizeMultiplier
                )
            }
        }

        // Prominent Page Navigation Buttons ("Précédent" / "Suivant")
        item {
            PageNavigationControls(
                currentPage = page.pageNumber,
                totalPages = totalPages,
                onPrevPage = onPrevPage,
                onNextPage = onNextPage
            )
        }

        // Page Footer ornament and page number
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LuminautesRosette(size = 22.dp, petalColor = readerColors.accent)
                Spacer(modifier = Modifier.height(8.dp))
                if (page.bookPageLabel.isNotBlank()) {
                    Text(
                        text = page.bookPageLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = readerColors.textSecondary,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(70.dp)) // Clearance for bottom player bar
            }
        }
    }
}

@Composable
fun PageNavigationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val readerColors = LocalReaderColors.current
    val hasPrev = currentPage > 1
    val hasNext = currentPage < totalPages

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = readerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bouton Précédent
            OutlinedButton(
                onClick = onPrevPage,
                enabled = hasPrev,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = readerColors.textPrimary,
                    disabledContentColor = readerColors.textSecondary.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasPrev) readerColors.accent.copy(alpha = 0.5f) else readerColors.textSecondary.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("page_nav_prev")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasPrev) "Page ${currentPage - 1}" else "Début",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Indicateur central
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(readerColors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$currentPage / $totalPages",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = readerColors.accent
                )
            }

            // Bouton Suivant
            Button(
                onClick = onNextPage,
                enabled = hasNext,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = readerColors.accent,
                    contentColor = if (readerColors.isDark) LumibookNavyDark else Color.White,
                    disabledContainerColor = readerColors.surfaceVariant,
                    disabledContentColor = readerColors.textSecondary.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("page_nav_next")
            ) {
                Text(
                    text = if (hasNext) "Page ${currentPage + 1}" else "Fin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PageHeaderCard(
    page: BookPage,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val readerColors = LocalReaderColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = readerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Page indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(readerColors.surfaceVariant)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Page ${page.pageNumber} sur $totalPages",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = readerColors.textSecondary
                    )
                }
            }

            if (page.chapterTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.chapterTitle,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = readerColors.accent,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }

            if (page.sectionTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = page.sectionTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = readerColors.textPrimary,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LuminautesRosette(size = 18.dp, petalColor = readerColors.accent)
        }
    }
}

@Composable
fun ParagraphCard(
    text: String,
    activeHighlight: ActiveHighlight?,
    fontSizeMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val readerColors = LocalReaderColors.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Calculate fractional line position based on active character offset
    val targetFractionalLine = remember(activeHighlight, textLayoutResult, text) {
        val layout = textLayoutResult
        if (activeHighlight == null || layout == null || text.isEmpty()) {
            0f
        } else {
            val charIndex = activeHighlight.charStart.coerceIn(0, (text.length - 1).coerceAtLeast(0))
            val line = layout.getLineForOffset(charIndex)
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)
            val lineChars = (lineEnd - lineStart).coerceAtLeast(1)
            val charProgress = ((charIndex - lineStart).toFloat() / lineChars.toFloat()).coerceIn(0f, 1f)
            line.toFloat() + charProgress
        }
    }

    // Butter-smooth interpolation of the continuous reading cursor along lines
    val animatedLinePos by animateFloatAsState(
        targetValue = targetFractionalLine,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "continuousLineHighlight"
    )

    // Gentle fade-in and fade-out for the entire paragraph card when narration starts or moves away
    val isParagraphActive = activeHighlight != null
    val paragraphAlpha by animateFloatAsState(
        targetValue = if (isParagraphActive) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "paragraphHighlightAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = readerColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .drawBehind {
                    val layout = textLayoutResult ?: return@drawBehind
                    if (paragraphAlpha <= 0.005f || layout.lineCount == 0) return@drawBehind

                    val pos = animatedLinePos
                    val baseLine = pos.toInt()
                    val p = (pos - baseLine).coerceIn(0f, 1f)
                    val totalLines = layout.lineCount

                    for (i in 0 until totalLines) {
                        // 3-line centered highlight:
                        // 1 line before the spoken line (baseLine - 1),
                        // the line currently read aloud (baseLine),
                        // and 1 line after (baseLine + 1) are all highlighted.
                        val rawLineAlpha = when {
                            totalLines == 1 -> 1f
                            totalLines == 2 -> {
                                if (baseLine == 0) {
                                    when (i) {
                                        0 -> 1f
                                        1 -> (0.65f + 0.35f * p).coerceIn(0.65f, 1f)
                                        else -> 0f
                                    }
                                } else {
                                    when (i) {
                                        0 -> (0.75f - 0.25f * p).coerceIn(0.50f, 0.75f)
                                        1 -> 1f
                                        else -> 0f
                                    }
                                }
                            }
                            else -> {
                                when {
                                    baseLine == 0 -> {
                                        // First line of paragraph
                                        when (i) {
                                            0 -> 1f // Line currently read
                                            1 -> (0.70f + 0.30f * p).coerceIn(0.70f, 1f) // Line after
                                            2 -> (p * 0.40f).coerceIn(0f, 0.40f) // Soft preview
                                            else -> 0f
                                        }
                                    }
                                    baseLine >= totalLines - 1 -> {
                                        // Last line of paragraph
                                        when (i) {
                                            baseLine - 2 -> ((1f - p) * 0.35f).coerceIn(0f, 0.35f) // Soft trail
                                            baseLine - 1 -> (0.75f - 0.25f * p).coerceIn(0.50f, 0.75f) // Line before
                                            baseLine -> 1f // Line currently read
                                            else -> 0f
                                        }
                                    }
                                    else -> {
                                        // 3 lines always highlighted (one before, spoken line, one after)
                                        when (i) {
                                            baseLine - 2 -> ((1f - p) * 0.35f).coerceIn(0f, 0.35f) // Soft fade-out trail
                                            baseLine - 1 -> (0.75f - 0.25f * p).coerceIn(0.50f, 0.75f) // 1 line before
                                            baseLine -> 1f // Spoken line
                                            baseLine + 1 -> (0.65f + 0.35f * p).coerceIn(0.65f, 1f) // 1 line after
                                            baseLine + 2 -> (p * 0.40f).coerceIn(0f, 0.40f) // Soft fade-in preview
                                            else -> 0f
                                        }
                                    }
                                }
                            }
                        }

                        val effectiveAlpha = (rawLineAlpha * paragraphAlpha).coerceIn(0f, 1f)
                        if (effectiveAlpha > 0.01f) {
                            val lineTop = layout.getLineTop(i)
                            val lineBottom = layout.getLineBottom(i)
                            val lineLeft = layout.getLineLeft(i)
                            val lineRight = layout.getLineRight(i)

                            val padH = 6.dp.toPx()
                            val padV = 2.dp.toPx()

                            val rLeft = (lineLeft - padH).coerceAtLeast(0f)
                            val rRight = (lineRight + padH).coerceAtMost(size.width)
                            val rTop = lineTop - padV
                            val rBottom = lineBottom + padV

                            // Soft luminescent pill behind text line - gentle and easy on the eyes
                            drawRoundRect(
                                color = readerColors.highlightBg.copy(alpha = 0.52f * effectiveAlpha),
                                topLeft = Offset(rLeft, rTop),
                                size = Size(
                                    (rRight - rLeft).coerceAtLeast(10f),
                                    (rBottom - rTop).coerceAtLeast(10f)
                                ),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }
        ) {
            Text(
                text = text,
                onTextLayout = { textLayoutResult = it },
                fontSize = (15.5 * fontSizeMultiplier).sp,
                lineHeight = (25 * fontSizeMultiplier).sp,
                color = readerColors.textPrimary,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Justify
            )
        }
    }
}
