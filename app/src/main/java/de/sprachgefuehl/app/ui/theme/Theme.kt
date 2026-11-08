package de.sprachgefuehl.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.sprachgefuehl.app.R

// Warm, calm palette: cream paper, sage green, terracotta accent.
val Cream = Color(0xFFFBF7F1)
val Paper = Color(0xFFFFFFFF)
val Sand = Color(0xFFF0E9DE)
val Sage = Color(0xFF3F6B4F)
val SageDark = Color(0xFF2C4C38)
val SageSoft = Color(0xFFE3EDE3)
val Terracotta = Color(0xFFB85C33)
val TerracottaSoft = Color(0xFFF6E3D8)
val Ink = Color(0xFF23201C)
val InkSoft = Color(0xFF5A544C)
val InkFaint = Color(0xFF8A8175)
val Line = Color(0xFFE0D7C9)

val NightBackground = Color(0xFF14150F)
val NightSurface = Color(0xFF1E211A)
val NightSurfaceHigh = Color(0xFF2A2E24)
val NightInk = Color(0xFFF2EEE5)
val NightInkSoft = Color(0xFFC3BDB1)
val NightLine = Color(0xFF3A3F33)

/** Umriss von Knöpfen und Karten: im Dunkeln heller, damit die Kante bei 3:1 sichtbar bleibt. */
val NightOutline = Color(0xFF6A7159)
val NightSage = Color(0xFF9CC4A9)
val NightTerracotta = Color(0xFFE0A07C)

object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Sizes {
    val touch = 56.dp
    val button = 64.dp
    val corner = 24.dp
    val cornerSmall = 16.dp
    val border = 1.5.dp
}

val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_bold, FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
val LexendFamily = FontFamily(
    Font(
        R.font.lexend,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.lexend,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 27.sp),
    bodySmall = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    labelLarge = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    labelMedium = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp)
)

private val LightScheme = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = SageDark,
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = TerracottaSoft,
    onSecondaryContainer = Color(0xFF6B2F14),
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = Line,
    error = Color(0xFF9C3B2E),
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = NightSage,
    onPrimary = Color(0xFF14251A),
    primaryContainer = Color(0xFF2B4030),
    onPrimaryContainer = Color(0xFFD6EBDC),
    secondary = NightTerracotta,
    onSecondary = Color(0xFF3A1A0C),
    secondaryContainer = Color(0xFF4A2B1B),
    onSecondaryContainer = Color(0xFFF6DCCB),
    background = NightBackground,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightInkSoft,
    outline = NightOutline,
    outlineVariant = NightLine,
    error = Color(0xFFE6A79B),
    onError = Color(0xFF3A0F08)
)

data class SprachColors(
    val leaf: Color,
    val leafSoft: Color,
    val success: Color,
    val warning: Color,
    val highlight: Color
)

val LightExtra = SprachColors(
    leaf = Sage,
    leafSoft = SageSoft,
    success = Color(0xFF3F6B4F),
    warning = Terracotta,
    highlight = Color(0xFFFFF1C9)
)

val DarkExtra = SprachColors(
    leaf = NightSage,
    leafSoft = Color(0xFF2B4030),
    success = NightSage,
    warning = NightTerracotta,
    highlight = Color(0xFF4A4023)
)

val LocalSprachColors = staticCompositionLocalOf { LightExtra }

@Composable
fun SprachgefuehlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scheme = if (darkTheme) DarkScheme else LightScheme
    CompositionLocalProvider(
        LocalSprachColors provides if (darkTheme) DarkExtra else LightExtra,
        LocalDensity provides Density(density = density.density, fontScale = fontScale.coerceIn(0.9f, 1.6f))
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content
        )
    }
}
