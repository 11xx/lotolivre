package org.x11xx.loterias.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val displayFont = GoogleFont("Google Sans")

/**
 * One family throughout, downloaded rather than packaged. If the provider
 * cannot serve it the platform's own sans stands in, which is why no style
 * depends on a metric only this face has.
 */
val AppFontFamily = FontFamily(
    Font(googleFont = displayFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = displayFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = displayFont, fontProvider = provider, weight = FontWeight.Bold),
)

/** Figures line up column to column throughout: dezenas, dates and money. */
private const val TABULAR = "tnum"

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontFeatureSettings = TABULAR,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)
