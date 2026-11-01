package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PathProgressTest {

    private val content = TestContent.bundle

    private fun lessonsOf(situationId: String) = content.lessons.filter { it.situationId == situationId }

    @Test
    fun `a new situation counts zero lessons`() {
        val status = PathProgress.of(content, Progress(), "arzt")!!
        assertEquals(0, status.done)
        assertEquals(lessonsOf("arzt").size, status.total)
        assertEquals(SituationState.NEW, status.state)
        assertEquals("Neu", status.label)
    }

    @Test
    fun `finishing one lesson moves the situation to started`() {
        val first = lessonsOf("arzt").first()
        val progress = Progress(completedLessons = setOf(first.id))
        val status = PathProgress.of(content, progress, "arzt")!!
        assertEquals(SituationState.STARTED, status.state)
        assertEquals(1, status.done)
        assertEquals(lessonsOf("arzt").drop(1).firstOrNull()?.id, status.nextLesson?.id)
    }

    @Test
    fun `finishing every lesson makes the situation done`() {
        val ids = lessonsOf("arzt").map { it.id }.toSet()
        val status = PathProgress.of(content, Progress(completedLessons = ids), "arzt")!!
        assertEquals(SituationState.DONE, status.state)
        assertNull(status.nextLesson)
    }

    @Test
    fun `paths report their own counter`() {
        val paths = PathProgress.paths(content, Progress())
        assertTrue(paths.isNotEmpty())
        paths.forEach { path ->
            assertEquals(path.situations.sumOf { it.total }, path.total)
            assertEquals(0, path.done)
            assertTrue(path.total > 0)
        }
    }

    @Test
    fun `without a profile the first open situation comes first`() {
        val next = PathProgress.nextUp(content, Progress())!!
        val firstPath = content.paths.minByOrNull { it.order }!!
        assertEquals(firstPath.id, next.situation.pathId)
    }

    @Test
    fun `a started situation wins over the profile preference`() {
        val started = lessonsOf("arzt").first()
        val progress = Progress(
            completedLessons = setOf(started.id),
            profile = Profile(reasonId = "familie")
        )
        assertEquals("arzt", PathProgress.nextUp(content, progress)!!.situation.id)
    }

    @Test
    fun `the reason moves a matching path forward for new situations`() {
        val next = PathProgress.nextUp(content, Progress(profile = Profile(reasonId = "familie")))!!
        assertEquals("leute", next.situation.pathId)
    }

    @Test
    fun `completed situations are skipped`() {
        val arztIds = lessonsOf("arzt").map { it.id }.toSet()
        val next = PathProgress.nextUp(content, Progress(completedLessons = arztIds))!!
        assertTrue(next.situation.id != "arzt")
    }

    @Test
    fun `the next situation inside a path is named`() {
        val following = PathProgress.nextInPath(content, Progress(), "arzt")
        assertNotNull(following)
        assertEquals("gesundheit", following!!.situation.pathId)
    }

    @Test
    fun `resume finds the half finished lesson with its step`() {
        val lesson = lessonsOf("arzt").first()
        val progress = Progress(lessonProgress = mapOf(lesson.id to 3))
        val resume = PathProgress.resumeLesson(content, progress)
        assertEquals(lesson.id, resume?.first?.id)
        assertEquals(3, resume?.second)
    }

    @Test
    fun `a fresh lesson is not offered as resume`() {
        assertNull(PathProgress.resumeLesson(content, Progress()))
    }

    @Test
    fun `a finished lesson is not offered as resume`() {
        val lesson = lessonsOf("arzt").first()
        val progress = Progress(
            lessonProgress = mapOf(lesson.id to lesson.steps.size),
            completedLessons = setOf(lesson.id)
        )
        assertNull(PathProgress.resumeLesson(content, progress))
    }
}
