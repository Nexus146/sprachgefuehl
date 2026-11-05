package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Situation
import de.sprachgefuehl.app.data.SituationVariant
import kotlin.random.Random

/**
 * Picks the story a learner meets. The same seed always gives the same answer, so the world
 * stays stable between two app starts; different learners get different stories.
 *
 * [tags] are story tags, not profile ids: the profile translates its answers through
 * [ProfileCatalog.storyTags] first, so "Bewegung & Gesundheit" opens the health stories.
 */
object VariantPicker {

    /** The stored choice if there is one, otherwise the stable pick. Never writes anything. */
    fun resolve(
        situation: Situation,
        seed: Long,
        tags: Set<String>,
        chosenId: String?
    ): SituationVariant? {
        if (chosenId != null) {
            situation.variants.firstOrNull { it.id == chosenId }?.let { return it }
        }
        return forSituation(situation, seed, tags)
    }

    fun forSituation(
        situation: Situation,
        seed: Long,
        tags: Set<String> = emptySet()
    ): SituationVariant? {
        if (situation.variants.isEmpty()) return null
        val preferred = situation.variants.filter {
            it.interestId.isNotBlank() && it.interestId in tags
        }
        val pool = preferred.ifEmpty { situation.variants }
        // An explicit seed of 0 means "not chosen yet": the situation id alone keeps it stable.
        val mixed = seed xor (situation.id.hashCode().toLong() shl 1) xor 0x5F3759DF
        return pool[Random(mixed).nextInt(pool.size)]
    }
}
