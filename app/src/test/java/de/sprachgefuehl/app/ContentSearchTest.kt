package de.sprachgefuehl.app

import de.sprachgefuehl.app.domain.ContentSearch
import de.sprachgefuehl.app.domain.SearchKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSearchTest {

    private val content = TestContent.bundle

    @Test
    fun `finds a sentence by an English word`() {
        val hits = ContentSearch.search(content, "headache")
        assertTrue(hits.isNotEmpty())
        val sentence = hits.first { it.kind == SearchKind.SENTENCE }
        assertTrue(sentence.title.contains("headache", ignoreCase = true))
        assertEquals("arzt_l1", sentence.lessonId)
    }

    @Test
    fun `finds the same sentence by its German translation`() {
        val hits = ContentSearch.search(content, "kopfschmerzen")
        assertTrue(hits.any { it.kind == SearchKind.SENTENCE && it.title.contains("headache") })
    }

    @Test
    fun `ignores queries that are too short`() {
        assertEquals(emptyList<Any>(), ContentSearch.search(content, " "))
        assertEquals(emptyList<Any>(), ContentSearch.search(content, "a"))
    }

    @Test
    fun `respects the limit`() {
        val hits = ContentSearch.search(content, "the", limit = 5)
        assertTrue(hits.size <= 5)
    }

    @Test
    fun `every hit points at an existing lesson`() {
        val ids = content.lessons.map { it.id }.toSet()
        listOf("kaffee", "termin", "weg", "arzt", "bitte").forEach { query ->
            ContentSearch.search(content, query).forEach { hit ->
                if (hit.lessonId.isNotBlank()) assertTrue("${hit.title} → ${hit.lessonId}", hit.lessonId in ids)
            }
        }
    }

    @Test
    fun `does not return the same title twice`() {
        val hits = ContentSearch.search(content, "have", limit = 50)
        val pairs = hits.map { it.kind to it.title }
        assertEquals(pairs.size, pairs.distinct().size)
    }
}
