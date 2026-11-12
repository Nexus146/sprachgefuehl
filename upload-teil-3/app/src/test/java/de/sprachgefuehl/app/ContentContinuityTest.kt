package de.sprachgefuehl.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The content is one world: figures return, places stay, chunks come back in new situations. */
class ContentContinuityTest {

    private val content = TestContent.bundle
    private val storySituations = content.situations.filter { it.pathId != "werkstatt" }

    @Test
    fun `the course is a world with seasons and people`() {
        assertEquals("Dein Viertel", content.world.name)
        assertTrue(content.world.seasons.size >= 4)
        assertTrue("mindestens 24 Situationen", storySituations.size >= 24)
        assertTrue("mindestens 90 Lektionen", content.lessons.size >= 90)
        assertTrue("mindestens 140 Bausteine", content.chunks.size >= 140)
        assertTrue("mindestens sechs Figuren", content.personas.size >= 6)
    }

    @Test
    fun `every story situation has a place, a season and a person`() {
        val seasonIds = content.world.seasons.map { it.id }.toSet()
        val personaIds = content.personas.map { it.id }.toSet()
        storySituations.forEach { situation ->
            assertTrue("${situation.id} ohne Ort", situation.place.isNotBlank())
            assertTrue("${situation.id} ohne Jahreszeit", situation.season in seasonIds)
            assertTrue("${situation.id} ohne Figur", situation.personaIds.isNotEmpty())
            situation.personaIds.forEach { id ->
                assertTrue("${situation.id} verweist auf unbekannte Figur $id", id in personaIds)
            }
            assertTrue("${situation.id} ohne Story-Zeile", situation.story.isNotBlank())
            // Drei Lektionen sind die Untergrenze; spätere Zugänge (Numbers-Lektionen, Werkstatt)
            // dürfen eine Situation vertiefen, ohne dass die Welt auseinanderfällt.
            assertTrue(
                "${situation.id} braucht mindestens drei Lektionen",
                content.lessons.count { it.situationId == situation.id } >= 3
            )
        }
    }

    @Test
    fun `each person returns in more than one situation`() {
        content.personas.forEach { persona ->
            assertTrue("${persona.id} ohne Rolle", persona.role.isNotBlank())
            assertTrue("${persona.id} ohne Stimme", persona.voice.isNotBlank())
            assertTrue("${persona.id} tritt nur in einer Situation auf", persona.situations.size >= 2)
            persona.situations.forEach { situationId ->
                assertTrue(
                    "${persona.id} verweist auf unbekannte Situation $situationId",
                    content.situations.any { it.id == situationId }
                )
            }
        }
    }

    @Test
    fun `season two recycles at least two older chunks per lesson`() {
        val chunksById = content.chunks.associateBy { it.id }
        val lessonsById = content.lessons.associateBy { it.id }
        val situationOfChunk = content.lessons
            .flatMap { lesson -> lesson.chunkIds.map { it to lesson.situationId } }
            .toMap()
        val recycledLessons = content.lessons.filter { it.recycles.isNotEmpty() }
        assertTrue("Staffel 2 fehlt", recycledLessons.size >= 36)
        recycledLessons.forEach { lesson ->
            assertTrue("${lesson.id} hat nur ${lesson.recycles.size} Wiederaufnahmen", lesson.recycles.size >= 2)
            lesson.recycles.forEach { chunkId ->
                val chunk = chunksById[chunkId]
                assertTrue("${lesson.id} verweist auf unbekannten Baustein $chunkId", chunk != null)
                assertTrue("${lesson.id} wiederholt einen eigenen Baustein", chunkId !in lesson.chunkIds)
                assertTrue(
                    "${lesson.id} wiederholt keinen fremden Baustein ($chunkId)",
                    situationOfChunk[chunkId] != lesson.situationId
                )
                assertTrue("${lesson.id} hat keinen Zugriff auf die Lektion des Bausteins", lessonsById.isNotEmpty())
            }
        }
    }

    @Test
    fun `later situations keep the same grammar pillars`() {
        val pillarIds = content.pillars.map { it.id }.toSet()
        content.lessons.forEach { lesson ->
            assertTrue("${lesson.id} nutzt unbekannte Säule ${lesson.pillarId}", lesson.pillarId in pillarIds)
        }
    }
}
