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
import de.sprachgefuehl.app.data.Option
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
 * Prüft an der echten Auswahlaufgabe, dass die gewählte Zeile nach dem Prüfen zusammen mit der
 * Rückmeldung im sichtbaren Bereich liegt. Die Markierung selbst (Farbe) prüft der Test nicht –
 * geprüft wird, dass die gewählte Zeile nicht aus dem Bild gescrollt wird.
 */
@RunWith(AndroidJUnit4::class)
class ChoiceRevealTest {

    @get:Rule
    val rule = createComposeRule()

    private val step = Step(
        id = "choice-reveal",
        type = "MeaningChoice",
        title = "Neuer Zusammenhang",
        promptDe = "Welcher Satz passt?",
        options = listOf(
            Option(text = "Stell die Kisten in den Flur."),
            Option(text = "Leider kann ich nicht."),
            Option(text = "Ich bringe einen Salat mit.", correct = true)
        )
    )

    @Test
    fun markierteAntwortBleibtNebenDerRueckmeldungSichtbar() {
        val chosen = "Ich bringe einen Salat mit."
        lateinit var scrollState: ScrollState

        rule.setContent {
            SprachgefuehlTheme {
                val speech = rememberSpeechController()
                scrollState = rememberScrollState()
                var solved by remember { mutableStateOf(false) }
                var viewportTop by remember { mutableFloatStateOf(0f) }
                var viewportBottom by remember { mutableFloatStateOf(0f) }
                var feedbackBottom by remember { mutableFloatStateOf(0f) }
                var feedbackPlaced by remember { mutableStateOf(false) }

                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .weight(1f)
                            .testTag("viewport")
                            .onGloballyPositioned { coordinates ->
                                viewportTop = coordinates.positionInRoot().y
                                viewportBottom = viewportTop + coordinates.size.height
                            }
                            .verticalScroll(scrollState)
                    ) {
                        // Steht für den erklärenden Text oberhalb der Aufgabe.
                        Spacer(Modifier.height(320.dp))
                        ChoiceStepForTest(
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
                                    detail = "Das Muster kennst du schon.",
                                    model = "I'll bring a salad.",
                                    onSpeakModel = {}
                                )
                            }
                        }
                        // Luft unter der Rückmeldung: „ans Ende scrollen“ würde zu weit gehen.
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
                    // Auswahlaufgaben haben keine Antwortfläche, die bleiben müsste.
                    val delta = revealDelta(
                        feedbackBottom = feedbackBottom,
                        viewportBottom = viewportBottom,
                        answerTop = 0f,
                        viewportTop = viewportTop,
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
        // Die Auswahl ist auch ohne Farbe eindeutig – so lesen Screenreader sie vor.
        rule.onNodeWithText(chosen).assertIsSelected()
        rule.onNodeWithText("Leider kann ich nicht.").assertIsNotSelected()
        rule.onNodeWithText("Prüfen").performClick()
        rule.waitForIdle()
        // Nach dem Prüfen bleibt die gewählte Zeile ausgezeichnet.
        rule.onNodeWithText(chosen).assertIsSelected()

        rule.onNodeWithText("Jetzt sitzt er").assertIsDisplayed()
        rule.onNodeWithText("Weiter").assertIsDisplayed()

        // Grundlage der Screenreader-Ansage: Text, Rolle und Zustand des Knotens.
        println("TALKBACK-GRUNDLAGE gewählt: ${rule.onNodeWithText(chosen).fetchSemanticsNode().config}")
        println(
            "TALKBACK-GRUNDLAGE nicht gewählt: " +
                rule.onNodeWithText("Leider kann ich nicht.").fetchSemanticsNode().config
        )

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
        assertTrue("Es wurde gar nicht nachgerückt", scrollState.value > 0)
        assertTrue(
            "Es wurde ans Ende gescrollt (${scrollState.value} von ${scrollState.maxValue})",
            scrollState.value < scrollState.maxValue
        )
    }
}
