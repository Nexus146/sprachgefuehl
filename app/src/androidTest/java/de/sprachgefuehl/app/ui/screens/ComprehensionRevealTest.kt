package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.sprachgefuehl.app.data.Question
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.SprachgefuehlTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prüft an der echten Verständnisaufgabe, dass die gewählte Zeile für Screenreader ausgezeichnet
 * ist und nach dem Prüfen zusammen mit der Rückmeldung sichtbar bleibt.
 */
@RunWith(AndroidJUnit4::class)
class ComprehensionRevealTest {

    @get:Rule
    val rule = createComposeRule()

    private val step = Step(
        id = "comprehension-reveal",
        type = "ComprehensionDeep",
        title = "Verstanden?",
        promptDe = "Hör noch einmal und antworte.",
        questions = listOf(
            Question(
                prompt = "Wie ist das Wetter?",
                options = listOf("Schön und wärmer als gestern", "Kalt und regnerisch"),
                answer = 1
            )
        )
    )

    @Test
    fun gewaehlteZeileIstAusgezeichnetUndBleibtSichtbar() {
        val chosen = "Kalt und regnerisch"
        val other = "Schön und wärmer als gestern"
        lateinit var scrollState: ScrollState

        rule.setContent {
            SprachgefuehlTheme {
                val speech = rememberSpeechController()
                scrollState = rememberScrollState()
                var solved by remember { mutableStateOf(false) }
                var viewportBottom by remember { mutableFloatStateOf(0f) }
                var feedbackBottom by remember { mutableFloatStateOf(0f) }
                var feedbackPlaced by remember { mutableStateOf(false) }

                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .weight(1f)
                            .testTag("viewport")
                            .onGloballyPositioned { coordinates ->
                                viewportBottom = coordinates.positionInRoot().y + coordinates.size.height
                            }
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(Modifier.height(320.dp))
                        ComprehensionStepForTest(
                            step = step,
                            speech = speech,
                            enabled = !solved,
                            onSolved = { solved = true }
                        )
                        if (solved) {
                            Box(
                                Modifier.onGloballyPositioned {
                                    feedbackBottom = it.positionInRoot().y + it.size.height
                                    feedbackPlaced = true
                                }
                            ) {
                                FeedbackPanel(
                                    tone = FeedbackTone.GOOD,
                                    title = "Jetzt sitzt er",
                                    detail = "Du hast die richtige Aussage erkannt.",
                                    model = "It is cold and rainy.",
                                    onSpeakModel = {}
                                )
                            }
                        }
                        Spacer(Modifier.height(240.dp))
                    }
                    if (solved) {
                        Surface(shadowElevation = 8.dp) {
                            BigButton("Weiter", onClick = {}, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                LaunchedEffect(solved, feedbackPlaced) {
                    if (!solved || !feedbackPlaced) return@LaunchedEffect
                    withFrameNanos { }
                    val delta = revealDelta(
                        feedbackBottom = feedbackBottom,
                        viewportBottom = viewportBottom,
                        answerTop = 0f,
                        viewportTop = 0f,
                        answerPlaced = false
                    )
                    if (delta > 1f) {
                        scrollState.animateScrollTo(
                            (scrollState.value + delta.toInt()).coerceIn(0, scrollState.maxValue)
                        )
                    }
                }
            }
        }

        rule.onNodeWithText(chosen).performClick()
        rule.onNodeWithText(chosen).assertIsSelected()
        rule.onNodeWithText(other).assertIsNotSelected()

        rule.onNodeWithText("Prüfen").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Jetzt sitzt er").assertIsDisplayed()
        rule.onNodeWithText("Weiter").assertIsDisplayed()
        // Die Auszeichnung überlebt das Prüfen.
        rule.onNodeWithText(chosen).assertIsSelected()

        val viewport = rule.onNodeWithTag("viewport").getBoundsInRoot()
        val marked = rule.onNodeWithText(chosen).getBoundsInRoot()
        assertTrue(
            "Die gewählte Zeile beginnt über dem sichtbaren Bereich (${marked.top} < ${viewport.top})",
            marked.top >= viewport.top
        )
        assertTrue(
            "Die gewählte Zeile endet unter dem sichtbaren Bereich (${marked.bottom} > ${viewport.bottom})",
            marked.bottom <= viewport.bottom
        )

        println("TALKBACK-GRUNDLAGE Verständnis gewählt: ${rule.onNodeWithText(chosen).fetchSemanticsNode().config}")
        println("TALKBACK-GRUNDLAGE Verständnis nicht gewählt: ${rule.onNodeWithText(other).fetchSemanticsNode().config}")
    }
}
