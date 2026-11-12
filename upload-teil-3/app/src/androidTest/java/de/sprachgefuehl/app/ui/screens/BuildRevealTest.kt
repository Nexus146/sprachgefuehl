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
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.theme.SprachgefuehlTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prüft an der echten Wortbau-Aufgabe, dass der zusammengesetzte Satz und die Rückmeldung nach
 * dem Prüfen gleichzeitig im sichtbaren Bereich liegen – und dass dafür nicht ans Ende gescrollt
 * wird (was den Satz oben herausschieben würde).
 */
@RunWith(AndroidJUnit4::class)
class BuildRevealTest {

    @get:Rule
    val rule = createComposeRule()

    private val step = Step(
        id = "build-reveal",
        type = "BuildSentence",
        title = "Wortstellung",
        promptDe = "Bau den Satz aus den Kacheln.",
        target = "You should rest.",
        words = listOf("rest.", "You", "should")
    )

    @Test
    fun satzUndRueckmeldungBleibenGemeinsamSichtbar() {
        lateinit var scrollState: ScrollState

        rule.setContent {
            SprachgefuehlTheme {
                scrollState = rememberScrollState()
                var solved by remember { mutableStateOf(false) }
                var viewportTop by remember { mutableFloatStateOf(0f) }
                var viewportBottom by remember { mutableFloatStateOf(0f) }
                var feedbackBottom by remember { mutableFloatStateOf(0f) }
                var feedbackPlaced by remember { mutableStateOf(false) }
                var answerTop by remember { mutableFloatStateOf(0f) }
                var answerPlaced by remember { mutableStateOf(false) }

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
                        BuildStepForTest(
                            step = step,
                            enabled = !solved,
                            onSolved = { solved = true },
                            onAnswerTop = {
                                answerTop = it
                                answerPlaced = true
                            }
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
                                    model = "You should rest.",
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
                    val delta = revealDelta(feedbackBottom, viewportBottom, answerTop, viewportTop, answerPlaced)
                    if (delta > 1f) {
                        scrollState.animateScrollTo(
                            (scrollState.value + delta.toInt()).coerceIn(0, scrollState.maxValue)
                        )
                    }
                }
            }
        }

        rule.onNodeWithText("You").performClick()
        rule.onNodeWithText("should").performClick()
        rule.onNodeWithText("rest.").performClick()
        rule.onNodeWithText("Prüfen").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Jetzt sitzt er").assertIsDisplayed()
        rule.onNodeWithText("Weiter").assertIsDisplayed()

        val viewport = rule.onNodeWithTag("viewport").getBoundsInRoot()
        val answer = rule.onAllNodesWithText("You should rest.").onFirst().getBoundsInRoot()

        assertTrue(
            "Der Satz beginnt über dem sichtbaren Bereich (${answer.top} < ${viewport.top})",
            answer.top >= viewport.top
        )
        assertTrue(
            "Der Satz endet unter dem sichtbaren Bereich (${answer.bottom} > ${viewport.bottom})",
            answer.bottom <= viewport.bottom
        )
        assertTrue(
            "Es wurde gar nicht nachgerückt",
            scrollState.value > 0
        )
        assertTrue(
            "Es wurde ans Ende gescrollt (${scrollState.value} von ${scrollState.maxValue})",
            scrollState.value < scrollState.maxValue
        )
    }

    /**
     * Die Regel selbst: So weit wie nötig, aber nie weiter als bis die eigene Antwort oben
     * anstößt. Ohne Antwortfläche (Auswahlaufgaben) gilt allein die Rückmeldung.
     */
    @Test
    fun nachrueckenIstAufDieEigeneAntwortBegrenzt() {
        // Rückmeldung ragt 1000 unter die Kante, die Antwort hat nur 400 Platz.
        assertEquals(400f, revealDelta(2000f, 1000f, 500f, 100f, answerPlaced = true), 0.01f)
        // Ohne Antwortfläche bleibt es beim vollen Betrag.
        assertEquals(1000f, revealDelta(2000f, 1000f, 0f, 0f, answerPlaced = false), 0.01f)
        // Fehlt nichts, wird nicht gescrollt.
        assertEquals(0f, revealDelta(900f, 1000f, 500f, 100f, answerPlaced = true), 0.01f)
    }
}
