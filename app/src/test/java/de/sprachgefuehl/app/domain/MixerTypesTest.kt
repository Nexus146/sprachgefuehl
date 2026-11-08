package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Der Mixer soll nicht nur denselben Satz anders verpacken, sondern verschiedene Aufgabenformen
 * erzeugen. Jede Form muss dabei eine gültige Lösung und – wo es sie gibt – ein Ziel haben.
 */
class MixerTypesTest {

    private val content = TestContent.bundle

    private fun stepsAcrossDays(): List<de.sprachgefuehl.app.data.Step> {
        val progress = Progress(seed = 42, selfRating = 2)
        // Sechzig Tage: Ein einzelner Tag zeigt nicht jede Form – das ist gerade der Punkt.
        return (0 until 60).flatMap { index ->
            val lesson = content.lessons[index % content.lessons.size]
            LessonMixer.steps(lesson, content, progress, day = 22_000L + index)
        }
    }

    @Test
    fun `the mixer reaches every planned task type`() {
        val types = stepsAcrossDays().map { it.type }.toSet()
        val expected = setOf(
            "SpeakRepeat", "TranslateToEnglish", "BuildSentence", "MeaningChoice", "ListenMeaning",
            "GapFill", "Dictation", "Recall", "ChooseConnector",
            "Shadowing", "Paraphrase", "AnswerQuestion", "WhatsAppReply",
            "ListenNumber", "MinimalPair", "UnderstandFastSpeech",
            "OddOneOut", "CorrectTheSentence", "TrueFalse", "TranslateToGerman",
            "WhichReply", "FinishTheLine"
        )
        val missing = expected - types
        assertTrue("Diese Typen entstehen nie: $missing. Gefunden: $types", missing.isEmpty())
    }

    @Test
    fun `every generated choice has exactly one answer and real options`() {
        val choiceTypes = setOf(
            "MeaningChoice", "ListenMeaning", "GapFill", "UnderstandFastSpeech",
            "TranslateToGerman", "TrueFalse", "CorrectTheSentence", "OddOneOut",
            "MinimalPair", "ListenNumber", "ChooseConnector", "WhichReply", "FinishTheLine"
        )
        stepsAcrossDays().filter { it.type in choiceTypes }.forEach { step ->
            assertTrue("${step.type} ${step.id}: keine Optionen", step.options.size >= 2)
            assertTrue(
                "${step.type} ${step.id}: ${step.options.count { it.correct }} Lösungen",
                step.options.count { it.correct } == 1
            )
            assertTrue("${step.type} ${step.id}: leere Option", step.options.all { it.text.isNotBlank() })
            assertTrue("${step.type} ${step.id}: doppelte Option", step.options.map { it.text }.distinct().size == step.options.size)
        }
    }

    @Test
    fun `every speaking task names what to say`() {
        val speaking = setOf(
            "SpeakRepeat", "Shadowing", "Paraphrase", "AnswerQuestion", "WhatsAppReply",
            "TranslateToEnglish", "Recall"
        )
        stepsAcrossDays().filter { it.type in speaking }.forEach { step ->
            assertTrue("${step.type} ${step.id}: kein Ziel", step.target.isNotBlank())
            assertTrue("${step.type} ${step.id}: keine Akzeptanz", step.accept.isNotEmpty())
            assertTrue("${step.type} ${step.id}: kein Chunk", step.chunkId.isNotBlank())
        }
    }

    @Test
    fun `a mistake brings its matching error card as anchor`() {
        val foreign = content.chunks.first { chunk ->
            content.lessons.any { chunk.id.startsWith(it.id + "_") } &&
                chunk.id !in content.lessons.first().chunkIds
        }
        val lesson = content.lessons.first { it.chunkIds.size >= 2 }
        val progress = Progress(
            seed = 9,
            selfRating = 2,
            mistakes = listOf(
                de.sprachgefuehl.app.data.Mistake(
                    chunkId = foreign.id,
                    lessonId = "lesson-x",
                    target = "I have lived here since 2020.",
                    heard = "I live here since 2020",
                    day = 22_000,
                    skill = "grammatik"
                )
            )
        )
        val step = LessonMixer.steps(lesson, content, progress, day = 22_000)
            .firstOrNull { it.id.endsWith("_mix_recall") }
        assertTrue("Keine Fehleraufgabe", step != null)
        assertTrue("Keine Fehlerkarte als Anker", step?.errorCardId.orEmpty().isNotBlank())
        assertTrue("Falsche Karte", content.errorCards.any { it.id == step?.errorCardId })
    }

    @Test
    fun `two mistakes bring two tasks`() {
        val lesson = content.lessons.first { it.chunkIds.size >= 2 }
        val foreign = content.chunks.filter { chunk ->
            content.lessons.any { chunk.id.startsWith(it.id + "_") } && chunk.id !in lesson.chunkIds
        }.take(2)
        val progress = Progress(
            seed = 9,
            selfRating = 2,
            mistakes = foreign.mapIndexed { index, chunk ->
                de.sprachgefuehl.app.data.Mistake(
                    chunkId = chunk.id,
                    lessonId = "lesson-$index",
                    target = chunk.en,
                    heard = "",
                    day = 22_000L - index,
                    skill = "sprechen"
                )
            }
        )
        val recalls = LessonMixer.steps(lesson, content, progress, day = 22_000)
            .filter { it.id.contains("_mix_recall") }
        assertTrue("Nur ${recalls.size} Fehleraufgabe(n)", recalls.size == 2)
        assertTrue("Doppelte ID", recalls.map { it.id }.distinct().size == 2)
    }
}
