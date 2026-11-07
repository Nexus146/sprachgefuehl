package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Chunk
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Option
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.data.TaskStat
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
        /** Zielstufe je Bereich: 0 Stütze, 1 geführt, 2 freier Abruf, 3 frei ohne Hilfe. */
        val targets: AdaptiveTarget,
        val taskStats: Map<String, TaskStat>,
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

        val learner = learnerFor(lesson, progress, day)
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
        // Ein Typ kommt pro Runde höchstens einmal vor – das hält die Mischung breit.
        val used = mutableSetOf<String>()
        chunks.forEachIndexed { index, chunk ->
            val memory = progress.memoryFor(chunk.id)
            val shaky = memory.lastGrade in 1..2 || memory.lapses > 0
            val count = if (index == 0 || shaky) 3 else 2
            repeat(count) {
                val type = if (index == 0 && it == 0) "SpeakRepeat"
                else pickType(learner, shaky, random, used)
                used += type
                result += task(type, chunk, lesson, content, random, practiceIndex++, learner)
            }
        }

        connectorStep(lesson, content, random, learner)?.let { result += it }
        // Fehler aus früheren Lektionen haben Vorrang: Sie schließen die Lücke von zuletzt.
        // Ab zwei offenen Fehlern kommen beide zurück – der Fehler ist Pflichtteil der Runde.
        mistakeSteps(lesson, content, learner, random).forEach { result += it }
        if (result.size <= 12) {
            weakSkillStep(lesson, chunks, content, learner, random)?.let { result += it }
        }
        if (result.size <= 13) {
            transferStep(lesson, chunks, content, learner, random)?.let { result += it }
        }

        speakFree?.let { result += it }
        grammar?.let { result += it }
        selfCheck?.let { result += it }
        // The round stays inside the advertised frame. Closing tasks keep their place; only
        // practice steps are trimmed when the generated mix grew too long.
        val tail = listOfNotNull(speakFree, grammar, selfCheck)
        val tailIds = tail.map { it.id }.toSet()
        val body = result.filterNot { it.id in tailIds }.distinctBy { it.id }
        val room = (MAX_TASKS - tail.size).coerceAtLeast(0)
        val base = (body.take(room) + tail).distinctBy { it.id }.toMutableList()

        // A short authored core must still become a full round. Use the same task factory so
        // the added steps carry a chunk, an id and the usual options.
        var extra = 0
        while (base.size < MIN_TASKS && chunks.isNotEmpty() && extra < 32) {
            val chunk = chunks[extra % chunks.size]
            val type = if (extra % 2 == 0) "MeaningChoice" else "SpeakRepeat"
            base += task(type, chunk, lesson, content, random, 200 + extra, learner)
            extra += 1
        }
        return addUnderstandingGate(base.distinctBy { it.id }, content, random)
    }

    private fun seed(lessonId: String, globalSeed: Long, day: Long, run: Int): Long =
        globalSeed xor lessonId.hashCode().toLong() xor (day * 1_000_003L) xor (run * 7_919L)

    private fun learnerFor(lesson: Lesson, progress: Progress, day: Long): Learner {
        val profile = progress.profile
        val focus = ProfileCatalog.focusSkills(profile.reasons() + profile.interests)
        val weakest = Skills.weakest(progress.skills)
        val mistakeSkills = progress.mistakes.mapNotNull { Skill.fromId(it.skill) }
        val weights = Skill.entries.associateWith { skill ->
            var weight = 1.0
            if (skill in focus) weight += 1.8
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
            targets = AdaptivePlan.forProgress(progress, day, lesson),
            taskStats = progress.taskStats,
            mistakes = progress.mistakes.filter { it.chunkId !in lesson.chunkIds },
            weakest = weakest,
            focusSkills = focus,
            preferredPaths = Personalization.preferredPaths(profile)
        )
    }

    private fun skillForType(type: String): Skill? = TaskRegistry.skillFor(type)

    private fun pickType(
        learner: Learner,
        shaky: Boolean,
        random: Random,
        used: Set<String>
    ): String {
        // Zuerst der Bereich, dann seine Stufe. Ein Grammatikziel von 0 darf keine
        // Wortstellungsaufgabe erzeugen, nur weil ein anderer Bereich gerade leicht ist.
        val skill = weightedSkill(learner, random)
        val tier = if (shaky) 0 else learner.targets.of(skill)
        val rungs = ladder(skill)
        val order = buildList {
            add(tier)
            for (offset in 1..3) {
                add((tier - offset).coerceAtLeast(0))
                add((tier + offset).coerceAtMost(3))
            }
        }.distinct()
        val pool = order.asSequence()
            .map { rungs[it] }
            .map { rung -> rung.filter { it !in used } }
            .firstOrNull { it.isNotEmpty() }
            ?: rungs.flatten().filter { it !in used }
        val rung = pool.ifEmpty { listOf("SpeakRepeat") }
        // Kalibrierung: In dieser Stufe wird ein Typ seltener, wenn er gemessen zu leicht ist,
        // und häufiger, wenn er dort noch wackelt. Die Stufe bleibt, damit die Vielfalt bleibt.
        val weights = rung.associateWith { type ->
            when {
                taskLevel(type, learner.taskStats) > tier -> 0.4
                taskLevel(type, learner.taskStats) < tier -> 1.6
                else -> 1.0
            }
        }
        return weightedPick(weights, random)
    }

    /** Ab so vielen Versuchen wird ein Aufgabentyp kalibriert statt nur vermutet. */
    internal const val CALIBRATION_SAMPLE = 20

    /** Gemessene Stufe eines Typs; ohne Messung seine Einführungsstufe in der Leiter. */
    internal fun taskLevel(type: String, stats: Map<String, TaskStat>): Int {
        val skill = Skill.entries.firstOrNull { type in typesFor(it) } ?: return 1
        val base = ladder(skill).indexOfFirst { type in it }.coerceIn(0, 3)
        val stat = stats[type] ?: return base
        if (stat.attempts < CALIBRATION_SAMPLE) return base
        return when {
            stat.rate >= 0.85 -> (base + 1).coerceAtMost(3)
            stat.rate <= 0.50 -> (base - 1).coerceAtLeast(0)
            else -> base
        }
    }

    private fun typesFor(skill: Skill): List<String> = ladder(skill).flatten().distinct()

    /** Die Leiter eines Bereichs: unten Stützen, oben freier Abruf. */
    private fun ladder(skill: Skill): List<List<String>> = when (skill) {
        Skill.SPRECHEN -> listOf(
            listOf("SpeakRepeat", "Shadowing"),
            listOf("SpeakRepeat", "TranslateToEnglish", "WhatsAppReply", "Shadowing"),
            listOf("TranslateToEnglish", "Recall", "Paraphrase", "AnswerQuestion", "WhatsAppReply", "Shadowing"),
            listOf("Recall", "Paraphrase", "AnswerQuestion")
        )
        Skill.HOEREN -> listOf(
            listOf("ListenMeaning", "MinimalPair"),
            listOf("ListenMeaning", "ListenNumber", "TrueFalse", "MinimalPair"),
            listOf("Dictation", "UnderstandFastSpeech", "ListenNumber", "TrueFalse", "ListenMeaning", "MinimalPair"),
            listOf("Dictation", "UnderstandFastSpeech")
        )
        Skill.GRAMMATIK -> listOf(
            listOf("CorrectTheSentence", "BuildSentence"),
            listOf("GapFill", "CorrectTheSentence", "BuildSentence"),
            listOf("BuildSentence", "CorrectTheSentence", "GapFill"),
            listOf("BuildSentence", "GapFill")
        )
        Skill.BAUSTEINE -> listOf(
            listOf("MeaningChoice", "TranslateToGerman"),
            listOf("MeaningChoice", "TranslateToGerman", "OddOneOut"),
            listOf("OddOneOut", "TranslateToGerman", "MeaningChoice"),
            listOf("OddOneOut", "TranslateToGerman")
        )
        // Verknüpfen hat seinen eigenen Schritt und wird hier nicht gezogen.
        Skill.VERKNUEPFEN -> List(4) { listOf("ChooseConnector") }
    }

    private fun weightedSkill(learner: Learner, random: Random): Skill {
        val entries = learner.weights.entries
            .filter { it.key != Skill.VERKNUEPFEN }
            .shuffled(random)
        val total = entries.sumOf { it.value }
        if (total <= 0.0) return entries.first().key
        var roll = random.nextDouble() * total
        for (entry in entries) {
            roll -= entry.value
            if (roll <= 0.0) return entry.key
        }
        return entries.last().key
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
            "Shadowing" -> Step(
                id = id,
                type = type,
                title = "Im Tempo mitsprechen",
                promptDe = "Hör zu und sprich gleichzeitig mit – so nah am Tempo wie du kommst.",
                target = chunk.en,
                accept = listOf(chunk.en),
                model = chunk.en,
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why
            )
            "Paraphrase" -> Step(
                id = id,
                type = type,
                title = "Anders sagen",
                promptDe = "Sag „${chunk.de}“ mit anderen Worten. Der Sinn bleibt, die Wörter ändern sich.",
                target = chunk.en,
                accept = listOf(chunk.en),
                model = chunk.en,
                chunkId = chunk.id,
                tip = "Nicht derselbe Satz – derselbe Gedanke.",
                why = why
            )
            "AnswerQuestion" -> Step(
                id = id,
                type = type,
                title = "Antworten",
                promptDe = "Jemand fragt nach „${chunk.de}“. Antworte in einem ganzen Satz.",
                target = chunk.en,
                accept = listOf(chunk.en),
                model = chunk.en,
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why
            )
            "WhatsAppReply" -> Step(
                id = id,
                type = type,
                title = "Kurz antworten",
                promptDe = "Eine kurze Nachricht: „${chunk.de}“. Ein Satz genügt.",
                target = chunk.en,
                accept = listOf(chunk.en),
                model = chunk.en,
                chunkId = chunk.id,
                tip = chunk.tip,
                why = why
            )
            "UnderstandFastSpeech" -> Step(
                id = id,
                type = type,
                title = "Schnell gesprochen",
                promptDe = "Hör den Satz im normalen Tempo und wähle die Bedeutung.",
                target = chunk.en,
                options = meaningOptions(chunk, content, random),
                model = chunk.de,
                chunkId = chunk.id,
                explanationDe = chunk.tip,
                why = why
            )
            "TranslateToGerman" -> Step(
                id = id,
                type = type,
                title = "Zurückübersetzen",
                promptDe = "Welche englische Fassung passt zu „${chunk.de}“?",
                promptEn = chunk.de,
                target = chunk.en,
                options = englishOptions(chunk, content, random),
                model = chunk.en,
                chunkId = chunk.id,
                explanationDe = chunk.tip,
                why = why
            )
            "TrueFalse" -> trueFalseTask(chunk, content, id, random, why)
                ?: task("MeaningChoice", chunk, lesson, content, random, index, learner).copy(why = why)
            "CorrectTheSentence" -> correctSentenceTask(chunk, id, random, why)
                ?: task("BuildSentence", chunk, lesson, content, random, index, learner).copy(why = why)
            "OddOneOut" -> oddOneOutTask(chunk, content, id, random, why)
                ?: task("TranslateToGerman", chunk, lesson, content, random, index, learner).copy(why = why)
            "MinimalPair" -> minimalPairTask(content, id, random, why)
                ?: task("ListenMeaning", chunk, lesson, content, random, index, learner).copy(why = why)
            "ListenNumber" -> listenNumberTask(chunk, content, id, random, why)
                ?: task("ListenMeaning", chunk, lesson, content, random, index, learner).copy(why = why)
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
        // Distractors come from the same pattern or level, not from any sentence in the
        // bundle. This keeps a gap-fill grammatically plausible instead of merely random.
        val family = content.chunks.filter { other ->
            other.id != chunk.id && (
                (chunk.patternId.isNotBlank() && other.patternId == chunk.patternId) ||
                    other.level == chunk.level
                )
        }
        val distractors = family
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

    /** Drei englische Fassungen: die richtige und zwei Sätze aus demselben Pool. */
    private fun englishOptions(chunk: Chunk, content: ContentBundle, random: Random): List<Option> {
        val others = content.chunks
            .filter { it.id != chunk.id && it.en.isNotBlank() && it.en != chunk.en }
            .shuffled(random)
            .map { it.en }
            .distinct()
            .take(2)
        return (listOf(chunk.en to true) + others.map { it to false })
            .shuffled(random)
            .map { (text, correct) -> Option(text = text, correct = correct) }
    }

    private val numberWords = listOf(
        "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
        "eighteen", "nineteen", "twenty", "thirty", "forty", "fifty"
    )

    private fun numberIn(text: String): String? = text.lowercase()
        .split(Regex("[^a-z0-9]+"))
        .firstOrNull { it.toIntOrNull() != null || it in numberWords }

    /** Eine Zahl aus dem Satz heraushören – nur wenn der Satz wirklich eine enthält. */
    private fun listenNumberTask(
        chunk: Chunk,
        content: ContentBundle,
        id: String,
        random: Random,
        why: String
    ): Step? {
        val source = if (numberIn(chunk.en) != null) chunk
        else content.chunks.filter { numberIn(it.en) != null }.randomOrNull(random) ?: return null
        val answer = numberIn(source.en) ?: return null
        val others = numberWords.filter { it != answer }.shuffled(random).take(3)
        if (others.size < 3) return null
        val options = (listOf(answer to true) + others.map { it to false })
            .shuffled(random)
            .map { (text, correct) -> Option(text = text, correct = correct) }
        return Step(
            id = id,
            type = "ListenNumber",
            title = "Welche Zahl hörst du?",
            promptDe = "Hör den Satz und wähle die Zahl, die vorkommt.",
            target = source.en,
            options = options,
            model = answer,
            chunkId = source.id,
            tip = source.tip,
            why = why
        )
    }

    /** Zwei Wörter, ein Lautunterschied: die reine Hörübung. */
    private fun minimalPairTask(
        content: ContentBundle,
        id: String,
        random: Random,
        why: String
    ): Step? {
        val pair = content.minimalPairs.randomOrNull(random) ?: return null
        val answer = if (random.nextBoolean()) pair.a else pair.b
        val options = listOf(
            Option(text = pair.a, correct = pair.a == answer),
            Option(text = pair.b, correct = pair.b == answer)
        ).shuffled(random)
        return Step(
            id = id,
            type = "MinimalPair",
            title = "Welches Wort hörst du?",
            promptDe = "Zwei Wörter klingen ähnlich. Hör genau hin.",
            target = answer,
            options = options,
            model = answer,
            tip = pair.tipDe,
            why = why
        )
    }

    /** Drei Bedeutungen aus einer Situation, eine aus einer anderen. */
    private fun oddOneOutTask(
        chunk: Chunk,
        content: ContentBundle,
        id: String,
        random: Random,
        why: String
    ): Step? {
        val family = content.chunks.filter { other ->
            other.id != chunk.id && other.de.isNotBlank() && other.tags.any { it in chunk.tags }
        }
        val foreign = content.chunks.filter { other ->
            other.id != chunk.id && other.de.isNotBlank() && other.tags.none { it in chunk.tags }
        }
        val own = family.map { it.de }.distinct().shuffled(random).take(3).toMutableList()
        val odd = foreign.map { it.de }.distinct().shuffled(random)
            .firstOrNull { it !in own } ?: return null
        own.add(odd)
        val options = own.shuffled(random)
            .map { text -> Option(text = text, correct = text == odd) }
        return Step(
            id = id,
            type = "OddOneOut",
            title = "Was passt nicht?",
            promptDe = "Drei Sätze gehören zur selben Situation, einer nicht. Welcher?",
            target = odd,
            options = options,
            model = odd,
            explanationDe = "Die anderen drei kommen aus einer gemeinsamen Situation.",
            why = why
        )
    }

    /** Zwei Fassungen, eine mit vertauschten Wörtern – die richtige steht daneben. */
    private fun correctSentenceTask(
        chunk: Chunk,
        id: String,
        random: Random,
        why: String
    ): Step? {
        val words = chunk.en.trim().split(Regex(" +")).filter { it.isNotBlank() }
        if (words.size < 4) return null
        val index = random.nextInt(words.size - 1)
        val swapped = words.toMutableList().also { list ->
            val first = list[index]
            list[index] = list[index + 1]
            list[index + 1] = first
        }.joinToString(" ")
        if (swapped == chunk.en) return null
        val options = listOf(
            Option(text = chunk.en, correct = true),
            Option(text = swapped, correct = false)
        ).shuffled(random)
        return Step(
            id = id,
            type = "CorrectTheSentence",
            title = "Welcher Satz ist richtig?",
            promptDe = "Nur eine Fassung ist richtig. Achte auf die Wortstellung.",
            options = options,
            target = chunk.en,
            model = chunk.en,
            chunkId = chunk.id,
            explanationDe = chunk.de,
            why = why
        )
    }

    /** Hören und prüfen: bedeutet der Satz wirklich das, was dasteht? */
    private fun trueFalseTask(
        chunk: Chunk,
        content: ContentBundle,
        id: String,
        random: Random,
        why: String
    ): Step? {
        val other = content.chunks
            .filter { it.id != chunk.id && it.de.isNotBlank() && it.de != chunk.de }
            .randomOrNull(random)?.de ?: return null
        val truthful = random.nextBoolean()
        val meaning = if (truthful) chunk.de else other
        val options = listOf(
            Option(text = "Richtig", correct = truthful),
            Option(text = "Falsch", correct = !truthful)
        )
        return Step(
            id = id,
            type = "TrueFalse",
            title = "Stimmt die Bedeutung?",
            promptDe = "Hör den Satz. Stimmt diese Bedeutung: „$meaning“?",
            target = chunk.en,
            options = options,
            model = chunk.de,
            chunkId = chunk.id,
            explanationDe = chunk.tip,
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

    private fun whyFor(skill: Skill?, learner: Learner): String {
        if (skill == null) return ""
        val note = learner.targets.noteFor(skill)
        return when {
            note.isNotEmpty() -> note
            skill == learner.weakest ->
                "${skill.tab} ist gerade dein schwächster Bereich – deshalb kommt diese Aufgabe."
            skill in learner.focusSkills ->
                "Passt zu deiner Auswahl beim Start: ${skill.tab}."
            else -> ""
        }
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
        val words = connector.words.split(" · ").map { it.trim() }.filter { it.isNotBlank() }
        val correctWord = words.firstOrNull { word ->
            Regex("\\b${Regex.escape(word.lowercase())}\\b").containsMatchIn(example.b.lowercase())
        } ?: words.firstOrNull().orEmpty()
        return base.copy(
            id = "${lesson.id}_mix_connector",
            promptEn = example.a,
            target = example.b,
            model = example.b,
            options = words.map { word ->
                Option(text = word, correct = word.equals(correctWord, ignoreCase = true))
            },
            why = why
        )
    }

    /**
     * Echte Fehler aus anderen Lektionen, mit dem, was damals gesagt wurde. Ab zwei offenen
     * Fehlern kommen beide zurück; passt eine Fehlerkarte zur Diagnose, wird sie zum Anker
     * der Aufgabe statt nur einer Wiederholung.
     */
    private fun mistakeSteps(
        lesson: Lesson,
        content: ContentBundle,
        learner: Learner,
        random: Random
    ): List<Step> {
        val limit = if (learner.mistakes.size >= 2) 2 else 1
        val originals = learner.mistakes.sortedByDescending { it.day }.take(limit).mapIndexedNotNull { position, mistake ->
            val chunk = content.chunks.firstOrNull { it.id == mistake.chunkId } ?: return@mapIndexedNotNull null
            val origin = content.lessons.firstOrNull { it.id == mistake.lessonId }
                ?: originLesson(chunk.id, content)
            val card = errorCardFor(mistake, content)
            val skill = Skill.fromId(mistake.skill)
            val type = when {
                card != null -> "CorrectTheSentence"
                skill == Skill.HOEREN -> if (learner.targets.of(skill) >= 2) "Dictation" else "ListenMeaning"
                skill == Skill.GRAMMATIK -> if (learner.targets.of(skill) >= 2) "BuildSentence" else "CorrectTheSentence"
                skill == Skill.SPRECHEN -> if (learner.targets.of(skill) >= 2) "Recall" else "SpeakRepeat"
                skill == Skill.BAUSTEINE -> "MeaningChoice"
                else -> "TranslateToEnglish"
            }
            val base = task(type, chunk, lesson, content, random, 90 + position, learner)
            val note = buildString {
                append("In „${origin?.title ?: "einer früheren Lektion"}“ ")
                if (mistake.heard.isNotBlank()) {
                    append("war deine letzte Antwort „${mistake.heard.take(60)}“ noch nicht sicher. ")
                } else {
                    append("war dieser Satz noch wackelig. ")
                }
                append("Jetzt kommt er zurück.")
                if (card != null) append(" Dazu passt die Erklärung „${card.title}“.")
            }
            base.copy(
                id = "${lesson.id}_mix_recall" + if (position == 0) "" else "${position + 1}",
                title = "Wieder getroffen",
                errorCardId = card?.id.orEmpty(),
                originLessonId = origin?.id.orEmpty(),
                originTitle = origin?.title.orEmpty(),
                originNote = note,
                why = card?.let { "Dieser Fehler kommt wieder – ${it.anchor}." }
                    ?: "Dieser Satz war zuletzt noch nicht sicher."
            )
        }
        // Derselbe Fehler in einem neuen Satz: Erst der Transfer macht aus Wiedererkennen Können.
        // Nicht nur der jüngste Fehler: Der erste, für den es einen zweiten Satz gibt, wird
        // transferiert. Sonst bliebe ein Einzelsatz-Muster davor und der Transfer fiele aus.
        val transfer = learner.mistakes.sortedByDescending { it.day }.asSequence().mapNotNull { mistake ->
            val chunk = content.chunks.firstOrNull { it.id == mistake.chunkId } ?: return@mapNotNull null
            val pattern = mistake.patternId.ifBlank { chunk.patternId }
            if (pattern.isBlank()) return@mapNotNull null
            val variant = content.chunks.firstOrNull {
                it.id != chunk.id && it.patternId == pattern && it.id !in lesson.chunkIds
            } ?: return@mapNotNull null
            val skill = Skill.fromId(mistake.skill)
            val type = if (learner.targets.of(skill) >= 2) "TranslateToEnglish" else "MeaningChoice"
            val origin = content.lessons.firstOrNull { it.id == mistake.lessonId }
            task(type, variant, lesson, content, random, 93, learner).copy(
                id = "${lesson.id}_mix_pattern",
                title = "Dasselbe Muster, neuer Satz",
                why = "Das Muster kommt in einem neuen Zusammenhang zurück.",
                originLessonId = origin?.id.orEmpty(),
                originTitle = origin?.title.orEmpty(),
                originNote = "Gleiches Muster, anderer Satz – so wird aus Wiedererkennen Können.",
                // Richtig gelöst heißt: Der alte Fehler ist eingelöst und kommt nicht wieder.
                resolvesMistake = mistake.chunkId
            )
        }.firstOrNull()
        // Der Transfer steht vor den Wiederholungen: Er ist der neue Wert und darf nicht als
        // letzter Schritt aus einer vollen Runde herausfallen.
        return listOfNotNull(transfer) + originals
    }

    /** Die Karte, die zur tatsächlichen Abweichung passt – nur dann ist sie ein Anker. */
    private fun errorCardFor(mistake: Mistake, content: ContentBundle): ErrorCard? {
        if (mistake.target.isBlank() || mistake.heard.isBlank()) return null
        val rule = ErrorDiagnosis.diagnose(mistake.target, mistake.heard)
        if (rule.isBlank()) return null
        // Die Diagnose liefert die Karten-ID; „rule“ ist nur der kurze Merksatz-Schlüssel.
        return content.errorCards.firstOrNull { it.id == rule }
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
        val type = ladder(weakest)[learner.targets.of(weakest).coerceIn(0, 3)].firstOrNull()
            ?: return null
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
    fun isSpeaking(type: String): Boolean = TaskRegistry.isSpeaking(type)

    /** Die tatsächliche Länge dieser Runde – für „Schritt 4 von 11“ außerhalb der Lektion. */
    fun stepCount(lesson: Lesson, content: ContentBundle, progress: Progress, day: Long): Int =
        steps(lesson, content, progress, day).size
}
