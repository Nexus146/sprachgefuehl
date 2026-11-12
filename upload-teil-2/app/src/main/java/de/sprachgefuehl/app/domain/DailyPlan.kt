package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import java.time.LocalDate

enum class DayMood {
    /** A planned learning day, the goal is still open. */
    OPEN,

    /** The goal for today is done – the app says so and stops nudging. */
    DONE,

    /** Today is not one of the learner's days. Nothing is expected. */
    REST
}

data class DaySummary(
    val minutes: Int,
    val goal: Int,
    val speakingTasks: Int,
    val newChunks: Int,
    val mood: DayMood
) {
    val reachedGoal: Boolean get() = goal > 0 && minutes >= goal
}

object DailyPlan {

    /** day is the epoch day stored in [SessionLog]; the profile uses ISO weekdays (1 = Monday). */
    fun weekday(day: Long): Int = LocalDate.ofEpochDay(day).dayOfWeek.value

    fun isRestDay(profile: Profile, day: Long): Boolean {
        val days = profile.weekdays
        if (days.isEmpty()) return false
        return weekday(day) !in days
    }

    fun summary(progress: Progress, day: Long): DaySummary {
        val goal = progress.profile.dailyMinutes.takeIf { it > 0 } ?: 10
        val sessions = progress.sessions.filter { it.day == day }
        val minutes = sessions.sumOf { it.minutes }
        val mood = when {
            isRestDay(progress.profile, day) -> DayMood.REST
            minutes >= goal -> DayMood.DONE
            else -> DayMood.OPEN
        }
        return DaySummary(
            minutes = minutes,
            goal = goal,
            speakingTasks = sessions.sumOf { it.speakingTasks },
            newChunks = sessions.sumOf { it.newChunks },
            mood = mood
        )
    }

    /** The next planned learning day after [day], used for the "bis Donnerstag" line. */
    fun nextLearningDay(profile: Profile, day: Long): Long? {
        if (profile.weekdays.isEmpty()) return null
        var candidate = day + 1
        repeat(7) {
            if (!isRestDay(profile, candidate)) return candidate
            candidate += 1
        }
        return null
    }
}
