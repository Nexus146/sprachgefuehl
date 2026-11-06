package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GardenAndErrorsTest {

    private val content = TestContent.bundle

    private fun newFile(): File = Files.createTempFile("sprachgefuehl-garden", ".json").toFile().also { it.delete() }

    @Test
    fun `several rounds on one day grow the garden as rounds`() {
        var progress = Garden.recordSession(Progress(), 1, 5, 2, 1)
        assertEquals(1, Garden.stage(progress))
        progress = Garden.recordSession(progress, 1, 5, 2, 1)
        progress = Garden.recordSession(progress, 1, 5, 2, 1)
        assertEquals(1, progress.sessions.size)
        assertEquals(3, progress.gardenRounds)
        assertEquals(2, Garden.stage(progress))
    }

    @Test
    fun `a resolved mistake leaves the top errors`() {
        val file = newFile()
        try {
            val repo = LearningRepository(content, ProgressStore(file), { 0L }, java.time.ZoneId.of("UTC"))
            val card = content.errorCards.first()
            val chunk = content.chunks.first()
            repo.recordError(card.id)
            repo.recordMistake(
                chunkId = chunk.id,
                lessonId = content.lessons.first().id,
                lessonTitle = "Test",
                target = chunk.en,
                heard = "falsch",
                skill = "grammatik",
                cardId = card.id
            )
            assertEquals(1, repo.progress.value.errorCounts[card.id])
            assertTrue(SessionBuilder.remediation(content, repo.progress.value).any { it.id == card.id })

            repo.resolveMistake(chunk.id)
            assertTrue(repo.progress.value.mistakes.isEmpty())
            assertFalse(repo.progress.value.errorCounts.containsKey(card.id))
            assertTrue(SessionBuilder.remediation(content, repo.progress.value).none { it.id == card.id })
        } finally {
            file.delete()
        }
    }

    @Test
    fun `the diagnosis trace is local and names the chosen steps`() {
        val file = newFile()
        try {
            val repo = LearningRepository(content, ProgressStore(file), { 0L }, java.time.ZoneId.of("UTC"))
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            repo.startLesson(lesson.id)
            val trace = repo.diagnosticTrace(lesson.id)
            assertTrue(trace != null)
            assertTrue(trace!!.contains("contentVersion=${content.version}"))
            assertTrue(trace.contains("lesson=${lesson.id}"))
            assertTrue(trace.contains("seed="))
            assertTrue(trace.contains("SpeakRepeat") || trace.contains("GapFill") || trace.contains("ChunkIntro"))
            assertNull(repo.diagnosticTrace("unknown_lesson"))
        } finally {
            file.delete()
        }
    }
}
