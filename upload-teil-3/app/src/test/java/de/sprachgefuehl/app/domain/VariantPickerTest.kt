package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VariantPickerTest {

    private val situations = TestContent.bundle.situations.filterNot { it.id.startsWith("ws_") }

    @Test
    fun `every situation has at least two stories`() {
        situations.forEach { situation ->
            assertTrue("${situation.id} hat nur ${situation.variants.size} Varianten", situation.variants.size >= 2)
            situation.variants.forEach { variant ->
                assertTrue("${variant.id} ohne Geschichte", variant.story.isNotBlank())
                assertTrue(
                    "${variant.id} verweist auf unbekannte Figur ${variant.personaId}",
                    TestContent.bundle.personas.any { it.id == variant.personaId }
                )
            }
        }
    }

    @Test
    fun `the same seed always gives the same story`() {
        val situation = situations.first()
        val first = VariantPicker.forSituation(situation, seed = 42L)
        val second = VariantPicker.forSituation(situation, seed = 42L)
        assertNotNull(first)
        assertEquals(first!!.id, second!!.id)
    }

    @Test
    fun `different seeds spread over the stories`() {
        val situation = situations.first()
        val picked = (1L..40L).map { VariantPicker.forSituation(situation, it)!!.id }.toSet()
        assertEquals(situation.variants.size, picked.size)
    }

    @Test
    fun `a stored choice wins over the seed`() {
        val situation = situations.first()
        val chosen = situation.variants.last()
        val resolved = VariantPicker.resolve(situation, 1L, emptySet(), chosen.id)
        assertEquals(chosen.id, resolved!!.id)
    }

    @Test
    fun `interests break the tie before the seed does`() {
        val situation = situations.first { s -> s.variants.any { it.interestId.isNotBlank() } }
        val tagged = situation.variants.first { it.interestId.isNotBlank() }
        val allowed = situation.variants.filter { it.interestId == tagged.interestId }.map { it.id }.toSet()
        repeat(10) { offset ->
            val resolved = VariantPicker.forSituation(situation, offset.toLong(), setOf(tagged.interestId))
            assertTrue("${resolved!!.id} passt nicht zum Interesse", resolved.id in allowed)
        }
        val withoutInterest = VariantPicker.forSituation(situation, 1L, emptySet())
        assertNotNull(withoutInterest)
    }

    @Test
    fun `a situation without variants stays as it is`() {
        val plain = situations.first().copy(variants = emptyList())
        assertEquals(null, VariantPicker.forSituation(plain, 1L))
    }
}
