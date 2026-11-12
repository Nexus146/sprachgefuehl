package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.CheckupState
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.SessionLog
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProgressBackupTest {

    private fun newStore(): ProgressStore {
        val file = File.createTempFile("sprachgefuehl-progress", ".json")
        file.delete()
        return ProgressStore(file)
    }

    @Test
    fun `progress survives save and load`() {
        val store = newStore()
        val progress = Progress(
            onboarded = true,
            profile = Profile(name = "Anna", dailyMinutes = 15, reasonId = "reisen"),
            milestones = setOf("first_round"),
            sessions = listOf(SessionLog(day = 1, minutes = 10, speakingTasks = 4, newChunks = 2))
        )
        store.save(progress)
        val loaded = store.load()
        assertEquals("Anna", loaded.profile.name)
        assertEquals(15, loaded.profile.dailyMinutes)
        assertEquals("reisen", loaded.profile.reasonId)
        assertEquals(setOf("first_round"), loaded.milestones)
        assertEquals(10, loaded.minutesOn(1))
    }

    @Test
    fun `a backup round trip keeps the profile`() {
        val store = newStore()
        val progress = Progress(
            onboarded = true,
            profile = Profile(name = "Hildegard", interests = setOf("kochen", "reisen"), dailyMinutes = 5)
        )
        val decoded = store.decode(store.encode(progress))
        assertNotNull(decoded)
        assertEquals("Hildegard", decoded!!.profile.name)
        assertEquals(setOf("kochen", "reisen"), decoded.profile.interests)
        assertEquals(5, decoded.profile.dailyMinutes)
    }

    @Test
    fun `broken or foreign files are rejected`() {
        val store = newStore()
        assertNull(store.decode("das ist keine Sicherung"))
        assertNull(store.decode("{\"name\":\"fremd\"}"))
        assertNull("ohne Onboarding ist es keine gültige Sicherung", store.decode("{\"onboarded\":false}"))
    }

    @Test
    fun `older progress without profile fields still loads`() {
        val store = newStore()
        val decoded = store.decode("{\"onboarded\":true,\"placementLevel\":1,\"sessions\":[]}")
        assertNotNull(decoded)
        assertEquals("", decoded!!.profile.name)
        assertEquals(10, decoded.profile.dailyMinutes)
        assertTrue(decoded.profile.weekdays.size == 7)
    }

    @Test
    fun `an old file without skills and seed still loads`() {
        val store = newStore()
        val decoded = store.decode("{\"onboarded\":true,\"sessions\":[]}")
        assertNotNull(decoded)
        assertEquals(0, decoded!!.skills.hoeren.observations)
        assertEquals(0L, decoded.seed)
        assertTrue(decoded.variantIds.isEmpty())
        assertTrue(!decoded.checkup.finished)
    }

    @Test
    fun `skills, checkup and variants survive a backup round trip`() {
        val store = newStore()
        val progress = Progress(
            onboarded = true,
            skills = SkillProfile(
                hoeren = SkillState(72.5, 6),
                sprechen = SkillState(41.0, 3),
                measuredAt = 1234L
            ),
            checkup = CheckupState(blockIndex = 2, level = 3, answeredIds = listOf("h_0_1"), results = mapOf("h_0_1" to true)),
            seed = 99L,
            variantIds = mapOf("arzt" to "arzt_hausbesuch")
        )
        val decoded = store.decode(store.encode(progress))
        assertNotNull(decoded)
        assertEquals(72.5, decoded!!.skills.hoeren.rating, 0.001)
        assertEquals(6, decoded.skills.hoeren.observations)
        assertEquals(1234L, decoded.skills.measuredAt)
        assertEquals(2, decoded.checkup.blockIndex)
        assertEquals(true, decoded.checkup.results["h_0_1"])
        assertEquals(99L, decoded.seed)
        assertEquals("arzt_hausbesuch", decoded.variantIds["arzt"])
    }
}
