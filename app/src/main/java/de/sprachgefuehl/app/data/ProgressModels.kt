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
    val interests: Set<String> = emptySet(),
    val dailyMinutes: Int = 10,
    val weekdays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val createdAt: Long = 0L
) {
    val greetingName: String get() = name.trim().take(24)
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

@Serializable
data class Progress(
    val onboarded: Boolean = false,
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
    val lastSessionDay: Long = 0L,
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
    val variantIds: Map<String, String> = emptyMap()
) {
    val totalSpeakingTasks: Int get() = sessions.sumOf { it.speakingTasks }
    val totalMinutes: Int get() = sessions.sumOf { it.minutes }

    fun memoryFor(chunkId: String): Memory = chunkMemory[chunkId] ?: Memory()

    fun minutesOn(day: Long): Int = sessions.filter { it.day == day }.sumOf { it.minutes }

    fun speakingOn(day: Long): Int = sessions.filter { it.day == day }.sumOf { it.speakingTasks }

    val activeDays: Int get() = sessions.map { it.day }.distinct().size
}
