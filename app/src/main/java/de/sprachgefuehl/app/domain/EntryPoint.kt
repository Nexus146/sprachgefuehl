package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress

/**
 * Wo die Lernende nach dem Standort-Check einsteigt.
 *
 * Bisher begann jeder bei der ersten Lektion des ersten Pfads – auch wer im Check Stufe 2 erreicht
 * hatte. Das Ziel hier ist nicht, Fortschritt zu erfinden, sondern Unterforderung zu vermeiden:
 * Wer schon sicher ist, startet weiter hinten; wer unsicher ist, beginnt vorne und bekommt Stützen.
 *
 * Der Einstieg gilt nur für den allerersten Start. Wer bereits eine Lektion begonnen hat, bleibt
 * in seinem Pfad – ein Sprung würde den roten Faden und den gespeicherten Fortschritt zerreißen.
 */
object EntryPoint {

    /** Rang der Niveaustufen; unbekannte Angaben zählen als A1. */
    private val ranks = mapOf("A1" to 0, "A1–A2" to 1, "A2" to 2, "B1" to 3)

    fun rankOf(lesson: Lesson): Int = ranks[lesson.level] ?: 0

    fun firstLesson(content: ContentBundle, progress: Progress): Lesson? {
        val ordered = SessionBuilder.orderedLessons(content)
        val remaining = ordered.filter { it.id !in progress.completedLessons }
        if (remaining.isEmpty()) return null
        if (progress.completedLessons.isNotEmpty() || progress.lessonProgress.isNotEmpty()) {
            return remaining.first()
        }

        val wanted = progress.placementLevel.coerceIn(0, 2)
        // Mindestens das gemessene Niveau. Gibt es nichts Passendes mehr, bleibt der Pfad übrig.
        val suitable = remaining.filter { rankOf(it) >= wanted }.ifEmpty { remaining }

        // Der Wunschpfad aus dem Onboarding gewinnt, aber nur innerhalb des passenden Niveaus.
        val preferred = Personalization.preferredPaths(progress.profile)
        val inPreferred = if (preferred.isEmpty()) emptyList() else suitable.filter { lesson ->
            val path = content.situations.firstOrNull { it.id == lesson.situationId }?.pathId
            path != null && path in preferred
        }
        val pool = inPreferred.ifEmpty { suitable }

        val position = ordered.withIndex().associate { it.value.id to it.index }
        return pool.minWithOrNull(
            compareBy({ rankOf(it) }, { position[it.id] ?: Int.MAX_VALUE })
        )
    }
}
