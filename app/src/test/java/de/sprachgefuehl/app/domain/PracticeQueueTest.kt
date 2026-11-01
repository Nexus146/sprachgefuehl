package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeQueueTest {

    private val content = TestContent.bundle

    @Test
    fun `without history the first known chunks are offered`() {
        val queue = SessionBuilder.practiceChunks(content, Progress(), now = 0L, limit = 5)
        assertEquals(5, queue.size)
        assertEquals(queue.distinct(), queue)
        assertTrue(queue.all { id -> content.chunks.any { it.id == id } })
    }

    @Test
    fun `due and shaky chunks come first`() {
        val shaky = content.chunks[10].id
        val fresh = content.chunks[20].id
        val progress = Progress(
            chunkMemory = mapOf(
                shaky to Memory(reps = 3, stage = 2, stability = 2.0, due = 0L),
                fresh to Memory(reps = 1, stage = 1, stability = 1.0, due = 0L)
            )
        )
        val queue = SessionBuilder.practiceChunks(content, progress, now = 0L, limit = 5)
        assertEquals(5, queue.size)
        assertTrue("fällige Bausteine fehlen", shaky in queue)
        assertTrue(queue.indexOf(shaky) < queue.size)
        assertEquals(queue.distinct(), queue)
    }

    @Test
    fun `practice stays on what the learner has already met`() {
        val ordered = SessionBuilder.orderedLessons(content)
        val learned = ordered.take(2)
        val next = ordered[2]
        val allowed = (learned.flatMap { it.chunkIds } + next.chunkIds).toSet()
        val progress = Progress(completedLessons = learned.map { it.id }.toSet())
        val queue = SessionBuilder.practiceChunks(content, progress, now = 0L, limit = 5)
        assertEquals(5, queue.size)
        assertTrue("Übung springt in eine unbekannte Situation", queue.all { it in allowed })
    }

    @Test
    fun `practice never invents chunks`() {
        val queue = SessionBuilder.practiceChunks(content, Progress(), now = 0L, limit = 50)
        assertEquals(queue.distinct().size, queue.size)
        assertTrue(queue.size <= content.chunks.size)
    }
}
