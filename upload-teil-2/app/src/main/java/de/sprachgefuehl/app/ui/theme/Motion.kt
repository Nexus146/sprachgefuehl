package de.sprachgefuehl.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.staticCompositionLocalOf

data class Motion(
    val reduced: Boolean = false
) {
    val easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    val fast: Int = if (reduced) 80 else 120
    val base: Int = if (reduced) 100 else 220
    val slow: Int = if (reduced) 120 else 380
    val garden: Int = if (reduced) 200 else 900
}

val LocalMotion = staticCompositionLocalOf { Motion() }
