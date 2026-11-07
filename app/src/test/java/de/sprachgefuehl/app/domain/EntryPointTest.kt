package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryPointTest {

    private val content = TestContent.bundle

    @Test
    fun `a higher placement starts on a higher level`() {
        val low = EntryPoint.firstLesson(content, Progress(onboarded = true, placementLevel = 0))!!
        val high = EntryPoint.firstLesson(content, Progress(onboarded = true, placementLevel = 2))!!
        assertEquals(0, EntryPoint.rankOf(low))
        assertTrue("Start bei ${high.level}", EntryPoint.rankOf(high) >= 2)
        assertNotEquals(low.id, high.id)
    }

    @Test
    fun `the middle placement skips the first easy lesson`() {
        val middle = EntryPoint.firstLesson(content, Progress(onboarded = true, placementLevel = 1))!!
        assertTrue("Start bei ${middle.level}", EntryPoint.rankOf(middle) >= 1)
    }

    @Test
    fun `the path from the onboarding wins inside the level`() {
        val lesson = EntryPoint.firstLesson(
            content,
            Progress(onboarded = true, placementLevel = 1, profile = Profile(reasonId = "reisen"))
        )!!
        val situation = content.situations.first { it.id == lesson.situationId }
        assertEquals("unterwegs", situation.pathId)
    }

    @Test
    fun `a started path is never skipped`() {
        val first = SessionBuilder.nextLesson(content, Progress())!!
        val after = EntryPoint.firstLesson(
            content,
            Progress(onboarded = true, placementLevel = 2, completedLessons = setOf("unknown"))
        )!!
        assertEquals(first.id, after.id)
        val resumed = EntryPoint.firstLesson(
            content,
            Progress(onboarded = true, placementLevel = 2, lessonProgress = mapOf("unknown" to 3))
        )!!
        assertEquals(first.id, resumed.id)
    }

    @Test
    fun `nothing is left when everything is done`() {
        val all = content.lessons.map { it.id }.toSet()
        assertNull(EntryPoint.firstLesson(content, Progress(onboarded = true, completedLessons = all)))
    }
}
