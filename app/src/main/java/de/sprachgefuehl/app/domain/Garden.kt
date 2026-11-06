package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog

object Garden {

    fun stageForSessions(sessions: Int): Int = stageForRounds(sessions)

    fun stageForRounds(rounds: Int): Int = when {
        rounds >= 15 -> 5
        rounds >= 10 -> 4
        rounds >= 6 -> 3
        rounds >= 3 -> 2
        rounds >= 1 -> 1
        else -> 0
    }

    private fun rounds(progress: Progress): Int =
        if (progress.gardenRounds > 0) progress.gardenRounds else progress.sessions.size

    fun stage(progress: Progress): Int = stageForRounds(rounds(progress))

    fun growth(progress: Progress): Float = stage(progress) / 5f

    fun label(stage: Int): String = when (stage) {
        0 -> "Ein Platz wartet auf dich"
        1 -> "Der erste Trieb"
        2 -> "Zwei Blätter"
        3 -> "Eine kleine Krone"
        4 -> "Knospen"
        else -> "Dein Garten blüht"
    }

    /** A short name for the next stage, used in the "noch zwei Runden" line. */
    fun shortLabel(stage: Int): String = when (stage) {
        1 -> "erster Trieb"
        2 -> "zwei Blätter"
        3 -> "kleine Krone"
        4 -> "Knospen"
        else -> "Blüte"
    }

    /** How far the next stage is, in whole rounds. Null when the garden is fully grown. */
    fun nextStep(progress: Progress): String? {
        val rounds = rounds(progress)
        val next = listOf(1, 3, 6, 10, 15).firstOrNull { it > rounds } ?: return null
        val remaining = next - rounds
        return "Noch $remaining ${if (remaining == 1) "Runde" else "Runden"} bis „${shortLabel(stageForRounds(next))}“"
    }

    /**
     * Adds a session once per day. A missed day never takes anything away: the garden only
     * ever grows, which is a deliberate choice for older learners.
     */
    fun recordSession(progress: Progress, day: Long, minutes: Int, speakingTasks: Int, newChunks: Int): Progress {
        val alreadyToday = progress.sessions.any { it.day == day }
        val session = SessionLog(day, minutes, speakingTasks, newChunks)
        val sessions = if (alreadyToday) {
            progress.sessions.map {
                if (it.day == day) it.copy(
                    minutes = it.minutes + minutes,
                    speakingTasks = it.speakingTasks + speakingTasks,
                    newChunks = it.newChunks + newChunks
                ) else it
            }
        } else {
            progress.sessions + session
        }
        val next = progress.copy(
            sessions = sessions,
            gardenRounds = progress.gardenRounds + 1,
            lastSessionDay = day
        )
        return next.copy(gardenGrowth = growth(next))
    }
}
