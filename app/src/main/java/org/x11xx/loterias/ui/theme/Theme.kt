package org.x11xx.loterias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** What the user chose in the drawer; the system's setting is the default. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

val LocalDarkTheme = staticCompositionLocalOf { false }

private val LightScheme = lightColorScheme(
    background = LightBackground,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightMuted,
    outlineVariant = LightLine,
    primary = LightInk,
    onPrimary = LightSurface,
    // Selection states are neutral too: the only colour in the app belongs to
    // a game, and a stray tint from the default palette would read as one.
    secondaryContainer = LightLine,
    onSecondaryContainer = LightInk,
)

private val DarkScheme = darkColorScheme(
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkMuted,
    outlineVariant = DarkLine,
    primary = DarkInk,
    onPrimary = DarkSurface,
    secondaryContainer = DarkLine,
    onSecondaryContainer = DarkInk,
)

@Composable
fun LotoLivreTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
