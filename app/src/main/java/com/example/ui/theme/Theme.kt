package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalReaderColors = staticCompositionLocalOf { LightReaderColors }

private val DarkColorScheme = darkColorScheme(
    primary = LumibookGold,
    secondary = LumibookGoldVariant,
    tertiary = Color(0xFF38BDF8),
    background = LumibookNavyDark,
    surface = LumibookNavyCard,
    onPrimary = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309),
    secondary = LumibookGoldVariant,
    tertiary = Color(0xFF0284C7),
    background = Color(0xFFF6F6F4),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun LumibookTheme(
    readerThemeMode: ReaderThemeMode = ReaderThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val readerColors = when (readerThemeMode) {
        ReaderThemeMode.LIGHT -> LightReaderColors
        ReaderThemeMode.SEPIA -> SepiaReaderColors
        ReaderThemeMode.DARK -> DarkReaderColors
    }

    val colorScheme = if (readerColors.isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = readerColors.background.toArgb()
            window.navigationBarColor = readerColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !readerColors.isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !readerColors.isDark
        }
    }

    CompositionLocalProvider(LocalReaderColors provides readerColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
