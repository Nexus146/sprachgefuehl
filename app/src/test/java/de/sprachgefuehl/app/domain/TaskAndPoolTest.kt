package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskAndPoolTest {

    private val content = TestContent.bundle

    @Test
    fun `all nine formats and speaking flags live in the registry`() {
        TaskFormats.all.forEach { format ->
            assertEquals(format.skill, TaskRegistry.skillFor(format.type))
            assertTrue(TaskRegistry.labelFor(format.type).isNotBlank())
        }
        assertTrue(TaskRegistry.isSpeaking("SpeakRepeat"))
        assertTrue(TaskRegistry.isSpeaking("TranslateToEnglish"))
        assertTrue(!TaskRegistry.isSpeaking("Dictation"))
        assertTrue(TaskRegistry.needsAnswer("GapFill"))
        assertTrue(!TaskRegistry.needsAnswer("ChunkIntro"))
    }

    @Test
    fun `word order matters for the top grade`() {
        val ordered = Scorer.score("I have a bad headache", "I have a bad headache")
        val reordered = Scorer.score("I have a bad headache", "headache bad a have I")
        assertEquals(3, ordered.grade)
        assertTrue("Vertauschte Wörter waren perfekt", reordered.grade < 3)
    }

    @Test
    fun `every lesson keeps the task invariants across seeds and levels`() {
        content.lessons.filter { it.chunkIds.isNotEmpty() }.forEach { lesson ->
            listOf(1L, 2L).forEach { seed ->
                (0..2).forEach { level ->
                    val steps = LessonMixer.steps(
                        lesson,
                        content,
                        Progress(seed = seed, selfRating = level),
                        day = 22_000L + seed + level
                    )
                    assertTrue(
                        "${lesson.id} seed $seed level $level has ${steps.size} steps",
                        steps.size in LessonMixer.MIN_TASKS..LessonMixer.MAX_TASKS
                    )
                    assertEquals("${lesson.id} duplicate step id", steps.size, steps.map { it.id }.distinct().size)
                    steps.filter { it.chunkId.isNotBlank() }.forEach { step ->
                        assertTrue(
                            "${step.id} references unknown chunk ${step.chunkId}",
                            content.chunks.any { it.id == step.chunkId }
                        )
                    }
                    steps.filter { it.type in setOf("MeaningChoice", "ListenMeaning", "GapFill", "ChooseConnector") }
                        .forEach { step ->
                            assertEquals("${step.id} needs exactly one answer", 1, step.options.count { it.correct })
                            assertEquals(
                                "${step.id} repeats an option",
                                step.options.size,
                                step.options.map { it.text.trim().lowercase() }.distinct().size
                            )
                        }
                }
            }
        }
    }

    @Test
    fun `practice keeps due first and repeats neither within nor across rounds`() {
        val ids = content.chunks.take(30).map { it.id }
        val due = ids.take(3).associateWith { Memory(reps = 1, stage = 2, stability = 1.0, due = 0L) }
        val rest = ids.drop(3).associateWith { Memory(reps = 1, stage = 4, stability = 20.0, due = 9_999_999_999L) }
        val progress = Progress(seed = 7, chunkMemory = due + rest)

        val first = SessionBuilder.practiceChunks(content, progress, now = 1_000L, limit = 5)
        assertEquals(5, first.size)
        assertEquals(first.distinct(), first)
        assertTrue("fällige Sätze fehlen", first.take(3).containsAll(due.keys))

        val second = SessionBuilder.practiceChunks(
            content,
            progress.copy(lastPracticeChunkIds = first),
            now = 2_000L,
            limit = 5
        )
        assertEquals(second.distinct(), second)
        assertTrue("Die nächste Runde wiederholt die letzte", second.any { it !in first })

        val perLesson = second.groupingBy { chunkId ->
            content.lessons.first { chunkId in it.chunkIds }.id
        }.eachCount()
        assertTrue("Mehr als zwei Sätze aus einer Lektion", perLesson.values.all { it <= 2 })
    }

    @Test
    fun `review queue mixes lessons and puts due entries first`() {
        val dueIds = content.chunks.take(2).map { it.id }
        val restIds = content.chunks.drop(2).take(6).map { it.id }
        val progress = Progress(
            seed = 9,
            chunkMemory = dueIds.associateWith { Memory(reps = 1, stage = 2, due = 0L) } +
                restIds.associateWith { Memory(reps = 1, stage = 4, due = 9_999_999_999L) }
        )
        val queue = SessionBuilder.reviewQueue(content, progress, now = 1_000L, limit = 6)
        assertEquals(queue.distinct(), queue)
        assertTrue(queue.take(2).containsAll(dueIds))
    }
}
