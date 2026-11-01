package de.sprachgefuehl.app.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import de.sprachgefuehl.app.Screen

/**
 * The four places a learner can be. Everything else is a detail view opened from one of them,
 * so the bar never has to guess where "back" goes.
 */
enum class AppTab(val label: String, val icon: String, val root: Screen) {
    START("Start", "home", Screen.Home),
    PRACTICE("Üben", "mic", Screen.Practice),
    // "Woche" instead of "Rückblick": the tab label has to fit next to the icon even when the
    // learner chooses the largest font size.
    RECAP("Woche", "leaf", Screen.Recap),
    PROFILE("Profil", "person", Screen.Profile)
}

/** True while a tab root is showing: there the bottom bar is the way back, not the arrow. */
val LocalTabRoot = compositionLocalOf { false }
