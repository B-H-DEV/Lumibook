package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookGold
import com.example.ui.theme.LumibookNavyDark
import com.example.ui.theme.ReaderThemeMode
import com.example.util.PdfExporter

@Composable
fun ReaderTopBar(
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    currentTheme: ReaderThemeMode,
    onThemeSelected: (ReaderThemeMode) -> Unit,
    fontSizeMultiplier: Float,
    onFontSizeChange: (Float) -> Unit,
    onOpenTableOfContents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val readerColors = LocalReaderColors.current
    var showThemeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = readerColors.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Main Top Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Logo and Book Title (Removed "Lumibook", now "Les Luminautes" and "La Voie de Lumière")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onOpenTableOfContents() }
                ) {
                    LumibookLogoBadge(size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Les Luminautes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = readerColors.textPrimary,
                            fontFamily = FontFamily.Serif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "La Voie de Lumière",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = readerColors.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Middle: Compact Original Printable PDF Download Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(readerColors.accent.copy(alpha = 0.12f))
                        .border(1.dp, readerColors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable {
                            PdfExporter.exportAndOpenPdf(context)
                        }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                        .testTag("download_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Télécharger ou imprimer le PDF original",
                        tint = readerColors.accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "PDF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = readerColors.accent
                    )
                }

                // Right: Compact luminautes.org web button (sphere/globe icon) & Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // luminautes.org compact web button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(readerColors.surfaceVariant)
                            .border(1.dp, readerColors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://luminautes.org"))
                                context.startActivity(intent)
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("luminautes_org_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Visiter le site luminautes.org",
                            tint = readerColors.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Web",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = readerColors.accent
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onOpenTableOfContents,
                        modifier = Modifier.size(34.dp).testTag("toc_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Table des matières",
                            tint = readerColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showThemeMenu = !showThemeMenu },
                        modifier = Modifier.size(34.dp).testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Changer le thème",
                            tint = readerColors.accent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Quick Toolbar Row (Themes & Font Size)
            if (showThemeMenu) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(readerColors.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Theme Chips: Clair, Sépia, Sombre
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReaderThemeMode.values().forEach { mode ->
                            val isSelected = mode == currentTheme
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) readerColors.accent else readerColors.surface)
                                    .clickable { onThemeSelected(mode) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("theme_chip_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) (if (readerColors.isDark) LumibookNavyDark else androidx.compose.ui.graphics.Color.White) else readerColors.textPrimary
                                )
                            }
                        }
                    }

                    // Font Size Adjuster: A- / A+
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onFontSizeChange((fontSizeMultiplier - 0.1f).coerceAtLeast(0.8f)) },
                            modifier = Modifier.size(32.dp).testTag("font_decrease")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextDecrease,
                                contentDescription = "Réduire la police",
                                tint = readerColors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${(fontSizeMultiplier * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = readerColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        IconButton(
                            onClick = { onFontSizeChange((fontSizeMultiplier + 0.1f).coerceAtMost(1.6f)) },
                            modifier = Modifier.size(32.dp).testTag("font_increase")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextIncrease,
                                contentDescription = "Agrandir la police",
                                tint = readerColors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
