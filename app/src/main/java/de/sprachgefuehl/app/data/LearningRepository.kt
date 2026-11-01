package de.sprachgefuehl.app.data

import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.domain.Milestones
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.SkillHistory
import de.sprachgefuehl.app.domain.Skills
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.VariantPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.max
import kotlin.random.Random

class LearningRepository(
    val content: ContentBundle,
    private val store: ProgressStore,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val _progress = MutableStateFlow(store.load())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private fun update(transform: (Progress) -> Progress) {
        // Every save passes the daily snapshot, so the week can show movement without a timer.
        val next = SkillHistory.record(transform(_progress.value), now())
        _progress.value = next
        store.save(next)
    }

    fun now(): Long = clock()

    fun setPlacement(level: Int, score: Int, selfRating: Int) = update {
        it.copy(
            onboarded = true,
            placementLevel = level,
            placementScore = score,
            selfRating = selfRating,
            seed = newSeed(it.seed),
            profile = if (it.profile.createdAt == 0L) it.profile.copy(createdAt = now()) else it.profile
        )
    }

    private fun newSeed(current: Long): Long = if (current != 0L) current else Random.nextLong() or 1L

    // ---- Skills and the standort check ----------------------------------------------------

    fun observe(skill: Skill, signal: Int) = update {
        it.copy(skills = Skills.observe(it.skills, skill, signal, now()))
    }

    fun observeAnswer(skill: Skill, correct: Boolean, level: Int = 1) =
        observe(skill, Skills.signal(correct, level))

    fun startCheckup(selfRating: Int) = update { it.copy(checkup = Checkup.start(now(), selfRating)) }

    fun answerCheckup(item: CheckupItem, correct: Boolean) = update { progress ->
        val before = progress.checkup.blockIndex
        val after = Checkup.answer(progress.checkup, item, correct, content.checkup)
        val skills = if (after.blockIndex > before) {
            Checkup.ratingFor(progress.skills, after, content.checkup, now())
        } else {
            progress.skills
        }
        progress.copy(checkup = after, skills = skills)
    }

    fun finishCheckup(selfRating: Int) = update { progress ->
        val state = progress.checkup
        val skills = if (state.results.isEmpty()) {
            Checkup.fromSelfRating(progress.skills, selfRating, now())
        } else {
            Checkup.ratingFor(progress.skills, state, content.checkup, now())
        }
        // Onboarding stays open on purpose: the learner should see the result before the app
        // switches into the learning shell.
        progress.copy(
            checkup = state.copy(finished = true),
            skills = skills,
            placementLevel = Checkup.overallLevel(skills),
            placementScore = state.results.count { it.value },
            selfRating = selfRating,
            profile = if (progress.profile.createdAt == 0L) progress.profile.copy(createdAt = now()) else progress.profile
        )
    }

    /** The story for a situation: chosen once, then kept. */
    fun variantFor(situationId: String): SituationVariant? {
        val situation = content.situations.firstOrNull { it.id == situationId } ?: return null
        if (situation.variants.isEmpty()) return null
        _progress.value.variantIds[situationId]?.let { chosen ->
            situation.variants.firstOrNull { it.id == chosen }?.let { return it }
        }
        val variant = VariantPicker.forSituation(
            situation = situation,
            seed = _progress.value.seed,
            interests = _progress.value.profile.interests
        ) ?: return null
        update { it.copy(variantIds = it.variantIds + (situationId to variant.id)) }
        return variant
    }

    fun completeOnboarding() = update { it.copy(onboarded = true) }

    fun updateSettings(transform: (Settings) -> Settings) = update { it.copy(settings = transform(it.settings)) }

    fun updateProfile(transform: (Profile) -> Profile) = update { it.copy(profile = transform(it.profile)) }

    fun updatePlacementLevel(level: Int) = update { it.copy(placementLevel = level.coerceIn(0, 2)) }

    /** Returns the milestones that just became true and marks them as celebrated. */
    fun celebrateMilestones(): List<Milestone> {
        val fresh = Milestones.newOnes(_progress.value)
        if (fresh.isEmpty()) return emptyList()
        update { it.copy(milestones = it.milestones + fresh.map { milestone -> milestone.id }) }
        return fresh
    }

    fun exportProgress(): String = store.encode(_progress.value)

    /** Accepts a validated backup document and replaces the current progress. */
    fun importProgress(text: String): Boolean {
        val decoded = store.decode(text) ?: return false
        _progress.value = decoded
        store.save(decoded)
        return true
    }

    fun recordStep(lessonId: String, stepIndex: Int) = update { progress ->
        val current = progress.lessonProgress[lessonId] ?: 0
        progress.copy(lessonProgress = progress.lessonProgress + (lessonId to max(current, stepIndex + 1)))
    }

    /**
     * One graded attempt on a chunk. [skills] says which parts of the profile the exercise really
     * trained: a dictation is listening, word order is grammar – not speaking.
     */
    fun reviewChunk(
        chunkId: String,
        grade: Int,
        usedHint: Boolean,
        skills: List<Skill> = listOf(Skill.BAUSTEINE, Skill.SPRECHEN)
    ) = update { progress ->
        val memory = progress.memoryFor(chunkId)
        val updated = Srs.review(memory, grade, now(), usedHint)
        val signal = Skills.gradeSignal(grade, usedHint)
        val observed = skills.fold(progress.skills) { profile, skill ->
            Skills.observe(profile, skill, signal, now())
        }
        progress.copy(chunkMemory = progress.chunkMemory + (chunkId to updated), skills = observed)
    }

    fun recordError(cardId: String) {
        if (cardId.isBlank()) return
        update { progress ->
            progress.copy(
                errorCounts = progress.errorCounts + (cardId to (progress.errorCounts[cardId] ?: 0) + 1),
                skills = Skills.observe(progress.skills, Skill.GRAMMATIK, 30, now())
            )
        }
    }

    fun markCanDo(id: String) = update { it.copy(canDo = it.canDo + id) }

    /**
     * [speakingTasks] zählt nur, was die Lernende wirklich gesprochen hat – ein mit "Weiter"
     * übersprungener Sprechschritt ist keine Sprechaufgabe.
     */
    fun completeLesson(lesson: Lesson, speakingTasks: Int): Boolean {
        val before = _progress.value
        if (lesson.id in before.completedLessons) return false
        val canDoIds = content.canDo.filter { it.lessonId == lesson.id }.map { it.id }.toSet()
        val day = now() / Srs.DAY_MS
        update { progress ->
            val withLesson = progress.copy(
                completedLessons = progress.completedLessons + lesson.id,
                canDo = progress.canDo + canDoIds
            )
            // Die Sätze der Lektion gehören ab jetzt zur Sammlung – auch wenn diesmal nicht
            // gesprochen wurde. Sonst hätte eine Lektion ohne Mikrofon nichts zum Wiederholen.
            val seeded = lesson.chunkIds.fold(withLesson) { acc, id ->
                if (id.isBlank() || acc.chunkMemory.containsKey(id)) acc
                else acc.copy(chunkMemory = acc.chunkMemory + (id to Memory(due = now())))
            }
            Garden.recordSession(
                seeded, day, lesson.xpMinutes, speakingTasks, lesson.chunkIds.size
            )
        }
        return true
    }

    fun recordReviewSession(minutes: Int, speakingTasks: Int) {
        val day = now() / Srs.DAY_MS
        update { progress -> Garden.recordSession(progress, day, minutes, speakingTasks, 0) }
    }

    fun addRecording(label: String, path: String) = update { progress ->
        val list = progress.recordings + RecordingRef(
            id = "rec_${now()}",
            label = label,
            path = path,
            day = now() / Srs.DAY_MS
        )
        progress.copy(recordings = list.takeLast(40))
    }

    /** Removes the entry and the file behind it – a deleted recording should not stay on disk. */
    fun removeRecording(id: String) {
        val item = _progress.value.recordings.firstOrNull { it.id == id } ?: return
        runCatching { File(item.path).delete() }
        update { progress -> progress.copy(recordings = progress.recordings.filterNot { it.id == id }) }
    }

    fun reset() = update { Progress() }

    fun dueReviewCount(): Int = _progress.value.chunkMemory.count { Srs.isDue(it.value, now()) }
}
