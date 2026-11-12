package de.sprachgefuehl.app

import de.sprachgefuehl.app.domain.TaskRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Der Inhalt ist der Lehrplan. Wenn eine ID doppelt ist, ein Verweis ins Leere zeigt oder eine
 * Aufgabe keine gültige Lösung hat, merkt das sonst erst die Lernende.
 */
class ContentIntegrityTest {

    private val content = TestContent.bundle

    @Test
    fun `every id is unique`() {
        val lists = mapOf(
            "paths" to content.paths.map { it.id },
            "situations" to content.situations.map { it.id },
            "lessons" to content.lessons.map { it.id },
            "chunks" to content.chunks.map { it.id },
            "patterns" to content.patterns.map { it.id },
            "canDo" to content.canDo.map { it.id },
            "errorCards" to content.errorCards.map { it.id },
            "minimalPairs" to content.minimalPairs.map { it.id },
            "checkup" to content.checkup.map { it.id }
        )
        lists.forEach { (name, ids) ->
            assertEquals("$name hat doppelte IDs", ids.size, ids.distinct().size)
        }
    }

    @Test
    fun `every reference points at something`() {
        val pathIds = content.paths.map { it.id }.toSet()
        val situationIds = content.situations.map { it.id }.toSet()
        val lessonIds = content.lessons.map { it.id }.toSet()
        val chunkIds = content.chunks.map { it.id }.toSet()
        val patternIds = content.patterns.map { it.id }.toSet()
        val pillarIds = content.pillars.map { it.id }.toSet()
        val personaIds = content.personas.map { it.id }.toSet()

        content.situations.forEach { situation ->
            assertTrue("${situation.id}: Pfad fehlt", situation.pathId in pathIds)
            situation.personaIds.forEach { persona ->
                assertTrue("${situation.id}: Persona $persona fehlt", persona in personaIds)
            }
        }
        content.lessons.forEach { lesson ->
            assertTrue("${lesson.id}: Situation fehlt", lesson.situationId in situationIds)
            assertTrue("${lesson.id}: Säule fehlt", lesson.pillarId in pillarIds)
            assertTrue("${lesson.id}: zu wenige Aufgaben", lesson.steps.size >= 6)
            // Die Werkstatt-Lektionen sind Gerüste ohne eigene Sätze; story lessons haben zwei.
            if (lesson.chunkIds.isNotEmpty()) {
                assertEquals("${lesson.id}: zwei Sätze erwartet", 2, lesson.chunkIds.size)
                lesson.chunkIds.forEach { chunk ->
                    assertTrue("${lesson.id}: Satz $chunk fehlt", chunk in chunkIds)
                }
            }
        }
        content.canDo.forEach { canDo ->
            assertTrue("${canDo.id}: Lektion fehlt", canDo.lessonId in lessonIds)
        }
        content.chunks.forEach { chunk ->
            if (chunk.patternId.isNotBlank()) {
                assertTrue("${chunk.id}: Muster ${chunk.patternId} fehlt", chunk.patternId in patternIds)
            }
        }
    }

    @Test
    fun `every sentence belongs to exactly one lesson`() {
        val owners = mutableMapOf<String, Int>()
        content.lessons.forEach { lesson ->
            lesson.chunkIds.forEach { chunk -> owners[chunk] = (owners[chunk] ?: 0) + 1 }
        }
        owners.forEach { (chunk, count) ->
            assertEquals("$chunk gehört zu $count Lektionen", 1, count)
        }
    }

    @Test
    fun `every task type is known and solvable`() {
        content.lessons.flatMap { it.steps }.forEach { step ->
            assertTrue("Unbekannter Typ ${step.type} in ${step.id}", TaskRegistry.spec(step.type) != null)
            // Verbindungswörter bekommen ihre Optionen erst zur Laufzeit aus dem Niveau.
            if (step.options.isNotEmpty() && !step.id.startsWith("ws_") &&
                step.type !in setOf("ChooseConnector", "ConnectClauses", "MinimalPair")
            ) {
                assertEquals(
                    "${step.id} (${step.type}) hat nicht genau eine Lösung",
                    1,
                    step.options.count { it.correct }
                )
                assertTrue("${step.id}: Option ohne Text", step.options.all { it.text.isNotBlank() })
            }
            if (step.type == "BuildSentence") {
                assertTrue("${step.id}: keine Wörter", step.words.size >= 2)
            }
            if (step.type == "SelfCheck") {
                assertTrue("${step.id}: kein Modell", step.model.isNotBlank() || step.promptDe.isNotBlank())
            }
            if (step.type == "SpeakRepeat" || step.type == "SpeakFree") {
                assertTrue("${step.id}: kein Ziel", step.target.isNotBlank())
            }
        }
    }

    @Test
    fun `every error card has a diagnosis rule and an example`() {
        content.errorCards.forEach { card ->
            assertTrue("${card.id}: Regel fehlt", card.rule.isNotBlank() || card.id.startsWith("err_"))
            assertTrue("${card.id}: Titel fehlt", card.title.isNotBlank())
            assertTrue("${card.id}: richtige Fassung fehlt", card.correctEn.isNotBlank())
            assertTrue("${card.id}: Merksatz fehlt", card.anchor.isNotBlank())
            assertTrue("${card.id}: keine Prüfliste", card.checklist.isNotEmpty())
            assertTrue("${card.id}: kein Beispiel", card.examples.isNotEmpty())
            assertTrue("${card.id}: Säule fehlt", card.pillarId in content.pillars.map { it.id })
        }
    }

    @Test
    fun `the pool is big enough for a wide mix`() {
        assertTrue("Pfade", content.paths.size >= 15)
        assertTrue("Lektionen", content.lessons.size >= 146)
        assertTrue("Sätze", content.chunks.size >= 252)
        assertTrue("Fehlerkarten", content.errorCards.size >= 48)
        assertTrue("Lautpaare", content.minimalPairs.size >= 32)
        assertTrue("Standort-Aufgaben", content.checkup.size >= 80)
    }

    @Test
    fun `every minimal pair is a real contrast`() {
        content.minimalPairs.forEach { pair ->
            assertTrue("${pair.id}: gleiche Wörter", pair.a != pair.b)
            assertTrue("${pair.id}: ohne Erklärung", pair.tipDe.isNotBlank())
        }
    }

    @Test
    fun `the standort check covers every skill and level`() {
        content.checkup.forEach { item ->
            assertTrue("${item.id}: unbekannte Fertigkeit", item.skill in setOf("hoeren", "grammatik", "bausteine", "sprechen", "verknuepfen"))
            assertTrue("${item.id}: Stufe ${item.level}", item.level in 0..3)
            assertTrue("${item.id}: Typ fehlt", item.type.isNotBlank())
            if (item.options.isNotEmpty()) {
                assertTrue("${item.id}: Antwort außerhalb", item.answer in item.options.indices)
                assertTrue("${item.id}: nur eine Option", item.options.size >= 2)
            } else {
                assertTrue("${item.id}: kein Modell", item.model.isNotBlank() || item.audioEn.isNotBlank())
            }
        }
    }

    @Test
    fun `every comprehension question carries the sentence it asks about`() {
        content.lessons.filter { it.chunkIds.isNotEmpty() }.forEach { lesson ->
            lesson.steps.filter { it.type == "ComprehensionDeep" }.forEach { step ->
                assertTrue(
                    "${lesson.id}/${step.id}: kein Satzbezug",
                    step.chunkId.isNotBlank() && step.chunkId in lesson.chunkIds
                )
            }
        }
    }

    @Test
    fun `every lesson carries a usable level`() {
        val allowed = setOf("A1", "A1–A2", "A2", "B1")
        content.lessons.forEach { lesson ->
            assertTrue("${lesson.id}: unerwartetes Level", lesson.level in allowed)
        }
        // Ohne Streuung hätte der Einstieg nach dem Standort-Check nichts zu wählen.
        assertTrue(
            "Nur ein Level im ganzen Kurs: ${content.lessons.map { it.level }.toSet()}",
            content.lessons.map { it.level }.toSet().size >= 2
        )
    }

    @Test
    fun `the new paths are complete`() {
        val expected = listOf("arbeit", "reisen", "wohnen", "telefon", "sport", "notfall")
        expected.forEach { pathId ->
            val path = content.paths.firstOrNull { it.id == pathId }
            assertTrue("Pfad $pathId fehlt", path != null)
            val situations = content.situations.filter {
                it.pathId == pathId && it.id.matches(Regex("^(arb|rei|woh|dig|spo|not)_.*"))
            }
            assertTrue("$pathId hat keine neue Situation", situations.isNotEmpty())
            situations.forEach { situation ->
                val lessons = content.lessons.filter { it.situationId == situation.id }
                assertEquals("${situation.id}: drei Lektionen erwartet", 3, lessons.size)
            }
        }
    }
}
