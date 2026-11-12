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

val NightBackground = Color(0xFF121310)
val NightSurface = Color(0xFF1C2019)
val NightSurfaceHigh = Color(0xFF2A2F26)
val NightInk = Color(0xFFF2EEE5)
val NightInkSoft = Color(0xFFC6C0B4)
val NightLine = Color(0xFF383D32)

/**
 * Umriss von Knöpfen und Karten: im Dunkeln heller, damit die Kante bei 3:1 sichtbar bleibt –
 * aber neutraler als vorher, sonst wirkt jede Karte grünlich umrandet.
 */
val NightOutline = Color(0xFF6A7160)
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
    onError = Color.White,
    // Material füllt Schalter, Textfelder und Dialoge aus dieser Familie. Ohne eigene Werte
    // nimmt es die Violett-Vorgaben – im hellen Thema fällt das kaum auf, im dunklen sofort.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF5ED),
    surfaceContainer = Color(0xFFF4EDE3),
    surfaceContainerHigh = Color(0xFFF0E9DE),
    surfaceContainerHighest = Color(0xFFEAE2D6),
    surfaceDim = Color(0xFFEDE6DA),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceTint = Sage,
    inverseSurface = Ink,
    inverseOnSurface = Cream,
    errorContainer = TerracottaSoft,
    onErrorContainer = Color(0xFF6B2F14)
)

private val DarkScheme = darkColorScheme(
    primary = NightSage,
    onPrimary = Color(0xFF14251A),
    primaryContainer = Color(0xFF2F4635),
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
    onError = Color(0xFF3A0F08),
    // Ohne diese Werte zeichnete Material Schalter und Textfelder in seinen Violett-Vorgaben –
    // der ausgeschaltete Schalter wirkte wie ein Fremdkörper im Garten-Grün.
    surfaceContainerLowest = Color(0xFF0D0E0B),
    surfaceContainerLow = Color(0xFF171A14),
    surfaceContainer = NightSurface,
    surfaceContainerHigh = Color(0xFF23271F),
    surfaceContainerHighest = NightSurfaceHigh,
    surfaceDim = NightBackground,
    surfaceBright = Color(0xFF2E332A),
    surfaceTint = NightSage,
    inverseSurface = NightInk,
    inverseOnSurface = NightBackground,
    errorContainer = Color(0xFF4A2019),
    onErrorContainer = Color(0xFFFFDAD3)
)

data class SprachColors(
    val leaf: Color,
    val leafSoft: Color,
    val success: Color,
    val warning: Color,
    val highlight: Color,
    val pot: Color,
    val bloomSun: Color,
    val bloomRose: Color,
    val bloomLavender: Color,
    val bloomTulip: Color,
    val bloomIvy: Color
)

val LightExtra = SprachColors(
    leaf = Sage,
    leafSoft = SageSoft,
    success = Color(0xFF3F6B4F),
    warning = Terracotta,
    highlight = Color(0xFFFFF1C9),
    pot = Color(0xFF6B4E16),
    bloomSun = Color(0xFFDDA32B),
    bloomRose = Color(0xFFC0504D),
    bloomLavender = Color(0xFF8B6FB0),
    bloomTulip = Color(0xFFDE7448),
    bloomIvy = Color(0xFF55648F)
)

/**
 * Im Dunkeln braucht der Garten eigene Töne: Das dunkle Braun des Topfes und das tiefe Veilchen
 * der Blüten verschwanden vorher fast auf der dunklen Karte. Die Formen bleiben gleich, nur die
 * Farben sind aufgehellt und entsättigt.
 */
val DarkExtra = SprachColors(
    leaf = NightSage,
    leafSoft = Color(0xFF2B4030),
    success = NightSage,
    warning = NightTerracotta,
    highlight = Color(0xFF4A4023),
    pot = Color(0xFFC39A6B),
    bloomSun = Color(0xFFE7BE68),
    bloomRose = Color(0xFFE8938C),
    bloomLavender = Color(0xFFBFA9E2),
    bloomTulip = Color(0xFFF0A783),
    bloomIvy = Color(0xFF9AACDC)
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
