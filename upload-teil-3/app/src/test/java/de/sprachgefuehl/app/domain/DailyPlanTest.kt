package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyPlanTest {

    // 2026-10-26 is a Monday.
    private val monday: Long = LocalDate.of(2026, 10, 26).toEpochDay()

    @Test
    fun `weekday maps epoch days to ISO weekdays`() {
        assertEquals(1, DailyPlan.weekday(monday))
        assertEquals(2, DailyPlan.weekday(monday + 1))
        assertEquals(7, DailyPlan.weekday(monday + 6))
    }

    @Test
    fun `an unselected weekday is a rest day`() {
        val profile = Profile(weekdays = setOf(1, 3))
        assertFalse(DailyPlan.isRestDay(profile, monday))
        assertTrue(DailyPlan.isRestDay(profile, monday + 1))
    }

    @Test
    fun `an empty selection means no rest days`() {
        assertFalse(DailyPlan.isRestDay(Profile(weekdays = emptySet()), monday + 1))
    }

    @Test
    fun `reaching the goal closes the day`() {
        val progress = Progress(
            profile = Profile(dailyMinutes = 10, weekdays = setOf(1)),
            sessions = listOf(SessionLog(monday, 12, 8, 3))
        )
        val summary = DailyPlan.summary(progress, monday)
        assertEquals(DayMood.DONE, summary.mood)
        assertTrue(summary.reachedGoal)
        assertEquals(12, summary.minutes)
        assertEquals(8, summary.speakingTasks)
    }

    @Test
    fun `a rest day stays calm even after practising`() {
        val progress = Progress(
            profile = Profile(dailyMinutes = 10, weekdays = setOf(3)),
            sessions = listOf(SessionLog(monday, 5, 2, 1))
        )
        assertEquals(DayMood.REST, DailyPlan.summary(progress, monday).mood)
    }

    @Test
    fun `an open day counts the remaining minutes`() {
        val progress = Progress(
            profile = Profile(dailyMinutes = 15, weekdays = setOf(1)),
            sessions = listOf(SessionLog(monday, 4, 2, 0))
        )
        val summary = DailyPlan.summary(progress, monday)
        assertEquals(DayMood.OPEN, summary.mood)
        assertEquals(15, summary.goal)
        assertFalse(summary.reachedGoal)
    }

    @Test
    fun `the next learning day skips the rest days`() {
        val profile = Profile(weekdays = setOf(5))
        assertEquals(monday + 4, DailyPlan.nextLearningDay(profile, monday))
    }
}
