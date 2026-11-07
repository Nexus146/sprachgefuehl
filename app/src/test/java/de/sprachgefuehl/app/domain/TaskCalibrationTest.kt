package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.TaskStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class TaskCalibrationTest {

    private val content = TestContent.bundle

    @Test
    fun `a type that almost always works moves up a level`() {
        val stats = mapOf("GapFill" to TaskStat(attempts = 30, correct = 29))
        assertEquals(2, LessonMixer.taskLevel("GapFill", stats))
    }

    @Test
    fun `a type that often fails moves down a level`() {
        val stats = mapOf("BuildSentence" to TaskStat(attempts = 30, correct = 6))
        assertEquals(0, LessonMixer.taskLevel("BuildSentence", stats))
    }

    @Test
    fun `a handful of answers is not enough to calibrate`() {
        val stats = mapOf("GapFill" to TaskStat(attempts = 5, correct = 5))
        assertEquals(1, LessonMixer.taskLevel("GapFill", stats))
    }

    @Test
    fun `the task type is counted with every checked answer`() {
        val file = Files.createTempFile("sprachgefuehl-stats", ".json").toFile()
        try {
            val repo = LearningRepository(
                content, ProgressStore(file), clock = { 20_000L * 86_400_000L }
            )
            repo.recordAnswer(
                chunkId = content.chunks.first().id,
                grade = 3,
                usedHint = false,
                skills = emptyList(),
                cardId = "",
                lessonId = "l1",
                lessonTitle = "Test",
                target = "x",
                heard = "x",
                mistakeSkill = "",
                trackMistake = false,
                checkSkill = null,
                checkCorrect = true,
                level = 1,
                taskType = "GapFill"
            )
            assertEquals(1, repo.progress.value.taskStats["GapFill"]?.attempts)
            assertEquals(1, repo.progress.value.taskStats["GapFill"]?.correct)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a comprehension mistake comes back with its pattern`() {
        val lesson = content.lessons.first { candidate ->
            candidate.chunkIds.isNotEmpty() &&
                candidate.steps.any { it.type == "ComprehensionDeep" && it.chunkId.isNotBlank() }
        }
        val step = lesson.steps.first { it.type == "ComprehensionDeep" }
        assertTrue("Der Satzbezug fehlt", step.chunkId in lesson.chunkIds)
        val file = Files.createTempFile("sprachgefuehl-comprehension", ".json").toFile()
        try {
            val repo = LearningRepository(
                content, ProgressStore(file), clock = { 20_000L * 86_400_000L }
            )
            repo.recordAnswer(
                chunkId = step.chunkId,
                grade = 1,
                usedHint = false,
                skills = listOf(Skill.HOEREN),
                cardId = "",
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                target = content.chunks.first { it.id == step.chunkId }.en,
                heard = "Kalt und regnerisch",
                mistakeSkill = Skill.HOEREN.id,
                trackMistake = true,
                checkSkill = Skill.HOEREN,
                checkCorrect = false,
                level = 1,
                taskType = step.type
            )
            val mistake = repo.progress.value.mistakes.single()
            assertEquals(step.chunkId, mistake.chunkId)
            assertTrue("Kein Muster am Fehler", mistake.patternId.isNotBlank())
            assertEquals(1, repo.progress.value.taskStats["ComprehensionDeep"]?.attempts)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `solving the transfer clears the original mistake`() {
        val file = Files.createTempFile("sprachgefuehl-resolve", ".json").toFile()
        try {
            val repo = LearningRepository(
                content, ProgressStore(file), clock = { 20_000L * 86_400_000L }
            )
            val chunk = content.chunks.first { it.patternId.isNotBlank() }
            repo.recordAnswer(
                chunkId = chunk.id,
                grade = 1,
                usedHint = false,
                skills = listOf(Skill.HOEREN),
                cardId = "",
                lessonId = "l1",
                lessonTitle = "Test",
                target = chunk.en,
                heard = "falsch",
                mistakeSkill = Skill.HOEREN.id,
                trackMistake = true,
                checkSkill = Skill.HOEREN,
                checkCorrect = false,
                level = 1,
                taskType = "ComprehensionDeep"
            )
            assertTrue(repo.progress.value.mistakes.any { it.chunkId == chunk.id })
            repo.resolveMistake(chunk.id)
            assertTrue(repo.progress.value.mistakes.none { it.chunkId == chunk.id })
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a correct answer does not count as an error`() {
        val file = Files.createTempFile("sprachgefuehl-count", ".json").toFile()
        try {
            val repo = LearningRepository(
                content, ProgressStore(file), clock = { 20_000L * 86_400_000L }
            )
            val card = content.errorCards.first()
            repo.recordAnswer(
                chunkId = content.chunks.first().id,
                grade = 3,
                usedHint = false,
                skills = emptyList(),
                cardId = card.id,
                lessonId = "l1",
                lessonTitle = "Test",
                target = "Should I take anything?",
                heard = "Should I take anything?",
                mistakeSkill = "",
                trackMistake = true,
                checkSkill = null,
                checkCorrect = true,
                level = 1,
                taskType = "SpeakFree"
            )
            assertEquals(0, repo.progress.value.errorCounts[card.id] ?: 0)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a wrong answer with a card still counts`() {
        val file = Files.createTempFile("sprachgefuehl-count-wrong", ".json").toFile()
        try {
            val repo = LearningRepository(
                content, ProgressStore(file), clock = { 20_000L * 86_400_000L }
            )
            val card = content.errorCards.first()
            repo.recordAnswer(
                chunkId = content.chunks.first().id,
                grade = 1,
                usedHint = false,
                skills = emptyList(),
                cardId = card.id,
                lessonId = "l1",
                lessonTitle = "Test",
                target = "Should I take anything?",
                heard = "I take anything?",
                mistakeSkill = "",
                trackMistake = true,
                checkSkill = null,
                checkCorrect = false,
                level = 1,
                taskType = "SpeakFree"
            )
            assertEquals(1, repo.progress.value.errorCounts[card.id])
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a mistake brings the same pattern in a new sentence`() {
        val lesson = content.lessons.first { it.chunkIds.size >= 2 }
        val chunk = content.chunks.first { candidate ->
            candidate.patternId.isNotBlank() &&
                candidate.id !in lesson.chunkIds &&
                content.lessons.any { candidate.id.startsWith(it.id + "_") } &&
                content.chunks.count {
                    it.patternId == candidate.patternId && it.id !in lesson.chunkIds
                } >= 2
        }
        val progress = Progress(
            seed = 11,
            selfRating = 2,
            mistakes = listOf(
                Mistake(
                    chunkId = chunk.id,
                    lessonId = "lesson-x",
                    target = chunk.en,
                    heard = "falsch",
                    day = 22_000,
                    skill = "grammatik",
                    patternId = chunk.patternId
                )
            )
        )
        val step = LessonMixer.steps(lesson, content, progress, day = 22_000)
            .firstOrNull { it.id.endsWith("_mix_pattern") }
        assertNotNull("Kein Muster-Transfer", step)
        assertTrue("Alter Satz statt neuer Kontext", step!!.chunkId != chunk.id)
        assertEquals(
            chunk.patternId,
            content.chunks.first { it.id == step.chunkId }.patternId
        )
        assertTrue(step.originNote.isNotBlank())
        assertEquals(chunk.id, step.resolvesMistake)
    }
}
