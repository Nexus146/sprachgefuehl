package de.sprachgefuehl.app.data

import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.ContentIndex
import de.sprachgefuehl.app.domain.Diagnostics
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.domain.Milestones
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.SkillHistory
import de.sprachgefuehl.app.domain.Skills
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.VariantPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.random.Random

class LearningRepository(
    val content: ContentBundle,
    private val store: ProgressStore,
    private val clock: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault()
) {
    private val index = ContentIndex.of(content)
    private val _progress = MutableStateFlow(index.migrate(store.load(), content.version))
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private val _saveFailed = MutableStateFlow(false)
    val saveFailed: StateFlow<Boolean> = _saveFailed.asStateFlow()

    /**
     * Serialized so an answer, the study timer and a settings change cannot overwrite each
     * other. The save result is kept; a failed write must not look like a successful one.
     */
    @Synchronized
    private fun update(transform: (Progress) -> Progress) {
        val today = today()
        val next = SkillHistory.record(transform(_progress.value), now(), today)
        _progress.value = next
        _saveFailed.value = !store.save(next)
    }

    fun now(): Long = clock()

    /** Local calendar day; SRS timestamps stay absolute. */
    fun today(): Long = dayOf(now())

    fun dayOf(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

    /**
     * Creates the one active round and stores its concrete steps. Opening the same lesson again
     * returns the existing round; starting a different lesson replaces it.
     */
    @Synchronized
    fun startLesson(lessonId: String, lessonOverride: Lesson? = null): LessonSession? {
        val current = _progress.value
        current.lessonSession?.let { existing ->
            // Eine Runde aus einer älteren Inhaltsversion wird verworfen: Sonst liefen alte
            // Schritte ohne Satzbezug weiter, und ein Hörfehler könnte nie zurückkommen.
            if (existing.lessonId == lessonId &&
                existing.steps.isNotEmpty() &&
                lessonId !in current.completedLessons &&
                current.contentVersion == content.version
            ) return existing
        }
        val lesson = lessonOverride ?: content.lessons.firstOrNull { it.id == lessonId } ?: return null
        // A focus round brings its own concrete steps; mixing it would dissolve the card.
        val steps = if (lessonOverride != null) lesson.steps
        else LessonMixer.steps(lesson, content, current, today())
        if (steps.isEmpty()) return null
        // A finished lesson is a deliberate repeat: start the new round at its first task.
        val start = if (lessonId in current.completedLessons) 0
        else (current.lessonProgress[lessonId] ?: 0).coerceIn(0, steps.lastIndex)
        val session = LessonSession(
            lessonId = lesson.id,
            runId = current.lessonRuns[lesson.id] ?: 0,
            startedDay = today(),
            steps = steps,
            index = start,
            startedAt = now()
        )
        update { progress ->
            progress.copy(
                lessonSession = session,
                lessonProgress = progress.lessonProgress + (lesson.id to start),
                contentVersion = content.version
            )
        }
        return session
    }

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

    /**
     * Gemessene Zeit, nicht geschätzt: Sie landet am Tag und im Gesamtwert. Ein zweiter Aufruf
     * für dieselben Sekunden ist ausgeschlossen, weil die Oberfläche nur einmal abrechnet.
     */
    fun recordLearningTime(seconds: Int) {
        if (seconds <= 0) return
        val day = today()
        update { progress ->
            progress.copy(
                studySeconds = progress.studySeconds + seconds,
                timeByDay = progress.timeByDay + (day to ((progress.timeByDay[day] ?: 0) + seconds))
            )
        }
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
            tags = Personalization.storyTagsFor(_progress.value.profile)
        ) ?: return null
        update { it.copy(variantIds = it.variantIds + (situationId to variant.id)) }
        return variant
    }

    fun completeOnboarding() = update { it.copy(onboarded = true) }

    fun updateSettings(transform: (Settings) -> Settings) = update { it.copy(settings = transform(it.settings)) }

    fun updateProfile(transform: (Profile) -> Profile) = update { it.copy(profile = transform(it.profile)) }

    fun markHomeIntroSeen() = update { it.copy(homeIntroSeen = true) }

    /** "Passt noch" – the profile is confirmed without changing a thing. */
    fun markProfileReviewed() = update {
        it.copy(profile = it.profile.copy(reviewedDay = today()))
    }

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
    @Synchronized
    fun importProgress(text: String): Boolean {
        val decoded = store.decode(text) ?: return false
        return importProgress(decoded)
    }

    /** Replaces the local document with a cloud or restored object. */
    @Synchronized
    fun importProgress(progress: Progress): Boolean {
        val decoded = index.migrate(progress, content.version)
        _progress.value = decoded
        _saveFailed.value = !store.save(decoded)
        return true
    }

    fun recordStep(lessonId: String, stepIndex: Int) = update { progress ->
        val next = max(progress.lessonProgress[lessonId] ?: 0, stepIndex + 1)
        val session = progress.lessonSession
            ?.takeIf { it.lessonId == lessonId }
            ?.let { existing -> existing.copy(index = max(existing.index, next)) }
        progress.copy(
            lessonProgress = progress.lessonProgress + (lessonId to next),
            lessonSession = session
        )
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

    /**
     * One checked answer written as one state transition. Memory, error card, mistake list and
     * skill profile all see the same snapshot, and the JSON file is written only once.
     */
    @Synchronized
    fun recordAnswer(
        chunkId: String,
        grade: Int,
        usedHint: Boolean,
        skills: List<Skill>,
        cardId: String,
        lessonId: String,
        lessonTitle: String,
        target: String,
        heard: String,
        mistakeSkill: String,
        trackMistake: Boolean,
        checkSkill: Skill?,
        checkCorrect: Boolean,
        level: Int,
        taskType: String? = null
    ) = update { progress ->
        var next = progress
        // Ein Aufgabentyp, der fast immer sitzt, wird später schwerer abgefragt; einer, der oft
        // scheitert, leichter. Die Zahlen stammen aus jeder geprüften Aufgabe.
        if (!taskType.isNullOrBlank()) {
            val stat = next.taskStats[taskType] ?: TaskStat()
            next = next.copy(
                taskStats = next.taskStats + (taskType to stat.copy(
                    attempts = stat.attempts + 1,
                    correct = stat.correct + if (grade >= 2) 1 else 0
                ))
            )
        }
        if (cardId.isNotBlank()) {
            next = next.copy(
                // Eine Karte gehört zur falschen Antwort – richtig gelöst zählt sie nicht als Fehler.
                errorCounts = if (grade < 2) {
                    next.errorCounts + (cardId to (next.errorCounts[cardId] ?: 0) + 1)
                } else next.errorCounts,
                skills = Skills.observe(next.skills, Skill.GRAMMATIK, 30, now())
            )
        }
        if (chunkId.isNotBlank()) {
            val signal = Skills.gradeSignal(grade, usedHint)
            val observed = skills.fold(next.skills) { profile, skill ->
                Skills.observe(profile, skill, signal, now())
            }
            next = next.copy(
                chunkMemory = next.chunkMemory + (chunkId to Srs.review(next.memoryFor(chunkId), grade, now(), usedHint)),
                skills = observed
            )
            if (trackMistake) {
                if (grade < 2) {
                    val entry = Mistake(
                        chunkId = chunkId,
                        lessonId = lessonId,
                        lessonTitle = lessonTitle,
                        target = target,
                        heard = heard.trim().take(120),
                        day = today(),
                        skill = mistakeSkill,
                        cardId = cardId,
                        patternId = content.chunks.firstOrNull { it.id == chunkId }?.patternId.orEmpty()
                    )
                    next = next.copy(
                        mistakes = (next.mistakes.filterNot { it.chunkId == chunkId } + entry).takeLast(24)
                    )
                } else {
                    val resolvedCard = next.mistakes.firstOrNull { it.chunkId == chunkId }
                        ?.cardId.orEmpty()
                    val counts = if (resolvedCard.isBlank()) next.errorCounts else {
                        val left = (next.errorCounts[resolvedCard] ?: 0) - 1
                        if (left <= 0) next.errorCounts - resolvedCard
                        else next.errorCounts + (resolvedCard to left)
                    }
                    next = next.copy(
                        mistakes = next.mistakes.filterNot { it.chunkId == chunkId },
                        errorCounts = counts
                    )
                }
            }
        }
        if (checkSkill != null) {
            next = next.copy(
                skills = Skills.observe(
                    next.skills,
                    checkSkill,
                    Skills.signal(checkCorrect, level),
                    now()
                )
            )
        }
        next
    }

    /**
     * Ein Fehler wird nicht nur gezählt, sondern mit Satz und Gehörtem gemerkt. Pro Baustein
     * bleibt der jüngste Eintrag; die Liste ist begrenzt, damit eine Sicherung klein bleibt.
     */
    fun recordMistake(
        chunkId: String,
        lessonId: String,
        lessonTitle: String,
        target: String,
        heard: String,
        skill: String,
        cardId: String
    ) {
        if (chunkId.isBlank()) return
        val day = today()
        update { progress ->
            val entry = Mistake(
                chunkId = chunkId,
                lessonId = lessonId,
                lessonTitle = lessonTitle,
                target = target,
                heard = heard.trim().take(120),
                day = day,
                skill = skill,
                cardId = cardId,
                patternId = content.chunks.firstOrNull { it.id == chunkId }?.patternId.orEmpty()
            )
            val list = (progress.mistakes.filterNot { it.chunkId == chunkId } + entry).takeLast(24)
            progress.copy(mistakes = list)
        }
    }

    /** Eine richtige Antwort auf einen eingestreuten Satz schließt den Eintrag. */
    fun resolveMistake(chunkId: String) {
        if (chunkId.isBlank()) return
        update { progress ->
            val cardId = progress.mistakes.firstOrNull { it.chunkId == chunkId }?.cardId.orEmpty()
            val counts = if (cardId.isBlank()) progress.errorCounts else {
                val left = (progress.errorCounts[cardId] ?: 0) - 1
                if (left <= 0) progress.errorCounts - cardId
                else progress.errorCounts + (cardId to left)
            }
            progress.copy(
                mistakes = progress.mistakes.filterNot { it.chunkId == chunkId },
                errorCounts = counts
            )
        }
    }

    fun markCanDo(id: String) = update { it.copy(canDo = it.canDo + id) }

    /**
     * [speakingTasks] zählt nur, was die Lernende wirklich gesprochen hat – ein mit "Weiter"
     * übersprungener Sprechschritt ist keine Sprechaufgabe.
     */
    @Synchronized
    fun completeLesson(lesson: Lesson, speakingTasks: Int, seconds: Int = 0): Boolean {
        var completed = false
        val canDoIds = content.canDo.filter { it.lessonId == lesson.id }.map { it.id }.toSet()
        val day = today()
        // Gemessene Zeit ersetzt die Schätzung, sobald es eine gibt.
        val minutes = if (seconds > 0) max(1, (seconds + 59) / 60) else lesson.xpMinutes
        update { progress ->
            if (lesson.id in progress.completedLessons) return@update progress
            completed = true
            val withLesson = progress.copy(
                completedLessons = progress.completedLessons + lesson.id,
                canDo = progress.canDo + canDoIds,
                lessonRuns = progress.lessonRuns + (lesson.id to ((progress.lessonRuns[lesson.id] ?: 0) + 1)),
                // Closing the round ends its persisted session.
                lessonSession = progress.lessonSession?.takeUnless { it.lessonId == lesson.id }
            )
            // Die Sätze der Lektion gehören ab jetzt zur Sammlung – auch wenn diesmal nicht
            // gesprochen wurde. Sonst hätte eine Lektion ohne Mikrofon nichts zum Wiederholen.
            val seeded = lesson.chunkIds.fold(withLesson) { acc, id ->
                if (id.isBlank() || acc.chunkMemory.containsKey(id)) acc
                else acc.copy(chunkMemory = acc.chunkMemory + (id to Memory(due = now())))
            }
            Garden.recordSession(
                seeded, day, minutes, speakingTasks, lesson.chunkIds.size
            )
        }
        return completed
    }

    fun recordReviewSession(minutes: Int, speakingTasks: Int, seconds: Int = 0) {
        val day = today()
        val measured = if (seconds > 0) max(1, (seconds + 59) / 60) else minutes
        update { progress -> Garden.recordSession(progress, day, measured, speakingTasks, 0) }
    }

    fun addRecording(label: String, path: String) = update { progress ->
        val list = progress.recordings + RecordingRef(
            id = "rec_${now()}",
            label = label,
            path = path,
            day = today()
        )
        progress.copy(recordings = list.takeLast(40))
    }

    /** Removes the entry and the file behind it – a deleted recording should not stay on disk. */
    fun removeRecording(id: String) {
        val item = _progress.value.recordings.firstOrNull { it.id == id } ?: return
        runCatching { File(item.path).delete() }
        update { progress -> progress.copy(recordings = progress.recordings.filterNot { it.id == id }) }
    }

    fun reset() = update { Progress(contentVersion = content.version) }

    /** Remembers what the last practice round worked on, so the next one can vary. */
    fun recordPracticeRound(chunkIds: List<String>) = update {
        it.copy(lastPracticeChunkIds = chunkIds.distinct().take(20))
    }

    fun dueReviewCount(): Int = _progress.value.chunkMemory.count {
        it.key in index.chunkIds && Srs.isDue(it.value, now())
    }

    /** Exportable local trace for support and tests; it never leaves the device by itself. */
    fun diagnosticTrace(lessonId: String): String? {
        val progress = _progress.value
        val lesson = content.lessons.firstOrNull { it.id == lessonId } ?: return null
        val session = progress.lessonSession?.takeIf { it.lessonId == lessonId }
        val steps = session?.steps ?: LessonMixer.steps(lesson, content, progress, today())
        return Diagnostics.trace(
            contentVersion = progress.contentVersion,
            startedDay = session?.startedDay ?: today(),
            seed = progress.seed,
            lesson = lesson,
            steps = steps,
            index = session?.index ?: (progress.lessonProgress[lessonId] ?: 0)
        ).render()
    }
}
