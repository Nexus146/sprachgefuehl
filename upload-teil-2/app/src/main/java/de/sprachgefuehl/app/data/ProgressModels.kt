package de.sprachgefuehl.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Memory(
    val stability: Double = 0.0,
    val difficulty: Double = 5.0,
    val due: Long = 0L,
    val reps: Int = 0,
    val lapses: Int = 0,
    val stage: Int = 1,
    val lastGrade: Int = 0,
    val contextCount: Int = 0
)

/**
 * The skill profile at the start of one day. Optional on purpose: backups from earlier versions
 * carry no history and still load.
 */
@Serializable
data class SkillSnapshot(
    val day: Long = 0L,
    /** Ratings in the order of [de.sprachgefuehl.app.domain.Skill.entries]. */
    val ratings: List<Double> = emptyList()
)

@Serializable
data class SessionLog(
    val day: Long,
    val minutes: Int,
    val speakingTasks: Int,
    val newChunks: Int,
    val errorCardsSeen: Int = 0
)

@Serializable
data class RecordingRef(
    val id: String,
    val label: String,
    val path: String,
    val day: Long
)

/**
 * Everything the app knows about the learner. Optional on purpose: the app stays usable
 * without a name, and the profile only changes tone and suggestions, never access.
 */
@Serializable
data class Profile(
    val name: String = "",
    val reasonId: String = "",
    /** Mehrere Gründe sind erlaubt – ein Leben hat selten nur einen. */
    val reasonIds: Set<String> = emptySet(),
    val interests: Set<String> = emptySet(),
    val dailyMinutes: Int = 10,
    val weekdays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val createdAt: Long = 0L,
    /** Epoch day of the last "passt noch" answer. 0 means it was never confirmed. */
    val reviewedDay: Long = 0L
) {
    val greetingName: String get() = name.trim().take(24)

    /** Alle Gründe – alte Sicherungen kennen nur den einen [reasonId]. */
    fun reasons(): Set<String> =
        reasonIds.ifEmpty { setOfNotNull(reasonId.takeIf { it.isNotBlank() }) }
}

/**
 * One skill, estimated from what the learner actually did. [rating] is 0..100 and only ever
 * moves a little per observation; [observations] says how much the value can be trusted.
 */
@Serializable
data class SkillState(
    val rating: Double = 40.0,
    val observations: Int = 0
)

@Serializable
data class SkillProfile(
    val hoeren: SkillState = SkillState(),
    val grammatik: SkillState = SkillState(),
    val bausteine: SkillState = SkillState(),
    val sprechen: SkillState = SkillState(),
    val verknuepfen: SkillState = SkillState(),
    /** When the standort check ran. 0 means it has not run yet. */
    val measuredAt: Long = 0L
)

/**
 * The standort check in progress. Every answer is stored, so closing the app in the middle costs
 * nothing: the next start continues on the same task.
 */
@Serializable
data class CheckupState(
    val blockIndex: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val answeredInBlock: Int = 0,
    val answeredIds: List<String> = emptyList(),
    val results: Map<String, Boolean> = emptyMap(),
    val selfRating: Int = 1,
    val startedAt: Long = 0L,
    val finished: Boolean = false
) {
    val answered: Int get() = answeredIds.size
}

@Serializable
data class Settings(
    val themeMode: String = "system",
    val fontScale: Float = 1.0f,
    val speechRate: Float = 0.85f,
    val voiceName: String = "",
    val timersEnabled: Boolean = true,
    val explainImmediately: Boolean = true,
    val reducedMotion: Boolean = false,
    val soundEnabled: Boolean = true,
    val showGarden: Boolean = true,
    val germanSupport: Boolean = true
)

/**
 * Ein Satz, der zuletzt nicht saß. Er wird in einer späteren Lektion wieder aufgegriffen – mit
 * dem, was damals tatsächlich gesagt wurde, damit die Wiederholung nicht beliebig wirkt.
 */
@Serializable
data class Mistake(
    val chunkId: String = "",
    val lessonId: String = "",
    val lessonTitle: String = "",
    val target: String = "",
    val heard: String = "",
    /** Epochentag, damit „zuletzt“ auch nach einem Neustart stimmt. */
    val day: Long = 0L,
    /** Skill-ID (hoeren, grammatik, …) – daraus wählt der Mixer das Wiederholungsformat. */
    val skill: String = "",
    val cardId: String = "",
    /** Das Muster des Satzes – daraus baut die nächste Lektion einen neuen Kontext. */
    val patternId: String = ""
)

/**
 * Wie oft ein Aufgabentyp saß. Aus diesen Zahlen kalibriert der Mixer seine Leiter: Ein Typ, der
 * fast immer gelingt, wandert eine Stufe nach oben; einer, der oft scheitert, eine nach unten.
 */
@Serializable
data class TaskStat(
    val attempts: Int = 0,
    val correct: Int = 0
) {
    val rate: Double get() = if (attempts == 0) 0.0 else correct.toDouble() / attempts
}

/**
 * The exact lesson round the learner is in. The generated steps are stored with it, so resuming
 * does not depend on the day, a new random seed or a changed task count. Only one lesson can be
 * active at a time.
 */
@Serializable
data class LessonSession(
    val lessonId: String = "",
    val runId: Int = 0,
    val startedDay: Long = 0L,
    val steps: List<Step> = emptyList(),
    val index: Int = 0,
    val startedAt: Long = 0L
)

@Serializable
data class Progress(
    val onboarded: Boolean = false,
    /** Version of content.json this progress was last reconciled with. */
    val contentVersion: Int = 1,
    val placementLevel: Int = 1,
    val placementScore: Int = 0,
    val selfRating: Int = 1,
    val completedSteps: Set<String> = emptySet(),
    val completedLessons: Set<String> = emptySet(),
    val lessonProgress: Map<String, Int> = emptyMap(),
    val chunkMemory: Map<String, Memory> = emptyMap(),
    val errorCounts: Map<String, Int> = emptyMap(),
    val canDo: Set<String> = emptySet(),
    val sessions: List<SessionLog> = emptyList(),
    val gardenGrowth: Float = 0f,
    /** Completed rounds, not active days: several lessons on one day still grow the garden. */
    val gardenRounds: Int = 0,
    val lastSessionDay: Long = 0L,
    /** Chunks from the last practice round, so the next round can avoid repeating them. */
    val lastPracticeChunkIds: List<String> = emptyList(),
    val recordings: List<RecordingRef> = emptyList(),
    val milestones: Set<String> = emptySet(),
    val profile: Profile = Profile(),
    val settings: Settings = Settings(),
    val skills: SkillProfile = SkillProfile(),
    /** One entry per active day, at most 60 – see SkillHistory. */
    val skillHistory: List<SkillSnapshot> = emptyList(),
    val checkup: CheckupState = CheckupState(),
    /** Decides which story a learner meets. Set once, so the world stays stable. */
    val seed: Long = 0L,
    /** The story chosen per situation. Kept so it does not change between two app starts. */
    val variantIds: Map<String, String> = emptyMap(),
    /** The one-time hint that explains the home screen. Only an explanation, never a gate. */
    val homeIntroSeen: Boolean = false,
    /** Wie oft eine Lektion schon begonnen wurde – dreht den Aufgabenpool weiter. */
    val lessonRuns: Map<String, Int> = emptyMap(),
    /** Die laufende Lektion mit ihren festen Schritten; null, wenn keine offen ist. */
    val lessonSession: LessonSession? = null,
    /** Wirklich gemessene Lernzeit in Sekunden, nicht geschätzt. */
    val studySeconds: Int = 0,
    /** Gemessene Sekunden je Kalendertag (Epochentag). */
    val timeByDay: Map<Long, Int> = emptyMap(),
    /** Offene Fehler, die in späteren Lektionen zurückkommen – der jüngste zuerst. */
    val mistakes: List<Mistake> = emptyList(),
    /** Wie oft welcher Aufgabentyp saß – die Grundlage der Kalibrierung. */
    val taskStats: Map<String, TaskStat> = emptyMap()
) {
    val totalSpeakingTasks: Int get() = sessions.sumOf { it.speakingTasks }
    val totalMinutes: Int get() = sessions.sumOf { it.minutes }

    fun studiedSecondsOn(day: Long): Int = timeByDay[day] ?: 0

    /** Gemessene Minuten eines Tages; nie kleiner als die tatsächlich angefangene Minute. */
    fun measuredMinutesOn(day: Long): Int {
        val seconds = studiedSecondsOn(day)
        return if (seconds <= 0) minutesOn(day) else (seconds + 59) / 60
    }

    val measuredMinutes: Int get() = (studySeconds + 59) / 60

    fun memoryFor(chunkId: String): Memory = chunkMemory[chunkId] ?: Memory()

    fun minutesOn(day: Long): Int = sessions.filter { it.day == day }.sumOf { it.minutes }

    fun speakingOn(day: Long): Int = sessions.filter { it.day == day }.sumOf { it.speakingTasks }

    val activeDays: Int get() = sessions.map { it.day }.distinct().size
}
