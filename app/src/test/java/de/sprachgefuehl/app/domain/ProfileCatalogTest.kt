package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Profile
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every question in the first start has to change something the learner can see. These tests keep
 * answers from drifting back into "nice to ask, no effect" – either an answer moves situations
 * forward, changes the stories, or it is not offered at all.
 */
class ProfileCatalogTest {

    private val content = TestContent.bundle
    private val pathIds = content.paths.map { it.id }.toSet()
    private val pathsWithSituations = content.situations.map { it.pathId }.toSet()
    private val storyTags = content.situations
        .flatMap { situation -> situation.variants.map { it.interestId } }
        .filter { it.isNotBlank() }
        .toSet()

    private val allChoices = ProfileCatalog.reasons + ProfileCatalog.interests

    @Test
    fun `every answer states what it changes`() {
        allChoices.forEach { choice ->
            assertTrue("${choice.id} hat keinen Wirkungstext", choice.effect.isNotBlank())
        }
    }

    @Test
    fun `every answer changes something real`() {
        allChoices.forEach { choice ->
            assertTrue(
                "${choice.id} hat weder Pfad noch Geschichte",
                choice.paths.isNotEmpty() || choice.storyTags.isNotEmpty()
            )
        }
    }

    @Test
    fun `every promised path exists and carries situations`() {
        allChoices.forEach { choice ->
            choice.paths.forEach { path ->
                assertTrue("${choice.id}: Pfad $path fehlt", path in pathIds)
                assertTrue("${choice.id}: Pfad $path ist leer", path in pathsWithSituations)
            }
        }
    }

    @Test
    fun `every promised story tag is written into the content`() {
        allChoices.forEach { choice ->
            choice.storyTags.forEach { tag ->
                assertTrue("${choice.id}: Geschichten-Tag $tag kommt nicht vor", tag in storyTags)
            }
        }
    }

    @Test
    fun `an interest without any effect is not offered`() {
        assertNull("nachrichten hatte keine Wirkung", ProfileCatalog.interests.firstOrNull { it.id == "nachrichten" })
    }

    @Test
    fun `moving counts as health stories`() {
        val tags = Personalization.storyTagsFor(Profile(interests = setOf("sport")))
        assertTrue("sport öffnet keine Gesundheitsgeschichten", "gesundheit" in tags)
    }

    @Test
    fun `reason and interests both reach the stories`() {
        val fromReason = Personalization.storyTagsFor(Profile(reasonId = "reisen"))
        assertTrue("reisen öffnet keine Reisegeschichten", "reisen" in fromReason)
        val both = Personalization.storyTagsFor(Profile(reasonId = "gesundheit", interests = setOf("kochen")))
        assertTrue("gesundheit" in both)
        assertTrue("kochen" in both)
    }

    @Test
    fun `the reason still moves situations forward`() {
        assertTrue("werkstatt" in Personalization.preferredPaths(Profile(reasonId = "kopf")))
        assertTrue("leute" in Personalization.preferredPaths(Profile(reasonId = "familie")))
        assertTrue(Personalization.preferredPaths(Profile()).isEmpty())
    }
}
