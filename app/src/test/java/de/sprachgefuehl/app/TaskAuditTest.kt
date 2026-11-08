package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.domain.AnswerKey
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.TaskRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Der große Durchgang durch die Aufgaben selbst – ohne Blick auf Inhalte.
 *
 * Geprüft wird, was eine Aufgabe ausmacht: Sagt sie, was zu tun ist? Hat sie genau eine Lösung,
 * und steht die im Schritt? Passt bei Wortstellung, Lücke und Verbindung alles zusammen? Und
 * wechseln sich die Formen in einer Runde ab, statt dreimal dasselbe zu fragen?
 */
class TaskAuditTest {

    private val content = TestContent.bundle
    private val chunks = content.chunks.associateBy { it.id }
    private val germanTexts = content.chunks.map { it.de }.filter { it.isNotBlank() }.toSet()
    private val englishTexts = content.chunks.map { it.en }.filter { it.isNotBlank() }.toSet()

    /**
     * Nur Lektionen mit Bausteinen werden gemischt. Die 20 Werkstatt-Lektionen bestehen aus
     * wenigen festen Schritten – das ist ihre Form, keine zu kurze Runde.
     */
    private val mixedLessons = content.lessons.filter { it.chunkIds.isNotEmpty() }

    private val choiceTypes = setOf(
        "MeaningChoice", "ListenMeaning", "GapFill", "UnderstandFastSpeech", "TranslateToGerman",
        "TrueFalse", "CorrectTheSentence", "OddOneOut", "MinimalPair", "ListenNumber",
        "ChooseConnector", "ConnectClauses", "GrammarSpot", "WhichReply", "FinishTheLine"
    )

    private val speakingTypes = setOf(
        "SpeakRepeat", "Shadowing", "Paraphrase", "AnswerQuestion", "WhatsAppReply",
        "TranslateToEnglish", "Recall", "SpeakFree", "TransferTask", "SpeakFillSlot"
    )

    private fun words(text: String): List<String> = text.lowercase()
        .replace(Regex("[^a-z0-9äöüß ]"), " ")
        .trim()
        .split(Regex(" +"))
        .filter { it.isNotBlank() }
        .sorted()

    private fun sentences(text: String): List<String> = text.lowercase()
        .split(Regex("[.!?]"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    private fun Quote(step: Step): String =
        Regex("[„\"]([^“\"]+)[“\"]").find(step.promptDe)?.groupValues?.get(1)?.trim().orEmpty()

    private fun round(lesson: Lesson, day: Long, run: Int = 0): List<Step> = LessonMixer.steps(
        lesson = lesson,
        content = content,
        progress = Progress(seed = 4_242, selfRating = 2, lessonRuns = mapOf(lesson.id to run)),
        day = day
    )

    private fun label(step: Step) = "${step.type} ${step.id}"

    // ---------------------------------------------------------------- Autoren-Schritte

    @Test
    fun `every authored step is a known task that says what to do`() {
        var steps = 0
        content.lessons.forEach { lesson ->
            val ids = lesson.steps.map { it.id }
            assertEquals("Doppelte Schritt-IDs in ${lesson.id}", ids.distinct().size, ids.size)
            lesson.steps.forEach { step ->
                steps++
                assertTrue("${label(step)}: unbekannter Typ", TaskRegistry.spec(step.type) != null)
                if (step.type !in setOf("DialogInput", "Noticing", "SelfCheck", "ComprehensionDeep")) {
                    assertTrue("${label(step)}: kein Titel", step.title.isNotBlank())
                    assertTrue("${label(step)}: keine deutsche Anweisung", step.promptDe.isNotBlank())
                }
                if (step.chunkId.isNotBlank()) {
                    assertTrue("${label(step)}: Chunk ${step.chunkId} fehlt", chunks.containsKey(step.chunkId))
                }
            }
        }
        assertTrue("Zu wenige Schritte geprüft: $steps", steps >= 1_900)
    }

    @Test
    fun `every authored choice offers exactly one answer`() {
        var choices = 0
        content.lessons.flatMap { it.steps }.forEach { step ->
            if (step.options.isEmpty()) return@forEach
            choices++
            assertTrue("${label(step)}: nur ${step.options.size} Optionen", step.options.size >= 2)
            // Einige Katalogformate tragen kein Häkchen; dort steht die Lösung im Zielsatz.
            val flagged = step.options.count { it.correct }
            if (flagged > 0) {
                assertEquals("${label(step)}: $flagged richtige Optionen", 1, flagged)
            } else {
                assertTrue(
                    "${label(step)}: keine Option markiert und keine ableitbare Lösung",
                    AnswerKey.correctIndex(step) >= 0
                )
            }
            assertEquals(
                "${label(step)}: doppelte Option",
                step.options.size,
                step.options.map { it.text.trim() }.distinct().size
            )
            assertTrue("${label(step)}: leere Option", step.options.all { it.text.isNotBlank() })
        }
        assertTrue("Zu wenige Auswahlaufgaben: $choices", choices >= 250)
    }

    @Test
    fun `word order tasks contain exactly the words of their sentence`() {
        var builds = 0
        content.lessons.flatMap { it.steps }.filter { it.type == "BuildSentence" }.forEach { step ->
            builds++
            assertTrue("${label(step)}: keine Wörter", step.words.isNotEmpty())
            assertEquals("${label(step)}: Wörter ergeben nicht den Satz", words(step.target), words(step.words.joinToString(" ")))
        }
        assertTrue("Zu wenige Wortstellungsaufgaben: $builds", builds >= 100)
    }

    @Test
    fun `connector and minimal pair answers really appear in their sentence`() {
        content.lessons.flatMap { it.steps }
            .filter { it.type in setOf("ChooseConnector", "ConnectClauses", "MinimalPair", "UnderstandFastSpeech") }
            .forEach { step ->
                val index = AnswerKey.correctIndex(step)
                assertTrue("${label(step)}: keine eindeutige Lösung", index >= 0)
                val answer = step.options[index].text.trim()
                assertTrue(
                    "${label(step)}: „$answer“ passt nicht zu „${step.target}“",
                    answer.equals(step.target.trim(), ignoreCase = true) ||
                        Regex("\\b${Regex.escape(answer)}\\b", RegexOption.IGNORE_CASE)
                            .containsMatchIn(step.target)
                )
            }
    }

    // ---------------------------------------------------------------- Generierte Runden

    @Test
    fun `a generated round keeps its length and repeats no step`() {
        mixedLessons.take(60).forEach { lesson ->
            (0..2).forEach { offset ->
                val steps = round(lesson, day = 22_000L + offset)
                assertTrue(
                    "${lesson.id}: ${steps.size} Schritte",
                    steps.size in LessonMixer.MIN_TASKS..LessonMixer.MAX_TASKS
                )
                assertEquals("${lesson.id}: doppelte IDs", steps.size, steps.map { it.id }.distinct().size)
                steps.forEach { step ->
                    assertTrue("${label(step)}: unbekannter Typ", TaskRegistry.spec(step.type) != null)
                }
            }
        }
    }

    @Test
    fun `a generated round mixes the formats instead of repeating one`() {
        var shortest = Int.MAX_VALUE
        mixedLessons.take(60).forEach { lesson ->
            val steps = round(lesson, day = 22_050L)
            val types = steps.map { it.type }
            val distinct = types.distinct().size
            shortest = minOf(shortest, distinct)
            assertTrue("${lesson.id}: nur $distinct verschiedene Typen in $types", distinct >= 5)
            var run = 1
            types.zipWithNext().forEach { (a, b) ->
                run = if (a == b) run + 1 else 1
                assertTrue("${lesson.id}: dreimal „$b“ hintereinander: $types", run <= 2)
            }
            assertEquals("${lesson.id}: Abschluss fehlt oder steht nicht am Ende", "SelfCheck", types.last())
            val firstPractice = steps.firstOrNull { it.id.endsWith("_mix_1") }
            assertEquals(
                "${lesson.id}: Runde beginnt nicht mit Nachsprechen",
                "SpeakRepeat",
                firstPractice?.type
            )
        }
        assertTrue("Zu wenig Abwechslung: $shortest Typen", shortest >= 5)
    }

    @Test
    fun `the same lesson looks different on another day or in another run`() {
        val lesson = mixedLessons.firstOrNull { it.chunkIds.size >= 3 } ?: mixedLessons.first()
        val first = round(lesson, day = 22_000L).map { it.type }
        val secondDay = round(lesson, day = 22_100L).map { it.type }
        val secondRun = round(lesson, day = 22_000L, run = 1).map { it.type }
        assertTrue("Gleiche Reihenfolge an zwei Tagen: $first", first != secondDay)
        assertTrue("Gleiche Reihenfolge in zwei Runden: $first", first != secondRun)
    }

    @Test
    fun `every generated choice has one answer and a way to solve it`() {
        mixedLessons.take(80).forEach { lesson ->
            val steps = round(lesson, day = 22_070L)
            steps.filter { it.type in choiceTypes }.forEach { step ->
                val index = AnswerKey.correctIndex(step)
                assertTrue("${label(step)}: keine eindeutige Lösung unter ${step.options.map { it.text }}", index >= 0)
                assertTrue("${label(step)}: nur ${step.options.size} Optionen", step.options.size >= 2)
                assertEquals(
                    "${label(step)}: ${step.options.count { it.correct }} richtige Optionen",
                    1,
                    step.options.count { it.correct }
                )
                assertEquals(
                    "${label(step)}: doppelte Option",
                    step.options.size,
                    step.options.map { it.text.trim() }.distinct().size
                )
                assertTrue("${label(step)}: leere Option", step.options.all { it.text.isNotBlank() })
            }
        }
    }

    @Test
    fun `generated options stay in the language of the question`() {
        mixedLessons.take(80).forEach { lesson ->
            round(lesson, day = 22_090L).forEach { step ->
                when (step.type) {
                    "MeaningChoice", "ListenMeaning", "UnderstandFastSpeech" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        assertTrue("${label(step)}: Lösung „$answer“ ist nicht die deutsche Bedeutung", answer in germanTexts)
                        assertTrue(
                            "${label(step)}: Option in falscher Sprache",
                            step.options.all { it.text in germanTexts }
                        )
                    }
                    "TranslateToGerman" -> assertTrue(
                        "${label(step)}: Option ist kein englischer Satz",
                        step.options.all { it.text in englishTexts }
                    )
                    "GapFill" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        val filled = step.promptEn.replace("_____", answer)
                        assertEquals("${label(step)}: Lücke ergibt nicht den Satz", words(step.target), words(filled))
                    }
                    "TrueFalse" -> {
                        val quoted = Quote(step)
                        val chunk = chunks[step.chunkId]
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        if (chunk != null && quoted.isNotBlank()) {
                            val truthful = quoted.trim() == chunk.de.trim()
                            assertEquals("${label(step)}: Richtig/Falsch passt nicht", truthful, answer == "Richtig")
                        }
                    }
                    "CorrectTheSentence" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        assertEquals("${label(step)}: Lösung ist nicht der Zielsatz", words(step.target), words(answer))
                    }
                    "ListenNumber" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        assertTrue(
                            "${label(step)}: Zahl „$answer“ kommt nicht vor",
                            Regex("\\b${Regex.escape(answer)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(step.target)
                        )
                    }
                    "ChooseConnector" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        assertTrue(
                            "${label(step)}: „$answer“ kommt im Zielsatz nicht vor",
                            Regex("\\b${Regex.escape(answer)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(step.target)
                        )
                    }
                    "OddOneOut" -> {
                        val answer = step.options[AnswerKey.correctIndex(step)].text
                        assertTrue("${label(step)}: falsche Sprache", answer in germanTexts)
                        assertTrue(
                            "${label(step)}: drei gleiche fehlen",
                            step.options.count { it.text != answer && it.text in germanTexts } >= 3
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `generated speaking tasks name a target and keep the model hidden where intended`() {
        mixedLessons.take(60).forEach { lesson ->
            round(lesson, day = 22_030L).forEach { step ->
                if (step.type in speakingTypes && step.id.contains("_mix_")) {
                    assertTrue("${label(step)}: kein Ziel", step.target.isNotBlank())
                    assertTrue("${label(step)}: keine akzeptierte Fassung", step.accept.isNotEmpty())
                    assertTrue("${label(step)}: Chunk fehlt", step.chunkId.isNotBlank())
                    if (step.type in setOf("Recall", "Dictation")) {
                        assertTrue("${label(step)}: Modell sollte verborgen sein", step.modelHidden)
                    }
                }
            }
        }
    }

    @Test
    fun `the understanding gate appears once and points at the right meaning`() {
        mixedLessons.take(60).forEach { lesson ->
            val steps = round(lesson, day = 22_060L)
            val gates = steps.filter { it.understandingOptions.isNotEmpty() }
            assertTrue("${lesson.id}: ${gates.size} Verständnisaufgaben", gates.size <= 1)
            gates.forEach { step ->
                assertEquals("${label(step)}: keine drei Möglichkeiten", 3, step.understandingOptions.size)
                assertTrue(
                    "${label(step)}: Antwortindex ${step.understandingAnswer} passt nicht",
                    step.understandingAnswer in step.understandingOptions.indices
                )
                assertEquals(
                    "${label(step)}: Index zeigt nicht auf die Bedeutung",
                    step.understandingDe,
                    step.understandingOptions[step.understandingAnswer]
                )
                assertEquals(
                    "${label(step)}: Bedeutung ist nicht die des Satzes",
                    chunks[step.chunkId]?.de,
                    step.understandingDe
                )
            }
        }
    }

    @Test
    fun `the rounds draw from all twenty-two planned formats`() {
        val seen = mixedLessons.take(60)
            .flatMap { lesson -> round(lesson, day = 22_040L) }
            .map { it.type }
            .toSet()
        val planned = setOf(
            "SpeakRepeat", "TranslateToEnglish", "BuildSentence", "MeaningChoice", "ListenMeaning",
            "GapFill", "Dictation", "Recall", "ChooseConnector", "Shadowing", "Paraphrase",
            "AnswerQuestion", "WhatsAppReply", "ListenNumber", "MinimalPair", "UnderstandFastSpeech",
            "OddOneOut", "CorrectTheSentence", "TrueFalse", "TranslateToGerman",
            "WhichReply", "FinishTheLine"
        )
        val missing = planned - seen
        assertTrue("Nie erzeugt: $missing", missing.isEmpty())
    }

    @Test
    fun `no generated step is missing its prompt`() {
        mixedLessons.take(80).forEach { lesson ->
            round(lesson, day = 22_080L).forEach { step ->
                assertTrue("${label(step)}: kein Titel", step.title.isNotBlank())
                assertTrue("${label(step)}: keine deutsche Anweisung", step.promptDe.isNotBlank())
                // Verständnisfragen tragen ihre Antwort in den Fragen, der Abschluss in seiner
                // Einschätzung – beide brauchen kein Ziel im Schritt.
                if (step.options.isEmpty() && step.questions.isEmpty() && step.type != "SelfCheck" &&
                    TaskRegistry.needsAnswer(step.type)
                ) {
                    assertTrue(
                        "${label(step)}: keine Lösung und keine Optionen",
                        step.target.isNotBlank() || step.accept.isNotEmpty()
                    )
                }
            }
        }
    }

    /** Kein Test, sondern eine Messung: Wie lang und wie bunt sind die Runden wirklich? */
    @Test
    fun `report the shape of the rounds`() {
        val rounds = mixedLessons.map { lesson -> lesson.id to round(lesson, day = 22_020L) }
        val lengths = rounds.map { it.second.size }
        val short = rounds.filter { it.second.size < LessonMixer.MIN_TASKS }.map { "${it.first}:${it.second.size}" }
        val long = rounds.filter { it.second.size > LessonMixer.MAX_TASKS }.map { "${it.first}:${it.second.size}" }
        val distinct = rounds.map { it.second.map { step -> step.type }.distinct().size }
        val counts = rounds.flatMap { it.second }.groupingBy { it.type }.eachCount().entries.sortedByDescending { it.value }
        println(
            "Runden: ${rounds.size} · Länge ${lengths.min()}–${lengths.max()} " +
                "(Ø ${lengths.average().toInt()}) · Typen pro Runde ${distinct.min()}–${distinct.max()} " +
                "(Ø ${"%.1f".format(distinct.average())})"
        )
        println("Häufigste Typen: " + counts.take(8).joinToString(", ") { "${it.key} ${it.value}" })
        assertTrue("Runden sind zu kurz: $short", short.isEmpty())
        assertTrue("Runden sind zu lang: $long", long.isEmpty())
        assertTrue("Zu wenig Vielfalt", distinct.min() >= 4)
    }
}
