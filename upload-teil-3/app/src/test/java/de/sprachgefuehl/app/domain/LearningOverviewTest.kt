package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningOverviewTest {

    private val content = TestContent.bundle
    private val now = Srs.DAY_MS * 20_000L

    @Test
    fun `the pool counts content and nine formats`() {
        val view = LearningOverview.of(content, Progress(seed = 1), now)
        assertEquals(content.chunks.size, view.pool.chunks)
        assertEquals(content.lessons.size, view.pool.lessons)
        assertEquals(content.patterns.size, view.pool.patterns)
        assertEquals(9, view.pool.formats)
        assertEquals(9, TaskFormats.all.size)
    }

    @Test
    fun `the next round shows its real size and format mix`() {
        val view = LearningOverview.of(content, Progress(seed = 1), now)
        assertNotNull(view.next)
        assertTrue(
            "Länge ${view.nextSteps}",
            view.nextSteps in LessonMixer.MIN_TASKS..LessonMixer.MAX_TASKS
        )
        assertTrue(view.nextMix.isNotEmpty())
        assertTrue(view.nextMix.all { it.label.isNotBlank() })
        assertTrue(view.nextMix.sumOf { it.count } > 0)
        assertTrue(view.nextMix.sumOf { it.count } <= view.nextSteps)
    }

    @Test
    fun `the week uses measured time and active days`() {
        val view = LearningOverview.of(
            content,
            Progress(seed = 1, timeByDay = mapOf(20_000L to 320, 19_999L to 60)),
            now
        )
        assertEquals(7, view.week.minutes)
        assertEquals(2, view.week.activeDays)
        assertEquals(0, view.week.previousMinutes)
    }

    @Test
    fun `stable and learning chunks are counted apart`() {
        val ids = content.chunks.take(3).map { it.id }
        val view = LearningOverview.of(
            content,
            Progress(
                seed = 1,
                chunkMemory = mapOf(
                    ids[0] to Memory(stage = 4, reps = 3),
                    ids[1] to Memory(stage = 2, reps = 1),
                    ids[2] to Memory()
                )
            ),
            now
        )
        assertEquals(3, view.pool.collected)
        assertEquals(1, view.pool.stable)
        assertEquals(1, view.pool.learning)
        assertEquals(3, view.pool.due)
    }

    @Test
    fun `mistakes are newest first and limited`() {
        val view = LearningOverview.of(
            content,
            Progress(
                seed = 1,
                mistakes = listOf(
                    Mistake(chunkId = content.chunks[0].id, target = "alt", day = 10),
                    Mistake(chunkId = content.chunks[1].id, target = "neu", day = 11),
                    Mistake(chunkId = content.chunks[2].id, target = "mittig", day = 9)
                )
            ),
            now
        )
        assertEquals(3, view.mistakes.size)
        assertEquals("neu", view.mistakes.first().target)
        assertEquals("alt", view.mistakes[1].target)
    }

    @Test
    fun `onboarding focus reaches the overview`() {
        val view = LearningOverview.of(
            content,
            Progress(seed = 1, profile = Profile(reasonIds = setOf("reisen"))),
            now
        )
        assertEquals(listOf("Hören", "Sprechen"), view.focus)
    }

    @Test
    fun `an empty learner still gets a next step`() {
        val view = LearningOverview.of(content, Progress(), now)
        assertNotNull(view.next)
        assertTrue(view.nextSteps > 0)
        assertEquals(5, view.skills.size)
    }
}
