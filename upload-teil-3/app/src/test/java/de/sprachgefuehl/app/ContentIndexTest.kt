package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.LessonSession
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.domain.LearningOverview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ContentIndexTest {

    private val content = TestContent.bundle

    private fun newFile(): File = Files.createTempFile("sprachgefuehl-content", ".json").toFile().also { it.delete() }

    @Test
    fun `old content ids are removed on load`() {
        val file = newFile()
        try {
            val known = content.chunks.first().id
            val lesson = content.lessons.first()
            ProgressStore(file).save(
                Progress(
                    onboarded = true,
                    contentVersion = 0,
                    chunkMemory = mapOf("removed_chunk" to Memory(), known to Memory()),
                    errorCounts = mapOf("removed_card" to 2),
                    lessonProgress = mapOf("removed_lesson" to 4, lesson.id to 1),
                    lessonSession = LessonSession(lessonId = "removed_lesson", steps = lesson.steps)
                )
            )
            val repo = LearningRepository(content, ProgressStore(file), { 0L }, java.time.ZoneId.of("UTC"))
            val migrated = repo.progress.value
            assertEquals(content.version, migrated.contentVersion)
            assertFalse(migrated.chunkMemory.containsKey("removed_chunk"))
            assertTrue(migrated.chunkMemory.containsKey(known))
            assertFalse(migrated.errorCounts.containsKey("removed_card"))
            assertFalse(migrated.lessonProgress.containsKey("removed_lesson"))
            assertNull(migrated.lessonSession)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `importing an old backup runs the same migration`() {
        val file = newFile()
        try {
            val repo = LearningRepository(content, ProgressStore(file), { 0L }, java.time.ZoneId.of("UTC"))
            val old = Progress(
                onboarded = true,
                contentVersion = 0,
                chunkMemory = mapOf("gone" to Memory()),
                completedLessons = setOf("gone_lesson")
            )
            assertTrue(repo.importProgress(ProgressStore(file).encode(old)))
            assertTrue(repo.progress.value.chunkMemory.isEmpty())
            assertTrue(repo.progress.value.completedLessons.isEmpty())
            assertEquals(content.version, repo.progress.value.contentVersion)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `overview and due numbers ignore unknown ids`() {
        val repo = LearningRepository(content, ProgressStore(newFile()), { 0L }, java.time.ZoneId.of("UTC"))
        val progress = Progress(
            onboarded = true,
            chunkMemory = mapOf("not_in_content" to Memory(due = 1L, reps = 1))
        )
        val overview = LearningOverview.of(content, progress, now = 0L, today = 0L)
        assertEquals(0, overview.pool.collected)
        assertEquals(0, overview.pool.due)
        assertEquals(0, repo.dueReviewCount())
    }
}
