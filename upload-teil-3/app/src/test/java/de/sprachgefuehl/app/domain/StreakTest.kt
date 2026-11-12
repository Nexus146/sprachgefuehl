package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakTest {

    private fun progress(vararg days: Long): Progress = Progress(
        sessions = days.map { SessionLog(day = it, minutes = 5, speakingTasks = 1, newChunks = 0) }
    )

    @Test
    fun `three days in a row count three`() {
        val info = Streak.of(progress(100, 101, 102), today = 102)
        assertEquals(3, info.current)
        assertTrue(info.todayDone)
    }

    @Test
    fun `an open today keeps yesterday's streak alive`() {
        val info = Streak.of(progress(100, 101), today = 102)
        assertEquals(2, info.current)
        assertFalse(info.todayDone)
    }

    @Test
    fun `a missed day ends the run without erasing the best`() {
        val info = Streak.of(progress(100, 101, 102, 105), today = 105)
        assertEquals(1, info.current)
        assertEquals(3, info.best)
    }

    @Test
    fun `the week starts on monday and marks active days`() {
        // 19723 ist ein Montag.
        val info = Streak.of(progress(19723, 19725), today = 19723)
        assertEquals(7, info.week.size)
        assertTrue(info.week[0])
        assertFalse(info.week[1])
        assertTrue(info.week[2])
        assertEquals(2, info.weekDone)
    }

    @Test
    fun `the sentence stays calm`() {
        assertTrue(Streak.line(Streak.of(progress(100), 100)).contains("Ein Tag"))
        assertTrue(Streak.line(Streak.of(progress(100), 101)).contains("noch offen"))
        assertTrue(Streak.line(Streak.of(Progress(), 101)).contains("Eine Runde heute"))
    }
}
