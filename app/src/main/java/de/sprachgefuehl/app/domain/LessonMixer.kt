package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Chunk
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Option
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Step
import kotlin.random.Random

/**
 * Baut jede Lektionsrunde neu – gewichtet nach Onboarding, Fehlern und Können.
 *
 * Der Pool wird zur Laufzeit aus dem Inhalt erzeugt: Nachsprechen, Übersetzen, Abrufen,
 * Wortstellung, Bedeutungswahl, Hören, Diktat, Lückensatz, Verbindungswörter und Transfer.
 */
object LessonMixer {

    /** Die Runde bleibt in diesem Rahmen, damit die Zeitangaben ehrlich bleiben. */
    const val MIN_TASKS = 13
    const val MAX_TASKS = 17

    private val speakingTypes = setOf(
        "SpeakRepeat", "SpeakFree", "SpeakFillSlot", "TransferTask", "Shadowing",
        "TranslateToEnglish", "Recall", "Paraphrase", "WhatsAppReply"
    )

    private val stopWords = setOf(
        "a", "an", "the", "to", "of", "in", "on", "at", "for", "and", "or", "is", "am", "are",
        "was", "were", "be", "been", "do", "does", "did", "i", "you", "he", "she", "it", "we",
        "they", "my", "your", "her", "his", "their", "this", "that", "these", "those", "please",
        "with", "from", "as", "me", "him", "them", "us", "there", "have", "has", "had", "would",
        "could", "can", "will", "should"
    )

    private data class Learner(
        val weights: Map<Skill, Double>,
        val level: Int,
        val mistakes: List<Mistake>,
        val weakest: Skill?,
        val focusSkills: Set<Skill>,
        val preferredPaths: Set<String>
    )

    fun steps(lesson: Lesson, content: ContentBundle, progress: Progress, day: Long): List<Step> {
        val run = progress.lessonRuns[lesson.id] ?: 0
        val random = Random(seed(lesson.id, progress.seed, day, run))
        val chunks = lesson.chunkIds.mapNotNull { id -> content.chunks.firstOrNull { it.id == id } }
        if (chunks.isEmpty()) return lesson.steps

        val learner = learnerFor(lesson, progress)
        val core = lesson.steps
        val dialog = core.firstOrNull { it.type == "DialogInput" }
        val comprehension = core.firstOrNull { it.type == "ComprehensionDeep" }
        val noticing = core.firstOrNull { it.type == "Noticing" }
        val intros = core.filter { it.type == "ChunkIntro" && it.title == "Baustein" }
        val speakFree = core.firstOrNull { it.type == "SpeakFree" }
        val grammar = core.firstOrNull { it.type == "GrammarSpot" }
        val selfCheck = core.firstOrNull { it.type == "SelfCheck" }

        val result = mutableListOf<Step>()
        dialog?.let { result += it }
        comprehension?.let { result += it }
        noticing?.let { result += it }
        result += intros

        // Der erste Baustein beginnt immer mit Sprechen. Danach entscheidet die Gewichtung.
        var practiceIndex = 0
        chunks.forEachIndexed { index, chunk ->
            val memory = progress.memoryFor(chunk.id)
            val shaky = memory.lastGrade in 1..2 || memory.lapses > 0
            val count = if (index == 0 || shaky) 2 else 1
            val used = mutableSetOf<String>()
            repeat(count) {
                val type = if (index == 0 && it == 0) "SpeakRepeat"
                else pickType(learner, shaky, random, used)
                used += type
                result += task(type, chunk, lesson, content, random, practiceIndex++, learner)
            }
        }

        connectorStep(lesson, content, random, learner)?.let { result += it }
        // Fehler aus früheren Lektionen haben Vorrang: Sie schließen die Lücke von zuletzt.
        mistakeStep(lesson, content, learner, random)?.let { result += it }
        if (result.size <= 12) {
            weakSkillStep(lesson, chunks, content, learner, random)?.let { result += it }
        }
        if (result.size <= 13) {
            transferStep(lesson, chunks, content, learner, random)?.let { result += it }
        }

        speakFree?.let { result += it }
        grammar?.let { result += it }
        selfCheck?.let { result += it }
        return addUnderstandingGate(result, content, random)
    }

    private fun seed(lessonId: String, globalSeed: Long, day: Long, run: Int): Long =
        globalSeed xor lessonId.hashCode().toLong() xor (day * 1_000_003L) xor (run * 7_919L)

    private fun learnerFor(lesson: Lesson, progress: Progress): Learner {
        val profile = progress.profile
        val focus = ProfileCatalog.focusSkills(profile.reasons() + profile.interests)
        val weakest = Skills.weakest(progress.skills)
        val mistakeSkills = progress.mistakes.mapNotNull { Skill.fromId(it.skill) }
        val weights = Skill.entries.associateWith { skill ->
            var weight = 1.0
            if (skill in focus) weight += 0.9
            if (skill == weakest) weight += 1.1
            weight += 0.55 * mistakeSkills.count { it == skill }.coerceAtMost(3)
            weight
        }
        val average = Skills.averageRating(progress.skills)
        val level = when {
            progress.selfRating >= 2 || progress.placementLevel >= 2 || average >= 72.0 -> 2
            progress.selfRating >= 1 || progress.placementLevel >= 1 || average >= 45.0 -> 1
            else -> 0
        }
        return Learner(
            weights = weights,
            level = level,
            mistakes = progress.mistakes.filter { it.chunkId !in lesson.chunkIds },
            weakest = weakest,
            focusSkills = focus,
            preferredPaths = Personalization.preferredPaths(profile)
        )
    }

    private fun skillForType(type: String): Skill? = when (type) {
        "SpeakRepeat", "TranslateToEnglish", "Recall", "SpeakFree", "TransferTask" -> Skill.SPRECHEN
        "BuildSentence", "GapFill" -> Skill.GRAMMATIK
        "MeaningChoice", "SpeakFillSlot" -> Skill.BAUSTEINE
        "ListenMeaning", "Dictation", "ListenNumber", "MinimalPair" -> Skill.HOEREN
        "ChooseConnector", "ConnectClauses" -> Skill.VERKNUEPFEN
        else -> null
    }

    private fun pickType(
        learner: Learner,
        shaky: Boolean,
        random: Random,
        used: Set<String>
    ): String {
        val supportive = listOf("SpeakRepeat", "ListenMeaning", "MeaningChoice")
        val normal = listOf(
            "SpeakRepeat", "TranslateToEnglish", "BuildSentence", "MeaningChoice",
            "ListenMeaning", "GapFill"
        )
        val free = listOf(
            "TranslateToEnglish", "BuildSentence", "MeaningChoice", "ListenMeaning",
            "Dictation", "Recall", "GapFill"
        )
        val base = when {
            learner.level == 0 -> supportive
            shaky -> supportive + "TranslateToEnglish"
            learner.level >= 2 -> free
            else -> normal
        }
        val pool = base.filter { it !in used }.ifEmpty { listOf("SpeakRepeat") }
        val weights = pool.associateWith { type ->
            val skill = skillForType(type)
            (skill?.let { learner.weights[it] } ?: 1.0).coerceAtLeast(0.15)
        }
        return weightedPick(weights, random)
    }

    private fun weightedPick(weights: Map<String, Double>, random: Random): String {
        val total = weights.values.sum()
        if (total <= 0.0) return weights.keys.first()
        var roll = random.nextDouble() * total
        for ((key, weight) in weights) {
            roll -= weight
            if (roll <= 0.0) return key
        }
        return weights.keys.last()
    }

    private fun task(
        type: String,
        chunk: Chunk,
        lesson: Lesson,
        content: ContentBundle,
        random: Random,
        index: Int,
        learner: Learner
    ): Step {
        val id = "${lesson.id}_mix_${index + 1}"
        val words = chunk.en.trim().split(Regex(" +")).filter { it.isNotBlank() }
        val skill = skillForType(type)
        val why = whyFor(skill, learner)
        return when (type) {
            "TranslateToEnglish" -> Step(
                id = id,
                type = type,
                title = "Auf Englisch sagen",
                promptDe = "Sag auf Englisch: „${chunk.de}“",
                target = chunk.en,
                accept = listOf(chunk.en),
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why
            )
            "Recall" -> Step(
                id = id,
                type = type,
                title = "Aus dem Kopf",
                promptDe = "Sag auf Englisch: „${chunk.de}“ – ohne Modell.",
                target = chunk.en,
                accept = listOf(chunk.en),
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why,
                modelHidden = true
            )
            "BuildSentence" -> Step(
                id = id,
                type = type,
                title = "Wortstellung",
                promptDe = "Tippe die Wörter in der richtigen Reihenfolge an.",
                target = chunk.en,
                words = words,
                model = chunk.en,
                chunkId = chunk.id,
                why = why
            )
            "MeaningChoice" -> Step(
                id = id,
                type = type,
                title = "Was heißt das?",
                promptDe = "Wähle die passende deutsche Bedeutung.",
                promptEn = chunk.en,
                target = chunk.en,
                options = meaningOptions(chunk, content, random),
                model = chunk.de,
                chunkId = chunk.id,
                explanationDe = chunk.tip,
                why = why
            )
            "ListenMeaning" -> Step(
                id = id,
                type = type,
                title = "Hör zu und wähle",
                promptDe = "Hör den Satz und wähle die passende Bedeutung.",
                target = chunk.en,
                options = meaningOptions(chunk, content, random),
                model = chunk.de,
                chunkId = chunk.id,
                explanationDe = chunk.tip,
                why = why
            )
            "Dictation" -> Step(
                id = id,
                type = type,
                title = "Diktat",
                promptDe = "Hör zu und schreibe den Satz auf. Hör so oft du magst.",
                target = chunk.en,
                chunkId = chunk.id,
                modelHidden = true,
                why = why
            )
            "GapFill" -> gapFillTask(chunk, id, content, random, why)
                ?: task("MeaningChoice", chunk, lesson, content, random, index, learner).copy(why = why)
            else -> Step(
                id = id,
                type = "SpeakRepeat",
                title = "Nachsprechen",
                promptDe = "Hör zu und sprich nach. Nicht perfekt – verständlich.",
                target = chunk.en,
                accept = listOf(chunk.en),
                model = chunk.en,
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why
            )
        }
    }

    private fun gapFillTask(
        chunk: Chunk,
        id: String,
        content: ContentBundle,
        random: Random,
        why: String
    ): Step? {
        val words = chunk.en.trim().split(Regex(" +")).filter { it.isNotBlank() }
        val candidates = words.withIndex().filter { (_, word) ->
            val clean = word.lowercase().trim('.', ',', '!', '?', ';', ':')
            clean.length >= 3 && clean !in stopWords
        }
        val pick = candidates.randomOrNull(random) ?: return null
        val correct = pick.value.trim('.', ',', '!', '?', ';', ':')
        val blanked = words.toMutableList().also { it[pick.index] = "_____" }.joinToString(" ")
        val distractors = content.chunks
            .flatMap { it.en.split(Regex(" +")) }
            .map { it.lowercase().trim('.', ',', '!', '?', ';', ':') }
            .filter { it.length >= 3 && it !in stopWords && it != correct.lowercase() }
            .distinct()
            .shuffled(random)
            .take(2)
        if (distractors.size < 2) return null
        val options = (listOf(correct to true) + distractors.map { it to false })
            .shuffled(random)
            .map { (text, isCorrect) -> Option(text = text, correct = isCorrect) }
        return Step(
            id = id,
            type = "GapFill",
            title = "Welches Wort fehlt?",
            promptDe = "Wähle das Wort, das in die Lücke passt.",
            promptEn = blanked,
            target = chunk.en,
            options = options,
            model = chunk.en,
            chunkId = chunk.id,
            explanationDe = chunk.de,
            why = why
        )
    }

    private fun meaningOptions(chunk: Chunk, content: ContentBundle, random: Random): List<Option> {
        val others = content.chunks
            .filter { it.id != chunk.id && it.de.isNotBlank() && it.de != chunk.de }
            .shuffled(random)
            .map { it.de }
            .distinct()
            .take(2)
        return (listOf(chunk.de to true) + others.map { it to false })
            .shuffled(random)
            .map { (text, correct) -> Option(text = text, correct = correct) }
    }

    private fun whyFor(skill: Skill?, learner: Learner): String = when {
        skill == null -> ""
        skill == learner.weakest ->
            "${skill.tab} ist gerade dein schwächster Bereich – deshalb kommt diese Aufgabe."
        skill in learner.focusSkills ->
            "Passt zu deiner Auswahl beim Start: ${skill.tab}."
        else -> ""
    }

    private fun connectorStep(
        lesson: Lesson,
        content: ContentBundle,
        random: Random,
        learner: Learner
    ): Step? {
        val base = lesson.steps.firstOrNull { it.type == "ChooseConnector" } ?: return null
        val connector = content.connectorLevels.firstOrNull { it.id == base.connectorId } ?: return base
        val example = connector.examples.getOrNull(random.nextInt(connector.examples.size.coerceAtLeast(1)))
            ?: return base
        val why = when {
            Skill.VERKNUEPFEN == learner.weakest ->
                "Verknüpfen ist gerade dein schwächster Bereich – deshalb ein anderes Beispiel."
            Skill.VERKNUEPFEN in learner.focusSkills ->
                "Passt zu deiner Auswahl beim Start: Verknüpfen."
            else -> ""
        }
        return base.copy(
            id = "${lesson.id}_mix_connector",
            promptEn = example.a,
            target = example.b,
            model = example.b,
            options = connector.words.split(" · ").map { Option(text = it) },
            why = why
        )
    }

    /** Ein echter Fehler aus einer anderen Lektion, mit dem, was damals gesagt wurde. */
    private fun mistakeStep(
        lesson: Lesson,
        content: ContentBundle,
        learner: Learner,
        random: Random
    ): Step? {
        val mistake = learner.mistakes.maxByOrNull { it.day } ?: return null
        val chunk = content.chunks.firstOrNull { it.id == mistake.chunkId } ?: return null
        val origin = content.lessons.firstOrNull { it.id == mistake.lessonId }
            ?: originLesson(chunk.id, content)
        val skill = Skill.fromId(mistake.skill)
        val type = when (skill) {
            Skill.HOEREN -> if (learner.level >= 1) "Dictation" else "ListenMeaning"
            Skill.GRAMMATIK -> if (random.nextBoolean()) "BuildSentence" else "GapFill"
            Skill.SPRECHEN -> if (learner.level >= 2) "Recall" else "SpeakRepeat"
            Skill.BAUSTEINE -> "MeaningChoice"
            else -> "TranslateToEnglish"
        }
        val base = task(type, chunk, lesson, content, random, 90, learner)
        val note = buildString {
            append("In „${origin?.title ?: "einer früheren Lektion"}“ ")
            if (mistake.heard.isNotBlank()) {
                append("war deine letzte Antwort „${mistake.heard.take(60)}“ noch nicht sicher. ")
            } else {
                append("war dieser Satz noch wackelig. ")
            }
            append("Jetzt kommt er zurück.")
        }
        return base.copy(
            id = "${lesson.id}_mix_recall",
            title = "Wieder getroffen",
            originLessonId = origin?.id.orEmpty(),
            originTitle = origin?.title.orEmpty(),
            originNote = note,
            why = "Dieser Satz war zuletzt noch nicht sicher."
        )
    }

    /** Der schwächste Bereich bekommt eine Extraaufgabe – aus dem Stoff dieser Lektion. */
    private fun weakSkillStep(
        lesson: Lesson,
        chunks: List<Chunk>,
        content: ContentBundle,
        learner: Learner,
        random: Random
    ): Step? {
        val weakest = learner.weakest ?: return null
        val type = when (weakest) {
            Skill.HOEREN -> if (learner.level >= 1) "Dictation" else "ListenMeaning"
            Skill.GRAMMATIK -> "GapFill"
            Skill.SPRECHEN -> if (learner.level >= 2) "Recall" else "SpeakRepeat"
            Skill.BAUSTEINE -> "MeaningChoice"
            Skill.VERKNUEPFEN -> return null
        }
        val chunk = chunks.randomOrNull(random) ?: return null
        val step = task(type, chunk, lesson, content, random, 91, learner)
        return step.copy(
            id = "${lesson.id}_mix_weak",
            why = "${weakest.tab} ist gerade dein schwächster Bereich – deshalb diese Extraaufgabe."
        )
    }

    /**
     * Ein Satz aus einem bevorzugten Bereich oder mit demselben Muster. Damit taucht der Stoff
     * aus dem Onboarding auch in Lektionen auf, die thematisch woanders liegen.
     */
    private fun transferStep(
        lesson: Lesson,
        chunks: List<Chunk>,
        content: ContentBundle,
        learner: Learner,
        random: Random
    ): Step? {
        val own = lesson.chunkIds.toSet()
        val preferred = content.chunks.filter { chunk ->
            chunk.id !in own && chunk.tags.any { it in learner.preferredPaths }
        }
        val patterns = chunks.map { it.patternId }.filter { it.isNotBlank() }.toSet()
        val samePattern = content.chunks.filter { chunk ->
            chunk.id !in own && chunk.patternId in patterns
        }
        val pool = preferred.ifEmpty { samePattern }
        val chunk = pool.randomOrNull(random) ?: return null
        val type = if (random.nextBoolean()) "TranslateToEnglish" else "MeaningChoice"
        val origin = originLesson(chunk.id, content)
        val base = task(type, chunk, lesson, content, random, 92, learner)
        return base.copy(
            id = "${lesson.id}_mix_transfer",
            title = "Neuer Zusammenhang",
            originLessonId = origin?.id.orEmpty(),
            originTitle = origin?.title.orEmpty(),
            originNote = "Das Muster kennst du aus „${origin?.title ?: "einer anderen Lektion"}“. " +
                "Jetzt in einem neuen Satz.",
            why = if (learner.preferredPaths.isNotEmpty()) {
                "Passt zu deinem Ziel aus dem Onboarding."
            } else {
                "Dasselbe Muster, eine neue Situation."
            }
        )
    }

    private fun originLesson(chunkId: String, content: ContentBundle): Lesson? =
        content.lessons
            .filter { chunkId.startsWith(it.id + "_") }
            .maxByOrNull { it.id.length }

    /**
     * Einmal pro Lektion: „Weißt du, was der Satz heißt?“ – nur bei langen Sätzen und nur dort,
     * wo das Deutsche nicht schon als Aufgabe dasteht. Drei Möglichkeiten, eine stimmt.
     */
    private fun addUnderstandingGate(
        steps: List<Step>,
        content: ContentBundle,
        random: Random
    ): List<Step> {
        val index = steps.indexOfFirst { step ->
            step.type in setOf("SpeakRepeat", "TransferTask") &&
                step.chunkId.isNotBlank() &&
                !step.modelHidden &&
                step.target.trim().split(Regex(" +")).size >= 6
        }
        if (index < 0) return steps
        val step = steps[index]
        val chunk = content.chunks.firstOrNull { it.id == step.chunkId } ?: return steps
        if (chunk.de.isBlank()) return steps
        val distractors = content.chunks
            .filter { it.id != chunk.id && it.de.isNotBlank() && it.de != chunk.de }
            .shuffled(random)
            .map { it.de }
            .distinct()
            .take(2)
        if (distractors.size < 2) return steps
        val options = (listOf(chunk.de) + distractors).shuffled(random)
        val updated = step.copy(
            understandingDe = chunk.de,
            understandingOptions = options,
            understandingAnswer = options.indexOf(chunk.de)
        )
        return steps.toMutableList().also { it[index] = updated }
    }

    /** Wird von der Oberfläche gebraucht, um Sprechaufgaben richtig zu buchen. */
    fun isSpeaking(type: String): Boolean = type in speakingTypes

    /** Die tatsächliche Länge dieser Runde – für „Schritt 4 von 11“ außerhalb der Lektion. */
    fun stepCount(lesson: Lesson, content: ContentBundle, progress: Progress, day: Long): Int =
        steps(lesson, content, progress, day).size
}
