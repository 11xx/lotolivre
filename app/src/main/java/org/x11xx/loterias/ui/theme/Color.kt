package org.x11xx.loterias.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Every game's colour is one hue rendered at a lightness and chroma the theme
 * fixes for all of them, so no game shouts louder than another and the same
 * pairing holds its contrast in both themes. Deriving the colours costs less
 * than maintaining eleven hand-picked palettes twice over.
 */
@Immutable
data class GameColors(
    val accent: Color,
    val soft: Color,
    val softInk: Color,
    val wash: Color,
    val edge: Color,
    val onAccent: Color,
)

fun gameColors(hue: Float, dark: Boolean): GameColors = if (dark) {
    GameColors(
        accent = oklch(0.80f, 0.14f, hue),
        soft = oklch(0.33f, 0.070f, hue),
        softInk = oklch(0.93f, 0.065f, hue),
        wash = oklch(0.225f, 0.026f, hue),
        edge = oklch(0.345f, 0.048f, hue),
        onAccent = oklch(0.20f, 0.045f, hue),
    )
} else {
    GameColors(
        accent = oklch(0.54f, 0.15f, hue),
        soft = oklch(0.945f, 0.045f, hue),
        softInk = oklch(0.40f, 0.12f, hue),
        wash = oklch(0.982f, 0.016f, hue),
        edge = oklch(0.905f, 0.038f, hue),
        onAccent = oklch(0.99f, 0.005f, hue),
    )
}

/** OKLCH to sRGB, so lightness reads as lightness across every hue. */
fun oklch(lightness: Float, chroma: Float, hueDegrees: Float): Color {
    val hue = Math.toRadians(hueDegrees.toDouble())
    val a = chroma * cos(hue).toFloat()
    val b = chroma * sin(hue).toFloat()

    val l = (lightness + 0.3963377774f * a + 0.2158037573f * b).pow(3)
    val m = (lightness - 0.1055613458f * a - 0.0638541728f * b).pow(3)
    val s = (lightness - 0.0894841775f * a - 1.2914855480f * b).pow(3)

    return Color(
        red = encode(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s),
        green = encode(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s),
        blue = encode(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s),
    )
}

private fun encode(linear: Float): Float {
    val value = if (linear <= 0.0031308f) 12.92f * linear else 1.055f * linear.pow(1f / 2.4f) - 0.055f
    return value.coerceIn(0f, 1f)
}

// Neutrals. Warm-free and low-chroma so the game hues carry all the colour.
val LightBackground = Color(0xFFFAF9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightInk = Color(0xFF191A1F)
val LightMuted = Color(0xFF54535D)
val LightLine = Color(0xFFE4E2EA)

val DarkBackground = Color(0xFF0F1014)
val DarkSurface = Color(0xFF18181E)
val DarkInk = Color(0xFFE8E5ED)
val DarkMuted = Color(0xFFACAAB6)
val DarkLine = Color(0xFF2B2A33)
