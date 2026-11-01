package de.sprachgefuehl.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorTest {

    private val content = TestContent.bundle

    private val knownStepTypes = setOf(
        "DialogInput", "ComprehensionDeep", "Noticing", "ChunkIntro", "SpeakRepeat",
        "SpeakFillSlot", "BuildSentence", "ChooseConnector", "SpeakFree", "GrammarSpot",
        "TransferTask", "SelfCheck", "Recall", "MinimalPair", "Shadowing", "ListenNumber",
        "UnderstandFastSpeech", "WhatsAppReply", "Paraphrase", "ConnectClauses",
        "TranslateToEnglish", "RepairTask"
    )

    @Test
    fun `content is large enough for a full course`() {
        assertTrue("mindestens 12 Situationen", content.situations.count { it.pathId != "werkstatt" } >= 12)
        assertTrue("mindestens 36 Lektionen", content.lessons.size >= 36)
        assertTrue("mindestens 60 Chunks", content.chunks.size >= 60)
        assertTrue("mindestens 20 Fehlerkarten", content.errorCards.size >= 20)
        assertTrue("12 Grammatik-Säulen", content.pillars.size >= 12)
        assertTrue("5 Konnektor-Stufen", content.connectorLevels.size >= 5)
        assertTrue("8 Funktionskarten", content.functions.size >= 8)
        assertEquals(6, content.placement.size)
    }

    @Test
    fun `all ids are unique`() {
        val ids = mutableListOf<String>()
        ids += content.lessons.map { it.id }
        ids += content.chunks.map { it.id }
        ids += content.patterns.map { it.id }
        ids += content.errorCards.map { it.id }
        ids += content.situations.map { it.id }
        ids += content.pillars.map { it.id }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue("Doppelte IDs: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `every situation brings at least two stories`() {
        val real = content.situations.filter { !it.id.startsWith("ws_") }
        assertTrue("mindestens 24 Situationen", real.size >= 24)
        real.forEach { situation ->
            assertTrue("${situation.id} hat zu wenige Varianten", situation.variants.size >= 2)
            situation.variants.forEach { variant ->
                assertTrue("${variant.id} ohne Geschichte", variant.story.isNotBlank())
                assertTrue("${variant.id} ohne Gruß", variant.greetingEn.isNotBlank())
                assertTrue(
                    "${variant.id} mit unbekannter Figur",
                    content.personas.any { it.id == variant.personaId }
                )
            }
        }
        val ids = real.flatMap { situation -> situation.variants.map { it.id } }
        assertEquals("doppelte Varianten-IDs", ids.size, ids.distinct().size)
    }

    @Test
    fun `the standort check covers every skill on the lower levels`() {
        assertTrue("mindestens 40 Prüfaufgaben", content.checkup.size >= 40)
        listOf("hoeren", "grammatik", "bausteine", "sprechen", "verknuepfen").forEach { skill ->
            listOf(0, 1, 2).forEach { level ->
                val count = content.checkup.count { it.skill == skill && it.level == level }
                assertTrue("$skill Stufe $level hat nur $count Aufgaben", count >= 3)
            }
        }
        content.checkup.forEach { item ->
            assertTrue("${item.id} ohne Aufgabe", item.promptDe.isNotBlank())
            if (item.type == "speak") {
                assertTrue("${item.id} ohne Modell", item.model.isNotBlank())
            } else {
                assertTrue("${item.id} mit ungültigem Index", item.answer in item.options.indices)
            }
        }
    }

    @Test
    fun `a third encounter recycles an earlier chunk`() {
        val recycled = content.lessons.count { lesson ->
            lesson.steps.any { it.title == "Wieder getroffen" && it.chunkId.isNotBlank() }
        }
        assertTrue("zu wenige Lektionen mit Wiederaufnahme: $recycled", recycled >= 20)
    }

    @Test
    fun `every lesson has a speaking task and a grammar anchor`() {
        content.lessons.forEach { lesson ->
            assertTrue("${lesson.id} hat keine Sprechaufgabe", lesson.steps.any {
                it.type in setOf("SpeakRepeat", "SpeakFree", "SpeakFillSlot", "TransferTask")
            })
            assertTrue("${lesson.id} hat zu wenige Schritte", lesson.steps.size >= 6)
            assertTrue("${lesson.id} hat keine Grammatik-Säule", lesson.pillarId.isNotBlank())
            assertTrue("${lesson.id} hat kein Ziel", lesson.goal.isNotBlank())
            assertTrue(
                "${lesson.id} verweist auf unbekannte Säule ${lesson.pillarId}",
                content.pillars.any { it.id == lesson.pillarId }
            )
        }
    }

    @Test
    fun `steps are well formed`() {
        content.lessons.flatMap { it.steps }.forEach { step ->
            assertTrue("Unbekannter Typ ${step.type}", step.type in knownStepTypes)
            assertTrue("Schritt ${step.id} hat keinen Titel", step.title.isNotBlank())
            when (step.type) {
                "SpeakRepeat", "SpeakFree", "TransferTask", "SpeakFillSlot", "MinimalPair" ->
                    assertTrue("${step.id} ohne Ziel", step.target.isNotBlank() || step.model.isNotBlank())
                "GrammarSpot" -> assertTrue("${step.id} ohne Optionen", step.options.size >= 2)
                "ComprehensionDeep" -> assertTrue("${step.id} ohne Fragen", step.questions.isNotEmpty())
                "DialogInput" -> assertTrue("${step.id} ohne Dialog", step.lines.isNotEmpty())
                "ChunkIntro" -> assertTrue("${step.id} ohne Chunk", step.chunkId.isNotBlank())
            }
            if (step.chunkId.isNotBlank()) {
                assertTrue("${step.id} verweist auf unbekannten Chunk", content.chunks.any { it.id == step.chunkId })
            }
        }
    }

    @Test
    fun `chunks and patterns are connected`() {
        content.chunks.forEach { chunk ->
            assertTrue("${chunk.id} ohne Deutsch", chunk.de.isNotBlank())
            assertTrue("${chunk.id} ohne Englisch", chunk.en.isNotBlank())
            if (chunk.patternId.isNotBlank()) {
                assertTrue("${chunk.id} verweist auf unbekanntes Muster", content.patterns.any { it.id == chunk.patternId })
            }
        }
        content.patterns.forEach { pattern ->
            assertTrue("${pattern.id} ohne Bausteine", pattern.chunks.isNotEmpty())
            assertTrue("${pattern.id} ohne deutsche Bedeutung", pattern.de.isNotBlank())
            pattern.chunks.forEach { id ->
                assertTrue("${pattern.id} verweist auf unbekannten Chunk $id", content.chunks.any { it.id == id })
            }
        }
    }

    @Test
    fun `every error card explains all six parts`() {
        content.errorCards.forEach { card ->
            assertTrue("${card.id} ohne Beobachtung", card.observation.isNotBlank())
            assertTrue("${card.id} ohne Begründung", card.whyDe.isNotBlank())
            assertTrue("${card.id} ohne richtigen Satz", card.correctEn.isNotBlank())
            assertTrue("${card.id} ohne Merkanker", card.anchor.isNotBlank())
            assertTrue("${card.id} ohne nächsten Schritt", card.nextStep.isNotBlank())
            assertTrue("${card.id} ohne Checkliste", card.checklist.size >= 2)
            assertTrue("${card.id} mit unbekannter Säule", content.pillars.any { it.id == card.pillarId })
        }
    }

    @Test
    fun `can do statements point at real lessons`() {
        assertTrue(content.canDo.isNotEmpty())
        content.canDo.forEach { item ->
            assertTrue("${item.id} verweist auf unbekannte Lektion", content.lessons.any { it.id == item.lessonId })
            assertTrue("${item.id} ohne Text", item.text.isNotBlank())
        }
    }

    @Test
    fun `connector levels and function cards are complete`() {
        content.connectorLevels.forEach { level ->
            assertTrue("${level.id} ohne Wörter", level.words.isNotBlank())
            assertTrue("${level.id} ohne Erklärung", level.explanationDe.isNotBlank())
            assertTrue("${level.id} ohne Kontrast", level.contrastDe.isNotBlank())
            assertTrue("${level.id} ohne Beispiele", level.examples.size >= 2)
        }
        content.functions.forEach { function ->
            assertTrue("${function.id} ohne Titel", function.title.isNotBlank())
            assertTrue("${function.id} ohne Wendungen", function.phrases.size >= 2)
        }
    }

    @Test
    fun `placement items have a valid answer index`() {
        content.placement.forEach { item ->
            assertTrue("${item.id} ohne Audio", item.audioText.isNotBlank())
            assertTrue("${item.id} mit ungültigem Index", item.answer in item.options.indices)
            assertTrue("${item.id} mit zu wenigen Optionen", item.options.size >= 2)
        }
    }
}
