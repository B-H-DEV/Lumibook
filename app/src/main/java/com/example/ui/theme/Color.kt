package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default brand colors for Lumibook
val LumibookNavyDark = Color(0xFF070B19)
val LumibookNavyCard = Color(0xFF10182E)
val LumibookGold = Color(0xFFFFD54F)
val LumibookGoldVariant = Color(0xFFF59E0B)

enum class ReaderThemeMode(val label: String) {
    LIGHT("Clair"),
    SEPIA("Sépia"),
    DARK("Sombre")
}

data class ReaderColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val highlightBg: Color,
    val highlightText: Color,
    val isDark: Boolean
)

val LightReaderColors = ReaderColors(
    background = Color(0xFFF6F6F4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFEFEA),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF64748B),
    accent = Color(0xFFD97706),
    highlightBg = Color(0xFFFEF08A),
    highlightText = Color(0xFF854D0E),
    isDark = false
)

val SepiaReaderColors = ReaderColors(
    background = Color(0xFFD8C394),       // Fond général bien jauni et assombri, zéro éblouissement
    surface = Color(0xFFEAD8B2),          // Arrière-plan des paragraphes plus chaud/jauni (fini le blanc éclatant)
    surfaceVariant = Color(0xFFC8B383),   // Nuance d'appui sépia ambrée
    textPrimary = Color(0xFF2C1D10),      // Texte bistre foncé très lisible et reposant
    textSecondary = Color(0xFF634D34),    // Texte secondaire doux
    accent = Color(0xFF8E4506),           // Accent chaud terre de Sienne
    highlightBg = Color(0xFFF3CE66),      // Surlignage mot-à-mot ambré
    highlightText = Color(0xFF452002),
    isDark = false
)

val DarkReaderColors = ReaderColors(
    background = Color(0xFF070B18),
    surface = Color(0xFF10172B),
    surfaceVariant = Color(0xFF19223D),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    accent = Color(0xFFFFD54F),
    highlightBg = Color(0xFF614E15),
    highlightText = Color(0xFFFFFBEB),
    isDark = true
)
