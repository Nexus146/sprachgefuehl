package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog

/** Die Pflanze eines Beets: eigener Name und eine Zeile, was sie bedeutet. */
data class GardenPlant(val id: String, val name: String, val line: String)

/** Ein Beet im Garten: eine Fertigkeit, ihre Pflanze und ihr Reifegrad. */
data class GardenBed(
    val skill: Skill,
    val plant: GardenPlant,
    val growth: Float,
    val stage: Int,
    val observed: Boolean,
    val title: String,
    val note: String,
    val nextLine: String?
)

/** Ein Stück Garten, das mit der Zeit dazukommt – Bank, Teich, Baum und so weiter. */
data class GardenFeature(val id: String, val title: String, val fromRounds: Int, val reached: Boolean)

/** Ein Tier, das in den Garten kommt, wenn ein Bereich sicher sitzt oder ein Stück steht. */
data class GardenResident(val id: String, val name: String, val line: String, val present: Boolean)

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

    /** Die fünf Pflanzen, je eine pro Bereich. */
    private val plants = mapOf(
        Skill.HOEREN to GardenPlant("sonnenblume", "Sonnenblume", "Sie dreht sich zum Klang."),
        Skill.GRAMMATIK to GardenPlant("rose", "Rose", "Sie hat Regeln – Dornen und Blüten."),
        Skill.BAUSTEINE to GardenPlant("lavendel", "Lavendel", "Viele kleine Blüten, ein Duft."),
        Skill.SPRECHEN to GardenPlant("tulpe", "Tulpe", "Sie öffnet sich, wenn du sprichst."),
        Skill.VERKNUEPFEN to GardenPlant("efeu", "Efeu", "Es verbindet, was einzeln steht.")
    )

    private val stageNames = listOf("Samen", "Trieb", "Blätter", "Knospe", "Blüte")

    fun stageName(stage: Int): String = stageNames[stage.coerceIn(0, stageNames.lastIndex)]

    /**
     * Der Reifegrad eines Beets: aus Können und Erfahrung. Ein einzelner guter Tag bringt
     * keine Blüte – dafür braucht es mindestens sechs Beobachtungen.
     */
    private fun stageFor(rating: Double, observations: Int): Int = when {
        observations == 0 -> 0
        rating >= 85.0 && observations >= 6 -> 4
        rating >= 65.0 -> 3
        rating >= 45.0 -> 2
        else -> 1
    }

    private fun nextLineFor(stage: Int): String? = when (stage) {
        0 -> "Bis „Trieb“: eine erste Aufgabe in diesem Bereich"
        1 -> "Bis „Blätter“: weiter üben, bis es leichter wird"
        2 -> "Bis „Knospe“: weiter üben, bis es sicher sitzt"
        3 -> "Bis „Blüte“: noch etwas Übung, bis es ohne Nachdenken geht"
        else -> null
    }

    /**
     * Ein Beet pro Fertigkeit, in fester Reihenfolge. Es wächst aus dem, was in diesem Bereich
     * wirklich beobachtet wurde. Ohne Beobachtung bleibt das Beet leer: Die App weiß dort noch
     * nichts und nennt den Bereich nicht schwach.
     */
    fun beds(progress: Progress): List<GardenBed> = Skill.entries.map { skill ->
        val state = progress.skills.state(skill)
        val observed = state.observations > 0
        val stage = stageFor(state.rating, state.observations)
        GardenBed(
            skill = skill,
            plant = plants.getValue(skill),
            growth = stage / 4f,
            stage = stage,
            observed = observed,
            title = skill.tab,
            note = if (observed) stageName(stage) else "Wartet auf dich",
            nextLine = nextLineFor(stage)
        )
    }

    /** Bank, Teich und so weiter – in der Reihenfolge, in der sie in den Garten kommen. */
    private val featurePlan = listOf(
        GardenFeature("bank", "Die Bank", 1, false),
        GardenFeature("teich", "Der Teich", 5, false),
        GardenFeature("baum", "Der Baum", 10, false),
        GardenFeature("vogelhaus", "Das Vogelhaus", 15, false),
        GardenFeature("wiese", "Die Blumenwiese", 25, false)
    )

    /** Was im Garten schon steht und was noch kommt. Erreichtes verschwindet nie. */
    fun features(progress: Progress): List<GardenFeature> {
        val rounds = rounds(progress)
        return featurePlan.map { it.copy(reached = rounds >= it.fromRounds) }
    }

    /** Das nächste Gartenstück, in Worten – null, wenn alles steht. */
    fun nextFeature(progress: Progress): String? {
        val rounds = rounds(progress)
        val next = featurePlan.firstOrNull { it.fromRounds > rounds } ?: return null
        val remaining = next.fromRounds - rounds
        return "Noch $remaining ${if (remaining == 1) "Runde" else "Runden"} bis „${next.title}“"
    }

    private data class ResidentPlan(
        val id: String,
        val name: String,
        val line: String,
        val skill: Skill?,
        val featureId: String?
    )

    private val residentPlan = listOf(
        ResidentPlan("schmetterling", "Ein Schmetterling", "Er kommt, wenn du frei sprichst.", Skill.SPRECHEN, null),
        ResidentPlan("amsel", "Eine Amsel", "Sie singt, wenn du sicher hörst.", Skill.HOEREN, null),
        ResidentPlan("biene", "Eine Biene", "Sie summt, wenn deine Wendungen sitzen.", Skill.BAUSTEINE, null),
        ResidentPlan("libelle", "Eine Libelle", "Sie fliegt über den Teich.", null, "teich"),
        ResidentPlan("igel", "Ein Igel", "Er wohnt unter dem Baum.", null, "baum")
    )

    /**
     * Wer im Garten wohnt: ein Tier kommt, wenn ein Bereich blüht oder sein Gartenstück steht.
     * Beides wächst nur – ein Bewohner verschwindet nie wieder.
     */
    fun residents(progress: Progress): List<GardenResident> {
        val rounds = rounds(progress)
        return residentPlan.map { plan ->
            val bySkill = plan.skill?.let { skill ->
                val state = progress.skills.state(skill)
                stageFor(state.rating, state.observations) >= 4
            } ?: false
            val byFeature = plan.featureId?.let { id ->
                featurePlan.any { it.id == id && rounds >= it.fromRounds }
            } ?: false
            GardenResident(plan.id, plan.name, plan.line, bySkill || byFeature)
        }
    }

    /** Die letzten Runden als Zeilen – ohne Datum und ohne Bewertung. */
    fun chronicle(progress: Progress, limit: Int = 4): List<String> =
        progress.sessions.takeLast(limit).reversed().map { session ->
            "${session.minutes} ${if (session.minutes == 1) "Minute" else "Minuten"} · " +
                "${session.speakingTasks} ${if (session.speakingTasks == 1) "Satz" else "Sätze"} gesprochen · " +
                "${session.newChunks} neu"
        }
}
