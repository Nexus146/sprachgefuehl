package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState

/** The five things this app actually checks – not one score, but a profile. */
enum class Skill(val id: String, val label: String, val tab: String) {
    HOEREN("hoeren", "Hören & Verstehen", "Hören"),
    GRAMMATIK("grammatik", "Grammatik", "Grammatik"),
    BAUSTEINE("bausteine", "Bausteine & Wortschatz", "Bausteine"),
    SPRECHEN("sprechen", "Sprechen", "Sprechen"),
    VERKNUEPFEN("verknuepfen", "Verbinden & Verknüpfen", "Verknüpfen");

    companion object {
        fun fromId(id: String): Skill? = entries.firstOrNull { it.id == id }
    }
}

fun SkillProfile.state(skill: Skill): SkillState = when (skill) {
    Skill.HOEREN -> hoeren
    Skill.GRAMMATIK -> grammatik
    Skill.BAUSTEINE -> bausteine
    Skill.SPRECHEN -> sprechen
    Skill.VERKNUEPFEN -> verknuepfen
}

fun SkillProfile.with(skill: Skill, state: SkillState): SkillProfile = when (skill) {
    Skill.HOEREN -> copy(hoeren = state)
    Skill.GRAMMATIK -> copy(grammatik = state)
    Skill.BAUSTEINE -> copy(bausteine = state)
    Skill.SPRECHEN -> copy(sprechen = state)
    Skill.VERKNUEPFEN -> copy(verknuepfen = state)
}

/**
 * The words for the four stages. Deliberately no percentages and no comparison with others:
 * the learner should read what she can do, not how far behind she is.
 */
object SkillCatalog {

    val stageLabels = listOf("Wir fangen an", "Du baust auf", "Du bist sicher", "Du kannst es frei")

    fun stage(rating: Double): Int = when {
        rating < 40.0 -> 0
        rating < 65.0 -> 1
        rating < 85.0 -> 2
        else -> 3
    }

    fun stageLabel(stage: Int): String = stageLabels[stage.coerceIn(0, 3)]

    fun meaning(skill: Skill, stage: Int): String = when (skill) {
        Skill.HOEREN -> when (stage.coerceIn(0, 3)) {
            0 -> "Kurze Sätze verstehst du, wenn langsam gesprochen wird."
            1 -> "Du verstehst einfache Alltagsgespräche in ruhigem Tempo."
            2 -> "Du folgst Gesprächen auch im normalen Tempo."
            else -> "Du verstehst auch schnelle, verschachtelte Sätze."
        }
        Skill.GRAMMATIK -> when (stage.coerceIn(0, 3)) {
            0 -> "Du baust einfache Sätze; für Vergangenheit fehlen dir noch Formen."
            1 -> "Du erkennst die Grundformen, bei Fragen und Zeit brauchst du Stützen."
            2 -> "Du bildest Fragen und Vergangenheit meist richtig."
            else -> "Du wechselst sicher zwischen den Zeiten."
        }
        Skill.BAUSTEINE -> when (stage.coerceIn(0, 3)) {
            0 -> "Du kennst einzelne Wörter, aber noch wenige fertige Wendungen."
            1 -> "Einige Bausteine kannst du schon ohne Hilfe abrufen."
            2 -> "Viele Wendungen sitzen so, dass du sie im Gespräch findest."
            else -> "Du kannst aus deinem Vorrat frei wählen."
        }
        Skill.SPRECHEN -> when (stage.coerceIn(0, 3)) {
            0 -> "Du sprichst nach, was du hörst."
            1 -> "Du sagst kurze Sätze mit einer Stütze."
            2 -> "Du sprichst in ganzen Sätzen, ohne lange zu suchen."
            else -> "Du hältst ein kurzes Gespräch selbst."
        }
        Skill.VERKNUEPFEN -> when (stage.coerceIn(0, 3)) {
            0 -> "Du sprichst noch in einzelnen Sätzen."
            1 -> "Du verbindest Sätze mit and, but und because."
            2 -> "Du nutzt when, if und that auch mitten im Satz."
            else -> "Du baust längere Gedanken flüssig."
        }
    }

    fun nextStep(skill: Skill, stage: Int): String = when (skill) {
        Skill.HOEREN -> when (stage.coerceIn(0, 3)) {
            0 -> "Wir hören jeden Dialog zuerst langsam, mit deutschem Hinweis."
            1 -> "Wir hören zweimal und antworten ohne Text."
            2 -> "Wir hören im normalen Tempo und achten auf Details."
            else -> "Wir hören schnelle Sätze und fassen sie zusammen."
        }
        Skill.GRAMMATIK -> when (stage.coerceIn(0, 3)) {
            0 -> "Wir fangen mit be und have an."
            1 -> "Fragen mit do üben wir in kleinen Schritten."
            2 -> "Wir üben Vergangenheit in einem echten Satz."
            else -> "Wir wechseln die Zeiten in einer kleinen Erzählung."
        }
        Skill.BAUSTEINE -> when (stage.coerceIn(0, 3)) {
            0 -> "Wir lernen zwei Wendungen pro Lektion."
            1 -> "Wir wiederholen deine Bausteine in neuen Sätzen."
            2 -> "Wir bauen neue Sätze aus deinen Bausteinen."
            else -> "Wir halten die Bausteine mit Wiederholungen frisch."
        }
        Skill.SPRECHEN -> when (stage.coerceIn(0, 3)) {
            0 -> "Wir sprechen nach, was du hörst."
            1 -> "Wir bauen einen Satz aus einem Baustein."
            2 -> "Wir sprechen frei über eine kleine Aufgabe."
            else -> "Wir erzählen in drei Sätzen von deinem Tag."
        }
        Skill.VERKNUEPFEN -> when (stage.coerceIn(0, 3)) {
            0 -> "Wir verbinden zwei kurze Sätze."
            1 -> "Wir üben and, but und because."
            2 -> "Wir bauen when- und if-Sätze."
            else -> "Wir erzählen mit mehreren verbundenen Sätzen."
        }
    }
}

/** What the profile screens show: the stage plus the sentence that explains it. */
data class SkillView(
    val skill: Skill,
    val stage: Int,
    val observed: Boolean,
    val meaning: String,
    val next: String
)

object Skills {

    /**
     * One observation. The first few move the estimate a lot (the app knows almost nothing yet),
     * later ones only a little – a single bad day never undoes weeks of work.
     */
    fun observe(profile: SkillProfile, skill: Skill, signal: Int, now: Long = 0L): SkillProfile {
        val current = profile.state(skill)
        val alpha = when {
            current.observations == 0 -> 0.7
            current.observations < 3 -> 0.4
            else -> (2.0 / (current.observations + 4)).coerceAtLeast(0.12)
        }
        val rating = (current.rating + alpha * (signal.coerceIn(0, 100) - current.rating))
            .coerceIn(0.0, 100.0)
        val next = profile.with(skill, SkillState(rating = rating, observations = current.observations + 1))
        return if (now > 0 && next.measuredAt == 0L) next.copy(measuredAt = now) else next
    }

    /** Signal for one task at a given difficulty (0..3). */
    fun signal(correct: Boolean, level: Int = 1): Int =
        if (correct) (52 + 13 * level.coerceIn(0, 3)) else (18 + 8 * level.coerceIn(0, 3))

    fun gradeSignal(grade: Int, usedHint: Boolean): Int {
        val base = when {
            grade >= 3 -> 88
            grade == 2 -> 62
            else -> 26
        }
        return base - if (usedHint) 8 else 0
    }

    fun views(profile: SkillProfile): List<SkillView> = Skill.entries.map { skill ->
        val state = profile.state(skill)
        val stage = SkillCatalog.stage(state.rating)
        SkillView(
            skill = skill,
            stage = stage,
            observed = state.observations > 0,
            meaning = SkillCatalog.meaning(skill, stage),
            next = SkillCatalog.nextStep(skill, stage)
        )
    }

    /**
     * The skill with the lowest stage, then the lowest rating. Skills without evidence are not
     * called weak – the app cannot know that yet.
     */
    fun weakest(profile: SkillProfile): Skill? = Skill.entries
        .filter { profile.state(it).observations >= 2 }
        .minWithOrNull(
            compareBy(
                { SkillCatalog.stage(profile.state(it).rating) },
                { profile.state(it).rating },
                { -profile.state(it).observations }
            )
        )

    fun averageRating(profile: SkillProfile): Double {
        val rated = Skill.entries.map { profile.state(it) }.filter { it.observations > 0 }
        if (rated.isEmpty()) return 50.0
        return rated.sumOf { it.rating } / rated.size
    }
}
