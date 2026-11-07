package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress

/**
 * Wie schwer es für diese Lernende in einem Bereich gerade zugehen soll – pro Fertigkeit, nicht
 * global. 0 heißt: mit Stütze, 1 geführt, 2 freier Abruf, 3 frei ohne Hilfe.
 *
 * Der Unterschied ist pädagogisch, nicht kosmetisch: Ein Grammatikfehler darf die Hör- und
 * Sprechaufgaben nicht mit leichter machen. Deshalb trägt jede Fertigkeit ihr eigenes Ziel.
 */
data class AdaptiveTarget(
    val bySkill: Map<Skill, Int>,
    val notes: Map<Skill, String> = emptyMap()
) {
    /** Das Ziel eines Bereichs; ohne Angabe die mittlere Stufe. */
    fun of(skill: Skill?): Int = skill?.let { bySkill[it] } ?: 1

    /** Warum dieser Bereich gerade ruhiger oder schwerer läuft – leer, wenn es keinen Grund gibt. */
    fun noteFor(skill: Skill): String = notes[skill].orEmpty()

    val overall: Int
        get() = if (bySkill.isEmpty()) 1 else bySkill.values.sum() / bySkill.size
}

/**
 * Leitet aus dem, was wirklich gemessen wurde, ein Ziel pro Bereich ab:
 *
 * - Basis ist das Rating aus dem Standort-Check und den Lektionen (ohne Messung die Platzierung).
 * - Jeder Fehler der letzten sieben Tage senkt den Bereich um eine Stufe, ab drei Fehlern um zwei.
 *   Ein Fehler aus genau dieser Lektion wiegt zusätzlich – derselbe Stoff, derselbe Tag.
 * - Wer in einem Bereich seit mindestens zwei Beobachtungen fehlerfrei über 64 liegt, bekommt
 *   eine Stufe mehr: Der Abruf soll anstrengend bleiben, sonst lernt niemand etwas dazu.
 *
 * Die Grenzen sind 0..3. Ein Bereich wird nie ganz abgeschaltet, nur gestützt.
 */
object AdaptivePlan {

    /** Fehler, die älter sind, sagen nichts mehr über den aktuellen Stand. */
    const val MISTAKE_WINDOW_DAYS = 7L

    fun forProgress(progress: Progress, day: Long, lesson: Lesson? = null): AdaptiveTarget {
        val notes = mutableMapOf<Skill, String>()
        val levels = Skill.entries.associateWith { skill ->
            levelFor(progress, skill, day, lesson, notes)
        }
        return AdaptiveTarget(levels, notes)
    }

    private fun levelFor(
        progress: Progress,
        skill: Skill,
        day: Long,
        lesson: Lesson?,
        notes: MutableMap<Skill, String>
    ): Int {
        val state = progress.skills.state(skill)
        val base = if (state.observations == 0) {
            // Noch nichts gemessen: Die Platzierung aus dem Onboarding ist die ehrlichste Annahme.
            when {
                progress.placementLevel >= 2 || progress.selfRating >= 2 -> 2
                progress.placementLevel >= 1 || progress.selfRating >= 1 -> 1
                else -> 0
            }
        } else when {
            state.rating >= 82.0 -> 3
            state.rating >= 64.0 -> 2
            state.rating >= 42.0 -> 1
            else -> 0
        }

        val recent = progress.mistakes.count { mistake ->
            mistake.skill == skill.id && day - mistake.day <= MISTAKE_WINDOW_DAYS
        }
        val fromThisLesson = lesson != null && progress.mistakes.any {
            it.lessonId == lesson.id && it.skill == skill.id
        }

        var level = base
        if (recent >= 1) level -= 1
        if (recent >= 3) level -= 1
        if (fromThisLesson) level -= 1
        val raised = recent == 0 && !fromThisLesson && state.observations >= 2 && state.rating >= 64.0
        if (raised) level += 1

        when {
            fromThisLesson -> notes[skill] =
                "${skill.tab}: eine Stufe ruhiger, weil dieser Fehler aus dieser Lektion kommt."
            recent >= 1 -> notes[skill] =
                "${skill.tab}: eine Stufe ruhiger, weil zuletzt $recent Fehler kamen."
            raised -> notes[skill] = "${skill.tab}: eine Stufe schwerer, weil es zuletzt saß."
        }
        return level.coerceIn(0, 3)
    }
}
