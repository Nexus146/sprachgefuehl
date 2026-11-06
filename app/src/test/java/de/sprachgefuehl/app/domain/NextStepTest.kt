package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.CheckupState
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The home screen shows exactly one recommendation. These tests hold the reasoning down: which
 * rule wins, and that every step carries a reason the learner can read.
 */
class NextStepTest {

    private val content = TestContent.bundle
    private val today = LocalDate.of(2026, 11, 2).toEpochDay()
    private val now = today * Srs.DAY_MS

    private fun day(minutes: Int, goal: Int = 10, reason: String = ""): Progress = Progress(
        onboarded = true,
        sessions = listOf(SessionLog(day = today, minutes = minutes, speakingTasks = 2, newChunks = 3)),
        profile = Profile(reasonId = reason, dailyMinutes = goal)
    )

    private fun plan(progress: Progress): NextStep =
        requireNotNull(NextPlanner.plan(content, progress, now)) { "keine Empfehlung" }

    @Test
    fun `an open standort check outranks everything else`() {
        val progress = day(minutes = 12).copy(checkup = CheckupState(startedAt = 1L, blockIndex = 2))
        val step = plan(progress)
        assertEquals(NextKind.CHECKUP, step.kind)
        assertTrue("Teil fehlt: ${step.detail}", step.detail.contains("3"))
        assertTrue(step.reason.isNotBlank())
    }

    @Test
    fun `a reached goal stops the nudging but keeps both doors open`() {
        val step = plan(day(minutes = 12))
        assertEquals(NextKind.DONE, step.kind)
        assertEquals(NextTarget.PRACTICE, step.primary.target)
        assertNotNull("Der Weg zurück zur Lektion fehlt", step.alternative)
        assertTrue(step.detail.contains("12"))
    }

    @Test
    fun `little time left is a short round, not a lesson`() {
        val step = plan(day(minutes = 8))
        assertEquals(NextKind.PRACTICE, step.kind)
        assertEquals(NextTarget.PRACTICE, step.primary.target)
        assertTrue("Begründung fehlt: ${step.reason}", step.reason.contains("Platz"))
    }

    @Test
    fun `little time left with due sentences becomes a review`() {
        val dueIds = content.chunks.take(2).map { it.id }
        val progress = day(minutes = 8).copy(
            chunkMemory = dueIds.associateWith { Memory(due = 1L, reps = 1) }
        )
        val step = plan(progress)
        assertEquals(NextKind.REVIEW, step.kind)
        assertEquals(NextTarget.REVIEW, step.primary.target)
    }

    @Test
    fun `a half-finished lesson wins over a new one and keeps the place`() {
        val lesson = SessionBuilder.orderedLessons(content).first { it.steps.size > 2 }
        val progress = Progress(onboarded = true, lessonProgress = mapOf(lesson.id to 2))
        val step = plan(progress)
        assertEquals(NextKind.CONTINUE, step.kind)
        assertEquals(lesson.id, step.lessonId)
        assertEquals("Weitermachen", step.primary.label)
        assertTrue("Schritt fehlt: ${step.reason}", step.reason.contains("Schritt 3"))
    }

    @Test
    fun `a fresh day follows the reason from the profile`() {
        val step = plan(Progress(onboarded = true, profile = Profile(reasonId = "reisen")))
        assertEquals(NextKind.NEW, step.kind)
        val situation = content.situations.first { it.id == step.situationId }
        assertEquals("unterwegs", situation.pathId)
        assertTrue("Begründung fehlt: ${step.reason}", step.reason.contains("Reisen"))
    }

    @Test
    fun `a real backlog outweighs the next story`() {
        val memories = content.chunks.take(9).associate { it.id to Memory(due = 1L, reps = 1) }
        val step = plan(Progress(onboarded = true, chunkMemory = memories))
        assertEquals(NextKind.REVIEW, step.kind)
        assertNotNull("Der Weg zur Lektion fehlt", step.alternative)
    }

    @Test
    fun `a rest day says so instead of demanding something`() {
        val profile = Profile(dailyMinutes = 10, weekdays = setOf(3))
        val step = plan(Progress(onboarded = true, profile = profile))
        val weekday = DailyPlan.weekday(today)
        assertTrue("Testtag muss ein anderer sein als der Lerntag", weekday != 3)
        assertEquals("Heute ist Pausentag", step.label)
    }

    @Test
    fun `a finished course falls back to repetition`() {
        val progress = Progress(onboarded = true, completedLessons = content.lessons.map { it.id }.toSet())
        val step = plan(progress)
        assertEquals(NextTarget.PRACTICE, step.primary.target)
        assertEquals("Sätze warm halten", step.title)
    }

    @Test
    fun `every recommendation carries a reason and a reachable action`() {
        val cases = listOf(
            day(minutes = 0),
            day(minutes = 8),
            day(minutes = 12),
            Progress(onboarded = true, checkup = CheckupState(startedAt = 1L)),
            Progress(onboarded = true, profile = Profile(reasonId = "kopf"))
        )
        cases.forEach { progress ->
            val step = plan(progress)
            assertTrue("ohne Begründung: ${step.title}", step.reason.isNotBlank())
            assertTrue("ohne Handlung: ${step.title}", step.primary.label.isNotBlank())
            if (step.primary.target == NextTarget.LESSON) {
                assertNotNull("Lektion ohne Kennung: ${step.title}", step.primary.lessonId)
            }
        }
    }
}
