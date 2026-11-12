package de.sprachgefuehl.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.ui.components.AdaptiveFrame
import de.sprachgefuehl.app.ui.components.AppBackground
import de.sprachgefuehl.app.ui.components.AppTabBar
import de.sprachgefuehl.app.ui.navigation.AppTab
import de.sprachgefuehl.app.ui.screens.AccountScreen
import de.sprachgefuehl.app.ui.screens.AdminScreen
import de.sprachgefuehl.app.ui.screens.CheckupScreen
import de.sprachgefuehl.app.ui.screens.CollectionScreen
import de.sprachgefuehl.app.ui.screens.GuideScreen
import de.sprachgefuehl.app.ui.screens.HomeScreen
import de.sprachgefuehl.app.ui.screens.LessonScreen
import de.sprachgefuehl.app.ui.screens.MapScreen
import de.sprachgefuehl.app.ui.screens.OnboardingScreen
import de.sprachgefuehl.app.ui.screens.PathsScreen
import de.sprachgefuehl.app.ui.screens.PracticeScreen
import de.sprachgefuehl.app.ui.screens.PreferencesScreen
import de.sprachgefuehl.app.ui.screens.ProfileScreen
import de.sprachgefuehl.app.ui.screens.RecapScreen
import de.sprachgefuehl.app.ui.screens.ReviewScreen
import de.sprachgefuehl.app.ui.screens.SearchScreen
import de.sprachgefuehl.app.ui.screens.SettingsScreen
import de.sprachgefuehl.app.ui.screens.SkillProfileScreen
import de.sprachgefuehl.app.ui.screens.SituationScreen
import de.sprachgefuehl.app.ui.screens.WorkshopScreen
import de.sprachgefuehl.app.ui.theme.LocalMotion
import de.sprachgefuehl.app.ui.theme.Spacing

@Composable
fun AppRoot(vm: AppViewModel, state: AppUiState) {
    val screen by vm.screen.collectAsStateWithLifecycle()
    val tab by vm.tab.collectAsStateWithLifecycle()
    val account by vm.account.collectAsStateWithLifecycle()
    val motion = LocalMotion.current

    // The bar belongs to the four roots. Inside a lesson or a detail page it disappears, so the
    // learner has one clear way forward instead of four tempting exits.
    // Während die App noch lädt, hat die Leiste nichts zu tun – ein Tipp darauf ins Leere wäre
    // nur verwirrend.
    // Ein Admin-Konto führt in die Konsole, nicht in die Lern-App: keine Tab-Leiste, kein Weg in
    // Lektionen. Das ist derselbe Login, aber ein anderer Ort.
    val showTabs = state.progress.onboarded && !state.loading && !account.isAdmin && screen == tab.root

    BackHandler(enabled = state.progress.onboarded && (screen != tab.root || tab != AppTab.START)) {
        if (!vm.back() && tab != AppTab.START) vm.openTab(AppTab.START)
    }

    // The Box paints the whole window (including the system bar insets); the Surface only provides
    // the content colour, so text without an explicit colour stays readable in the dark theme
    // instead of falling back to black.
    // Der Verlauf liegt hinter allem und scrollt nicht mit: Tiefe, ohne unruhig zu werden.
    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            val insets = if (showTabs) {
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
            } else {
                Modifier.safeDrawingPadding()
            }
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .weight(1f)
                    .then(insets),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    de.sprachgefuehl.app.ui.navigation.LocalTabRoot provides (screen == tab.root)
                ) {
                when {
                    state.loading -> LoadingState()
                    state.error != null -> ErrorState(state.error ?: "")
                    account.isAdmin -> AdminScreen(vm = vm, state = state, account = account)
                    !state.progress.onboarded -> AdaptiveFrame { OnboardingScreen(vm = vm, state = state) }
                    else -> AnimatedContent(
                        targetState = screen,
                        transitionSpec = {
                            (fadeIn(tween(motion.base, easing = motion.easing)) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = tween(motion.base, easing = motion.easing)
                                )).togetherWith(fadeOut(tween(motion.fast)))
                        },
                        label = "screen"
                    ) { target ->
                        AdaptiveFrame {
                        when (target) {
                            Screen.Home -> HomeScreen(vm, state)
                            Screen.Paths -> PathsScreen(vm, state)
                            is Screen.Situation -> SituationScreen(vm, state, target.id)
                            is Screen.Lesson -> LessonScreen(
                                vm = vm,
                                lessonId = target.id,
                                onExit = { if (!vm.back()) vm.navigate(Screen.Home) }
                            )
                            Screen.Review -> ReviewScreen(vm, state)
                            Screen.Map -> MapScreen(vm, state)
                            Screen.Workshop -> WorkshopScreen(vm, state)
                            Screen.Collection -> CollectionScreen(vm, state)
                            Screen.Practice -> PracticeScreen(vm, state)
                            Screen.Recap -> RecapScreen(vm, state)
                            Screen.Profile -> ProfileScreen(vm, state)
                            Screen.Settings -> SettingsScreen(vm, state)
                            Screen.Search -> SearchScreen(vm, state)
                            Screen.SkillProfile -> SkillProfileScreen(vm, state)
                            Screen.Preferences -> PreferencesScreen(vm, state)
                            Screen.Checkup -> CheckupScreen(vm, state)
                            Screen.Guide -> GuideScreen(vm)
                            Screen.Account -> AccountScreen(vm, state)
                            Screen.Onboarding -> OnboardingScreen(vm, state)
                        }
                        }
                    }
                }
                }
            }
            if (showTabs) {
                AppTabBar(active = tab, onSelect = { vm.openTab(it) })
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(Spacing.m))
        Text("Sprachgefühl wird vorbereitet …", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Etwas ist schiefgelaufen", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(Spacing.s))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}
