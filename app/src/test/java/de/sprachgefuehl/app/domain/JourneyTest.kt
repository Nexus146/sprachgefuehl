package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The home screen may not show 104 lessons, so the journey has to answer three questions in words:
 * which chapter, which step now, and what comes after it.
 */
class JourneyTest {

    private val content = TestContent.bundle
    private val ordered = SessionBuilder.orderedLessons(content)

    @Test
    fun `a fresh learner stands at the first chapter and sees two steps ahead`() {
        val journey = requireNotNull(JourneyPlanner.of(content, Progress()))
        val firstPath = content.paths.sortedBy { it.order }
            .first { path -> content.situations.any { it.pathId == path.id } }
        assertEquals(firstPath.title, journey.chapterTitle)
        assertEquals(1, journey.chapterIndex)
        assertEquals(0, journey.chapterDone)
        assertTrue("Kapitel ohne Lektionen: ${journey.chapterTotal}", journey.chapterTotal > 0)
        assertEquals(ordered.first().id, journey.now?.lessonId)
        assertEquals(JourneyPlanner.AHEAD, journey.ahead.size)
        assertEquals(0, journey.doneLessons)
        assertEquals(ordered.size, journey.totalLessons)
        assertTrue("Folgekapitel fehlt", journey.afterChapter.isNotBlank())
        assertEquals("Kapitel 1 von ${journey.chapterCount} · ${journey.chapterTitle}", journey.chapterLine)
    }

    @Test
    fun `a started situation wins over the plain order`() {
        // Ein angefangener Ort ist der Ort, an dem die Lernende steht – auch ohne Lektionskennung.
        val travel = ordered.first { lesson ->
            content.situations.firstOrNull { it.id == lesson.situationId }?.pathId == "unterwegs"
        }
        val journey = requireNotNull(
            JourneyPlanner.of(content, Progress(completedLessons = setOf(travel.id)))
        )
        assertEquals("Unterwegs", journey.chapterTitle)
        val suggestion = requireNotNull(Personalization.suggestion(content, Progress(completedLessons = setOf(travel.id))))
        val suggestedPath = content.situations.first { it.id == suggestion.lesson.situationId }.pathId
        assertEquals(
            "Startseite und Bereichsübersicht müssen denselben Ort nennen",
            "unterwegs",
            suggestedPath
        )
    }

    @Test
    fun `the given lesson decides the chapter`() {
        val travel = ordered.first { lesson ->
            content.situations.firstOrNull { it.id == lesson.situationId }?.pathId == "unterwegs"
        }
        val journey = requireNotNull(JourneyPlanner.of(content, Progress(), travel.id))
        assertEquals("Unterwegs", journey.chapterTitle)
        assertEquals(travel.id, journey.now?.lessonId)
        assertTrue("Kapitelnummer fehlt", journey.chapterIndex > 1)
    }

    @Test
    fun `finished lessons move the count, not the chapter order`() {
        val first = ordered.first()
        val journey = requireNotNull(
            JourneyPlanner.of(content, Progress(completedLessons = setOf(first.id)))
        )
        assertEquals(1, journey.doneLessons)
        assertTrue("Der zweite Schritt muss der nächste offene sein", journey.now?.lessonId != first.id)
    }

    @Test
    fun `a finished course still names a place instead of nothing`() {
        val all = Progress(completedLessons = ordered.map { it.id }.toSet())
        val journey = requireNotNull(JourneyPlanner.of(content, all))
        assertNotNull("Ohne Ort kein Satz", journey.now)
        assertEquals(ordered.size, journey.doneLessons)
        assertEquals(1f, journey.chapterProgress)
    }
}
