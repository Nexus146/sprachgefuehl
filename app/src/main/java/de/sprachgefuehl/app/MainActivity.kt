package de.sprachgefuehl.app

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.sprachgefuehl.app.ui.AppRoot
import de.sprachgefuehl.app.ui.theme.LocalMotion
import de.sprachgefuehl.app.ui.theme.Motion
import de.sprachgefuehl.app.ui.theme.SprachgefuehlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (state.progress.settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }
            SprachgefuehlTheme(
                darkTheme = darkTheme,
                fontScale = state.progress.settings.fontScale
            ) {
                LaunchedEffect(state.progress.settings.soundEnabled, state.progress.settings.speechRate) {
                    de.sprachgefuehl.app.speech.SpeechDefaults.enabled = state.progress.settings.soundEnabled
                    de.sprachgefuehl.app.speech.SpeechDefaults.rate = state.progress.settings.speechRate
                }
                SystemBarTint(darkTheme)
                CompositionLocalProvider(
                    LocalMotion provides Motion(reduced = state.progress.settings.reducedMotion)
                ) {
                    AppRoot(vm = vm, state = state)
                }
            }
        }
    }
}

/**
 * The activity theme paints the status and navigation bar cream. As soon as the learner picks the
 * dark mode by hand, the bars have to follow the Compose theme instead of the system setting.
 */
@Composable
private fun SystemBarTint(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        @Suppress("DEPRECATION")
        run {
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
        }
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
