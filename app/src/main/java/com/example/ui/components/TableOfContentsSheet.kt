package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookPage
import com.example.data.BookRepository
import com.example.ui.theme.LocalReaderColors
import com.example.ui.theme.LumibookGold
import com.example.ui.theme.LumibookNavyDark

data class TocItem(
    val title: String,
    val subtitle: String,
    val pageNumber: Int
)

val TocItems = listOf(
    TocItem("Prologue", "Ressentez-vous parfois ce léger décalage ?", 3),
    TocItem("1. Les Fondations", "1.1 Les Illusions de l'Appartenance et du Conflit", 7),
    TocItem("1. Les Fondations", "1.2 Le Trouble des Repères", 11),
    TocItem("1. Les Fondations", "1.3 La Fièvre du Monde : Une Leçon de Thermodynamique", 13),
    TocItem("1. Les Fondations", "1.4 Le Rêve comme Boussole, non comme Refuge", 15),
    TocItem("2. L'Art de la Juste Maîtrise", "2.1 Le Baromètre de la Conscience", 17),
    TocItem("2. L'Art de la Juste Maîtrise", "2.2 Les Chemins de la Pensée : Le Cercle et le Tourbillon", 19),
    TocItem("2. L'Art de la Juste Maîtrise", "2.3 Le Chemin de la Sagesse", 21),
    TocItem("3. La Clarté et l'Épreuve", "3.1 L'Éveil", 25),
    TocItem("3. La Clarté et l'Épreuve", "3.2 L'espoir d'un fou", 27),
    TocItem("3. La Clarté et l'Épreuve", "3.3 Forge Tous Risques ! (Dessin & Écrits)", 31),
    TocItem("4. La Cristallisation d'une Vision", "4.1 En ces temps...", 37),
    TocItem("4. La Cristallisation d'une Vision", "4.2 La Naissance d'une Idée", 41),
    TocItem("4. La Cristallisation d'une Vision", "4.3 Les Trois Piliers de la Voie", 43),
    TocItem("4. La Cristallisation d'une Vision", "4.4 Le Dialogue des Sagesses", 47),
    TocItem("5. L'Épreuve du Feu", "5.1 Le Combat : Le Laboratoire de l'Âme", 49),
    TocItem("5. L'Épreuve du Feu", "5.2 La Clé : La Spirale de l'Épreuve (Dessin & Texte)", 53),
    TocItem("6. Derrière la Porte", "6.1 La Mission et la Cosmologie", 55),
    TocItem("6. Derrière la Porte", "6.2 Le Mécanisme des Luminautes", 57),
    TocItem("6. Derrière la Porte", "6.3 Le Manifeste du Passeur de Lumière", 59)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsSheet(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val readerColors = LocalReaderColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = readerColors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LuminautesRosette(size = 28.dp, petalColor = readerColors.accent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Table des matières",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = readerColors.textPrimary,
                        fontFamily = FontFamily.Serif
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = readerColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TocItems) { item ->
                    val isCurrent = item.pageNumber == currentPage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) readerColors.accent.copy(alpha = 0.18f) else readerColors.surfaceVariant)
                            .clickable {
                                onPageSelected(item.pageNumber)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("toc_item_${item.pageNumber}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = readerColors.accent
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = readerColors.textPrimary,
                                    fontFamily = FontFamily.Serif
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(readerColors.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "p. ${item.pageNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = readerColors.textSecondary
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
