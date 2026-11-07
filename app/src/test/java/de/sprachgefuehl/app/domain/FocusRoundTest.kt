package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusRoundTest {

    private val content = TestContent.bundle
    private val card = content.errorCards.first { it.id == "err_since_for" || it.id == "err_be_agree" }

    private fun round() = FocusRound.lesson(card, content, Progress(seed = 12), day = 22_000)

    @Test
    fun `the round starts at the card and stays short`() {
        val lesson = round()
        assertEquals(FocusRound.lessonId(card.id), lesson.id)
        assertTrue("Titel nennt den Fehler", lesson.title.contains(card.title))
        assertEquals("Noticing", lesson.steps.first().type)
        assertEquals("Dein Fehler", lesson.steps.first().title)
        assertEquals(card.correctEn, lesson.steps.first().promptEn)
        assertTrue("zu kurz: ${lesson.steps.size}", lesson.steps.size >= FocusRound.MIN_TASKS)
        assertTrue("zu lang: ${lesson.steps.size}", lesson.steps.size <= 12)
        assertEquals("SelfCheck", lesson.steps.last().type)
        assertEquals("Geschafft", lesson.steps.last().title)
    }

    @Test
    fun `every step has an id, a known type and an anchor`() {
        val steps = round().steps
        assertEquals(steps.size, steps.map { it.id }.distinct().size)
        steps.forEach { step ->
            assertTrue("Unbekannter Typ ${step.type}", TaskRegistry.spec(step.type) != null)
        }
        assertTrue("Kein Sprechen", steps.any { TaskRegistry.isSpeaking(it.type) })
        assertTrue("Kein Satzbau", steps.any { it.type == "BuildSentence" })
        assertTrue("Keine Optionen geprüft", steps.filter { it.options.isNotEmpty() }.all { s -> s.options.count { it.correct } == 1 })
    }

    @Test
    fun `the round is stable for the same day and changes with it`() {
        val first = FocusRound.lesson(card, content, Progress(seed = 12), day = 22_000)
        val second = FocusRound.lesson(card, content, Progress(seed = 12), day = 22_000)
        assertEquals(first.steps.map { it.id }, second.steps.map { it.id })
        val variants = (22_000L..22_005L).map { day ->
            FocusRound.lesson(card, content, Progress(seed = 12), day).steps.map { it.target }
        }.toSet()
        assertTrue("Die Runde wechselt nie", variants.size > 1)
    }

    @Test
    fun `every card can build a round`() {
        content.errorCards.forEach { errorCard ->
            val lesson = FocusRound.lesson(errorCard, content, Progress(seed = 1), day = 22_000)
            assertTrue("${errorCard.id}: ${lesson.steps.size} Schritte", lesson.steps.size >= FocusRound.MIN_TASKS)
            assertTrue("${errorCard.id}: kein Modell", lesson.steps.all { it.type == "SelfCheck" || it.promptEn.isNotBlank() || it.target.isNotBlank() })
        }
    }
}
