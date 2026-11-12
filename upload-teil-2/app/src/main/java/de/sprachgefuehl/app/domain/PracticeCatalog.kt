package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress

/**
 * The practice hub offers four ways to work on the same sentences. They differ in what they
 * train, so the menu can name the one that fits the profile instead of leaving the learner
 * alone with four equal cards.
 */
data class PracticeMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val skills: List<Skill>
) {
    /** "Sprechen & Bausteine" – shown under the title. */
    val trainsLine: String get() = skills.joinToString(" & ") { it.tab }

    /** Only a mode that asks for the voice counts as a speaking task in the weekly log. */
    val speaks: Boolean get() = Skill.SPRECHEN in skills
}

/** What is waiting right now, plus the mode that fits. */
data class PracticeStanding(
    val due: Int,
    val shaky: Int,
    val learned: Int,
    val mode: PracticeMode,
    val line: String,
    val because: String
)

object PracticeCatalog {

    /**
     * The order matters: the first entry is also the gentle default when the app has no
     * evidence yet. Every mode names the skills it really trains, so the profile stays honest.
     */
    val modes = listOf(
        PracticeMode(
            id = "shadow",
            title = "Nachsprechen",
            subtitle = "Drei Tempi hören, dann selbst sagen.",
            icon = "mic",
            skills = listOf(Skill.SPRECHEN, Skill.BAUSTEINE)
        ),
        PracticeMode(
            id = "free",
            title = "Frei sprechen",
            subtitle = "Eine Begegnung, drei Sätze von dir.",
            icon = "star",
            skills = listOf(Skill.VERKNUEPFEN, Skill.SPRECHEN)
        ),
        PracticeMode(
            id = "dictation",
            title = "Diktat",
            subtitle = "Hören und aufschreiben, was du hörst.",
            icon = "chat",
            skills = listOf(Skill.HOEREN, Skill.BAUSTEINE)
        ),
        PracticeMode(
            id = "order",
            title = "Wortstellung",
            subtitle = "Die Wörter in die richtige Reihenfolge bringen.",
            icon = "link",
            skills = listOf(Skill.GRAMMATIK, Skill.BAUSTEINE)
        )
    )

    fun mode(id: String): PracticeMode = modes.firstOrNull { it.id == id } ?: modes.first()

    /** The mode that trains the weakest skill; without evidence it is the gentlest one. */
    fun recommended(progress: Progress): PracticeMode {
        val weakest = Skills.weakest(progress.skills) ?: return modes.first()
        return modes.firstOrNull { weakest in it.skills } ?: modes.first()
    }

    /** What is due, what is still shaky, and which round fits right now. */
    fun standing(progress: Progress, now: Long): PracticeStanding {
        val due = progress.chunkMemory.count { Srs.isDue(it.value, now) }
        val shaky = progress.chunkMemory.count { it.value.stage < 4 && it.value.reps > 0 }
        val learned = progress.chunkMemory.size
        val weakest = Skills.weakest(progress.skills)
        val mode = recommended(progress)
        val line = when {
            due > 0 -> "$due ${if (due == 1) "Satz ist" else "Sätze sind"} bald fällig."
            shaky > 0 -> "$shaky ${if (shaky == 1) "Satz wackelt" else "Sätze wackeln"} noch."
            learned == 0 -> "Noch keine Sätze gelernt – die erste Lektion bringt welche."
            else -> "Nichts fällig. Du kannst trotzdem eine Runde üben."
        }
        val because = if (weakest == null) {
            "Ohne Messung ist Nachsprechen der ruhigste Einstieg."
        } else {
            "Für ${weakest.tab} passt ${mode.title} gerade am besten."
        }
        return PracticeStanding(due, shaky, learned, mode, line, because)
    }
}
