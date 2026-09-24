package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookGold
import com.example.ui.theme.LumibookNavyDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LumibookLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LumibookNavyDark),
        contentAlignment = Alignment.Center
    ) {
        // Concentric yellow ring surrounding the flower
        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .border(
                    width = (size * 0.05f).coerceAtLeast(1.5.dp),
                    color = LumibookGold,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            LuminautesRosette(
                size = size * 0.70f,
                petalColor = LumibookGold
            )
        }
    }
}

@Composable
fun LuminautesRosette(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    petalColor: Color = LumibookGold
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val radius = this.size.minDimension / 2.2f

        // Central radiant circle
        drawCircle(
            color = petalColor,
            radius = radius * 0.16f,
            center = Offset(cx, cy)
        )

        // 6 Petals with loops
        for (i in 0 until 6) {
            val angle = i * 60f
            rotate(degrees = angle, pivot = Offset(cx, cy)) {
                val petalPath = Path().apply {
                    val pTop = cy - radius * 0.85f
                    val pMid = cy - radius * 0.42f
                    val pBase = cy - radius * 0.12f
                    val widthOuter = radius * 0.32f

                    moveTo(cx, pBase)
                    cubicTo(cx - widthOuter, pMid, cx - widthOuter * 0.8f, pTop, cx, pTop)
                    cubicTo(cx + widthOuter * 0.8f, pTop, cx + widthOuter, pMid, cx, pBase)
                    close()
                }
                drawPath(path = petalPath, color = petalColor)

                // Outer decorative light dot
                drawCircle(
                    color = petalColor,
                    radius = radius * 0.07f,
                    center = Offset(cx, cy - radius * 0.98f)
                )
            }
        }
    }
}

@Composable
fun ForgeIllustrationCard(modifier: Modifier = Modifier) {
    val readerColors = LocalReaderColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(readerColors.surfaceVariant)
            .border(1.dp, readerColors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FORGE TOUS RISQUES !",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = readerColors.accent,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "ASSURANCE TOUT RISQUES",
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = readerColors.textSecondary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Author's symbolic sketch canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val w = size.width
            val h = size.height
            val strokeColor = readerColors.textPrimary
            val accentColor = readerColors.accent

            // Sun on top right
            drawCircle(color = accentColor, radius = 24.dp.toPx(), center = Offset(w * 0.8f, h * 0.2f), style = Stroke(2.5f))
            for (i in 0 until 8) {
                val a = i * (Math.PI / 4)
                val x1 = w * 0.8f + (cos(a) * 28.dp.toPx()).toFloat()
                val y1 = h * 0.2f + (sin(a) * 28.dp.toPx()).toFloat()
                val x2 = w * 0.8f + (cos(a) * 38.dp.toPx()).toFloat()
                val y2 = h * 0.2f + (sin(a) * 38.dp.toPx()).toFloat()
                drawLine(color = accentColor, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 2f)
            }

            // Moon with craters on top left
            drawCircle(color = strokeColor, radius = 22.dp.toPx(), center = Offset(w * 0.2f, h * 0.2f), style = Stroke(2.5f))
            drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(w * 0.17f, h * 0.18f))
            drawCircle(color = strokeColor, radius = 3.dp.toPx(), center = Offset(w * 0.23f, h * 0.22f))

            // Central spiritual silhouette (the author / spiritual scarecrow / warrior)
            val bodyCx = w * 0.5f
            // Head
            drawCircle(color = strokeColor, radius = 26.dp.toPx(), center = Offset(bodyCx, h * 0.38f), style = Stroke(2.5f))
            // Left eye: full pupil with 3 tears
            drawCircle(color = strokeColor, radius = 5.dp.toPx(), center = Offset(bodyCx - 10.dp.toPx(), h * 0.37f))
            // 3 Tears
            drawLine(color = accentColor, start = Offset(bodyCx - 10.dp.toPx(), h * 0.40f), end = Offset(bodyCx - 12.dp.toPx(), h * 0.45f), strokeWidth = 2.5f)
            drawLine(color = accentColor, start = Offset(bodyCx - 9.dp.toPx(), h * 0.45f), end = Offset(bodyCx - 11.dp.toPx(), h * 0.49f), strokeWidth = 2.5f)
            // Right eye: hollow pupil
            drawCircle(color = strokeColor, radius = 5.dp.toPx(), center = Offset(bodyCx + 10.dp.toPx(), h * 0.37f), style = Stroke(2f))

            // Body tunic with peace sign and yin-yang
            drawLine(color = strokeColor, start = Offset(bodyCx, h * 0.46f), end = Offset(bodyCx, h * 0.85f), strokeWidth = 3f)
            drawLine(color = strokeColor, start = Offset(bodyCx - 36.dp.toPx(), h * 0.55f), end = Offset(bodyCx + 36.dp.toPx(), h * 0.55f), strokeWidth = 2.5f)
            // Legs
            drawLine(color = strokeColor, start = Offset(bodyCx, h * 0.85f), end = Offset(bodyCx - 24.dp.toPx(), h * 0.98f), strokeWidth = 3f)
            drawLine(color = strokeColor, start = Offset(bodyCx, h * 0.85f), end = Offset(bodyCx + 24.dp.toPx(), h * 0.98f), strokeWidth = 3f)

            // Steering wheel on bottom left
            drawCircle(color = accentColor, radius = 18.dp.toPx(), center = Offset(w * 0.2f, h * 0.82f), style = Stroke(2.5f))
            drawLine(color = accentColor, start = Offset(w * 0.2f - 18.dp.toPx(), h * 0.82f), end = Offset(w * 0.2f + 18.dp.toPx(), h * 0.82f), strokeWidth = 2f)
            drawLine(color = accentColor, start = Offset(w * 0.2f, h * 0.82f - 18.dp.toPx()), end = Offset(w * 0.2f, h * 0.82f + 18.dp.toPx()), strokeWidth = 2f)

            // Mountains on left
            val mPath = Path().apply {
                moveTo(w * 0.08f, h * 0.58f)
                lineTo(w * 0.16f, h * 0.46f)
                lineTo(w * 0.24f, h * 0.58f)
                lineTo(w * 0.30f, h * 0.48f)
                lineTo(w * 0.36f, h * 0.58f)
            }
            drawPath(path = mPath, color = strokeColor, style = Stroke(2f))

            // Snake and flower on right
            val snakePath = Path().apply {
                moveTo(w * 0.75f, h * 0.88f)
                cubicTo(w * 0.78f, h * 0.84f, w * 0.82f, h * 0.92f, w * 0.86f, h * 0.87f)
                cubicTo(w * 0.89f, h * 0.82f, w * 0.92f, h * 0.86f, w * 0.94f, h * 0.83f)
            }
            drawPath(path = snakePath, color = accentColor, style = Stroke(2.5f, cap = StrokeCap.Round))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Page 25 du manuscrit : \"Une âme qui, pour se rebâtir, a accepté de forger tous risques\"",
            fontSize = 11.sp,
            color = readerColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SpiralIllustrationCard(modifier: Modifier = Modifier) {
    val readerColors = LocalReaderColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(readerColors.surfaceVariant)
            .border(1.dp, readerColors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LA SPIRALE DE L'ÉPREUVE",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = readerColors.accent,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Author's Archimedean Spiral Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = size.minDimension * 0.44f

            val spiralPath = Path()
            val loops = 4.2f
            val totalSteps = 400
            val maxAngle = loops * 2f * Math.PI.toFloat()

            for (i in 0..totalSteps) {
                val t = i.toFloat() / totalSteps
                val theta = t * maxAngle
                val r = t * maxR
                val x = cx + r * cos(theta)
                val y = cy + r * sin(theta)
                if (i == 0) {
                    spiralPath.moveTo(x, y)
                } else {
                    spiralPath.lineTo(x, y)
                }
            }

            drawPath(
                path = spiralPath,
                color = readerColors.accent,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Inner starting light core
            drawCircle(
                color = LumibookGold,
                radius = 6.dp.toPx(),
                center = Offset(cx, cy)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Author's handwritten message box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(readerColors.surface)
                .padding(12.dp)
        ) {
            Text(
                text = "“ Remind yourself that there is always a way out !\nJust balance, enjoy life but try to see the difference in everything,\nI will get out, I will survive, One Love, Jah Rastafari. ”",
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = readerColors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
