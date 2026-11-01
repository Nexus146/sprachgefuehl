package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Memory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {

    private val day = 86_400_000L
    private val now = 1_700_000_000_000L

    @Test
    fun `a clear answer schedules further ahead than a hesitant one`() {
        val clear = Srs.review(Memory(), 3, now)
        val hesitant = Srs.review(Memory(), 2, now)
        assertTrue(clear.stability > hesitant.stability)
        assertTrue(clear.due > hesitant.due)
    }

    @Test
    fun `a failure shortens the interval and lowers the stage`() {
        var memory = Memory(reps = 3, stability = 10.0, stage = 4, due = now - day)
        val before = memory.stability
        memory = Srs.review(memory, 1, now)
        assertTrue(memory.stability < before)
        assertTrue(memory.stage <= 3)
        assertTrue(memory.lapses == 1)
    }

    @Test
    fun `used hints slow the growth but do not punish`() {
        val plain = Srs.review(Memory(), 3, now)
        val withHint = Srs.review(Memory(), 3, now, usedHint = true)
        assertTrue(withHint.stability < plain.stability)
        assertTrue(withHint.reps == 1)
        assertTrue(withHint.lapses == 0)
    }

    @Test
    fun `stage five requires repeated success in different contexts`() {
        var memory = Memory()
        repeat(6) { memory = Srs.review(memory, 3, now) }
        assertTrue(memory.stage >= 4)
        assertEquals(5, memory.stage)
    }

    @Test
    fun `intervals stay inside the caps`() {
        var memory = Memory()
        repeat(20) { memory = Srs.review(memory, 3, now) }
        assertTrue(memory.stability <= Srs.MAX_INTERVAL_DAYS)
        assertTrue(memory.difficulty in 1.0..10.0)
    }

    @Test
    fun `new cards are due immediately`() {
        assertTrue(Srs.isDue(Memory(), now))
        assertTrue(!Srs.isDue(Srs.review(Memory(), 3, now), now))
    }
}
