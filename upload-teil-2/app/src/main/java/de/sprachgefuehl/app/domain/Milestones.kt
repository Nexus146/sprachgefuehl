package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress

/**
 * Milestones are collected, never taken away. No streak can break and nothing is ranked:
 * each badge marks a moment in the neighbourhood that is now true.
 */
data class Milestone(
    val id: String,
    val title: String,
    val detail: String,
    val icon: String = "star"
)

object Milestones {

    val all: List<Milestone> = listOf(
        Milestone("first_round", "Der erste Kaffee", "Deine erste Runde ist geschafft – Sam kennt dich jetzt.", "leaf"),
        Milestone("three_rounds", "Stammgast", "Dreimal im Viertel geübt.", "leaf"),
        Milestone("five_days", "Fünf Tage im Viertel", "An fünf Tagen geübt, ohne Druck.", "leaf"),
        Milestone("ten_lessons", "Zehn Begegnungen", "Zehn Gesprächssituationen geöffnet.", "chat"),
        Milestone("fifty_sentences", "Fünfzig Sätze gesprochen", "Fünfzig Mal selbst gesprochen.", "chat"),
        Milestone("hundred_minutes", "Hundert Minuten", "Über eineinhalb Stunden Englisch.", "star"),
        Milestone("first_fluent", "Ein Satz sitzt", "Ein Baustein kommt ohne Stütze.", "star"),
        Milestone("ten_solid", "Zehn sichere Sätze", "Zehn Bausteine sitzen im Langzeitgedächtnis.", "star"),
        Milestone("error_hunter", "Ruths Rat beherzigt", "Drei typische Fehler bearbeitet.", "grammar"),
        Milestone("own_voice", "Deine Stimme", "Du hast dich aufgenommen und verglichen.", "chat")
    )

    fun unlocked(progress: Progress): Set<String> {
        val ids = mutableSetOf<String>()
        if (progress.sessions.isNotEmpty()) ids += "first_round"
        if (progress.sessions.size >= 3) ids += "three_rounds"
        if (progress.activeDays >= 5) ids += "five_days"
        if (progress.completedLessons.size >= 10) ids += "ten_lessons"
        if (progress.totalSpeakingTasks >= 50) ids += "fifty_sentences"
        if (progress.totalMinutes >= 100) ids += "hundred_minutes"
        if (progress.chunkMemory.values.any { it.stage >= 4 }) ids += "first_fluent"
        if (progress.chunkMemory.values.count { it.stage >= 3 } >= 10) ids += "ten_solid"
        if (progress.errorCounts.size >= 3) ids += "error_hunter"
        if (progress.recordings.isNotEmpty()) ids += "own_voice"
        return ids
    }

    /** Milestones that are true but were not celebrated yet. */
    fun newOnes(progress: Progress): List<Milestone> {
        val unlocked = unlocked(progress)
        return all.filter { it.id in unlocked && it.id !in progress.milestones }
    }

    fun earned(progress: Progress): List<Milestone> = all.filter { it.id in progress.milestones }
}
