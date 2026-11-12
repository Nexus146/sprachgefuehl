package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.domain.PathProgress
import de.sprachgefuehl.app.domain.Srs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executors

class LearningEngineReliabilityTest {

    private val content = TestContent.bundle
    private val today = 20_000L
    private val now = today * Srs.DAY_MS

    private fun newFile(): File = Files.createTempFile("sprachgefuehl-engine", ".json").toFile().also { it.delete() }

    private fun repository(file: File, clock: () -> Long = { now }, zone: ZoneId = ZoneId.of("UTC")) =
        LearningRepository(content, ProgressStore(file), clock, zone)

    @Test
    fun `starting a lesson stores the exact round and resumes it after a restart`() {
        val file = newFile()
        try {
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            val first = repository(file).startLesson(lesson.id)
            assertNotNull(first)
            assertTrue(first!!.steps.size in 13..17)
            repository(file).recordStep(lesson.id, 0)

            val restarted = repository(file).startLesson(lesson.id)
            assertEquals(first.steps.map { it.id }, restarted!!.steps.map { it.id })
            assertEquals(1, restarted.index)
            assertEquals(today, restarted.startedDay)
            assertEquals(
                1,
                PathProgress.resumeLesson(content, repository(file).progress.value, today)!!.second
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a new day does not replace the running round`() {
        val file = newFile()
        var clock = now
        try {
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            val repo = repository(file, clock = { clock })
            val first = repo.startLesson(lesson.id)!!
            clock = (today + 1) * Srs.DAY_MS

            val resumed = repo.startLesson(lesson.id)!!
            assertEquals(first.steps.map { it.id }, resumed.steps.map { it.id })
            assertEquals(first.startedDay, resumed.startedDay)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `opening another lesson replaces the active one`() {
        val file = newFile()
        try {
            val first = content.lessons[0]
            val second = content.lessons[1]
            val repo = repository(file)
            repo.startLesson(first.id)
            val replaced = repo.startLesson(second.id)!!
            assertEquals(second.id, replaced.lessonId)
            assertEquals(second.id, repo.progress.value.lessonSession?.lessonId)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `completion clears the session and cannot complete twice`() {
        val file = newFile()
        try {
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            val repo = repository(file)
            val session = repo.startLesson(lesson.id)!!
            repo.recordStep(lesson.id, session.steps.lastIndex)

            assertTrue(repo.completeLesson(lesson, speakingTasks = 0))
            assertNull(repo.progress.value.lessonSession)
            assertFalse(repo.completeLesson(lesson, speakingTasks = 0))
            assertNull(PathProgress.resumeLesson(content, repo.progress.value, today))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `old progress without a session still resumes at its saved index`() {
        val lesson = content.lessons.first { it.chunkIds.size >= 2 }
        val progress = Progress(onboarded = true, lessonProgress = mapOf(lesson.id to 2))
        val resume = PathProgress.resumeLesson(content, progress, today)
        assertEquals(lesson.id, resume?.first?.id)
        assertEquals(2, resume?.second)
    }

    @Test
    fun `the local calendar day changes at midnight and not at UTC midnight`() {
        val berlin = ZoneId.of("Europe/Berlin")
        assertEquals(
            LocalDate.of(2026, 7, 1).toEpochDay(),
            repository(newFile(), { Instant.parse("2026-07-01T21:59:00Z").toEpochMilli() }, berlin).today()
        )
        assertEquals(
            LocalDate.of(2026, 7, 2).toEpochDay(),
            repository(newFile(), { Instant.parse("2026-07-01T22:01:00Z").toEpochMilli() }, berlin).today()
        )
        assertEquals(
            LocalDate.of(2026, 7, 1).toEpochDay(),
            repository(newFile(), { Instant.parse("2026-07-01T22:01:00Z").toEpochMilli() }, ZoneId.of("UTC")).today()
        )
    }

    @Test
    fun `daylight saving time keeps the local date stable`() {
        val berlin = ZoneId.of("Europe/Berlin")
        val before = repository(
            newFile(),
            { Instant.parse("2026-03-28T23:30:00Z").toEpochMilli() },
            berlin
        )
        val after = repository(
            newFile(),
            { Instant.parse("2026-03-29T01:30:00Z").toEpochMilli() },
            berlin
        )
        assertEquals(LocalDate.of(2026, 3, 29).toEpochDay(), before.today())
        assertEquals(LocalDate.of(2026, 3, 29).toEpochDay(), after.today())
    }

    @Test
    fun `parallel updates are serialized and not lost`() {
        val file = newFile()
        val pool = Executors.newFixedThreadPool(4)
        try {
            val repo = repository(file)
            val tasks = (1..100).map { pool.submit { repo.recordLearningTime(1) } }
            tasks.forEach { it.get() }
            assertEquals(100, repo.progress.value.studySeconds)
            assertEquals(100, repo.progress.value.studiedSecondsOn(today))
        } finally {
            pool.shutdown()
            file.delete()
        }
    }

    @Test
    fun `a failed write is reported instead of looking successful`() {
        val directory = Files.createTempDirectory("sprachgefuehl-failed-write").toFile()
        try {
            val store = ProgressStore(directory)
            assertFalse(store.save(Progress()))
            val repo = LearningRepository(content, store, { now }, ZoneId.of("UTC"))
            repo.recordLearningTime(1)
            assertTrue(repo.saveFailed.value)
        } finally {
            directory.deleteRecursively()
        }
    }
}
