package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionBuilderTest {

    private val content = TestContent.bundle
    private val now = 1_700_000_000_000L

    @Test
    fun `the first lesson comes from the first path`() {
        val lesson = SessionBuilder.nextLesson(content, Progress())
        assertNotNull(lesson)
        assertEquals("arzt", content.situations.first { it.id == lesson!!.situationId }.id)
    }

    @Test
    fun `completing a lesson moves the course forward`() {
        val first = SessionBuilder.nextLesson(content, Progress())!!
        val next = SessionBuilder.nextLesson(content, Progress(completedLessons = setOf(first.id)))
        assertNotNull(next)
        assertTrue(next!!.id != first.id)
    }

    @Test
    fun `due chunks are picked up and ordered`() {
        val chunk = content.chunks.first()
        val progress = Progress(
            chunkMemory = mapOf(
                chunk.id to Memory(reps = 1, stability = 1.0, due = now - 1000),
                content.chunks[1].id to Memory(reps = 1, stability = 1.0, due = now + 100_000)
            )
        )
        val due = SessionBuilder.dueChunks(content, progress, now)
        assertEquals(listOf(chunk.id), due)
    }

    @Test
    fun `review queue puts shaky cards first`() {
        val a = content.chunks[0].id
        val b = content.chunks[1].id
        val progress = Progress(
            chunkMemory = mapOf(
                a to Memory(reps = 4, stability = 20.0, stage = 4, due = now + 1000),
                b to Memory(reps = 1, stability = 1.0, stage = 1, due = now - 1000)
            )
        )
        val queue = SessionBuilder.reviewQueue(content, progress, now, limit = 2)
        assertEquals(b, queue.first())
    }

    @Test
    fun `remediation sorts error cards by frequency`() {
        val cards = content.errorCards.take(3)
        val progress = Progress(
            errorCounts = mapOf(
                cards[0].id to 1,
                cards[1].id to 5,
                cards[2].id to 3
            )
        )
        val remediation = SessionBuilder.remediation(content, progress, limit = 3)
        assertEquals(cards[1].id, remediation[0].id)
        assertEquals(cards[2].id, remediation[1].id)
    }

    @Test
    fun `daily round contains a lesson and reviews`() {
        val first = content.chunks.first()
        val progress = Progress(
            chunkMemory = mapOf(first.id to Memory(reps = 1, stability = 1.0, due = now - 1))
        )
        val round = SessionBuilder.dailyRound(content, progress, now)
        assertNotNull(round.lesson)
        assertTrue(round.reviewChunkIds.isNotEmpty())
    }
}
