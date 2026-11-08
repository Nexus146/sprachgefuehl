package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.SessionLog
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Ein Update darf nie mit dem alten Speicherstand kollidieren: fehlende Felder bekommen ihre
 * Defaults, neue Funktionen rechnen aus dem Vorhandenen, und eine kaputte Datei fällt auf die
 * letzte gute Fassung zurück.
 */
class ProgressCompatibilityTest {

    /** Legt eine Datei mit genau diesem Inhalt an und gibt den Store dazu zurück. */
    private fun storeWith(text: String?): Pair<ProgressStore, File> {
        val file = File.createTempFile("sg-progress", ".json")
        if (text == null) file.delete() else file.writeText(text)
        return ProgressStore(file) to file
    }

    private val document14 = """
        {
          "onboarded": true,
          "contentVersion": 12,
          "placementLevel": 2,
          "sessions": [
            {"day": 20754, "minutes": 9, "speakingTasks": 3, "newChunks": 2},
            {"day": 20755, "minutes": 11, "speakingTasks": 4, "newChunks": 1},
            {"day": 20756, "minutes": 8, "speakingTasks": 2, "newChunks": 3}
          ],
          "gardenRounds": 12,
          "skills": {
            "hoeren": {"rating": 78.0, "observations": 12},
            "bausteine": {"rating": 88.0, "observations": 15}
          }
        }
    """.trimIndent()

    @Test
    fun `a 1_4 document without newer fields opens with all its values`() {
        val (store, file) = storeWith(document14)
        try {
            val progress = store.load()
            assertTrue(progress.onboarded)
            assertEquals(3, progress.sessions.size)
            assertEquals(28, progress.totalMinutes)
            assertEquals(12, progress.gardenRounds)
            assertEquals(78.0, progress.skills.hoeren.rating, 0.001)
            assertEquals(15, progress.skills.bausteine.observations)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `the garden grows retroactively from a 1_4 document`() {
        val (store, file) = storeWith(document14)
        try {
            val progress = store.load()
            val beds = Garden.beds(progress).associateBy { it.skill }
            assertEquals("Knospe", beds.getValue(Skill.HOEREN).note)
            assertEquals("Blüte", beds.getValue(Skill.BAUSTEINE).note)
            assertEquals("Wartet auf dich", beds.getValue(Skill.SPRECHEN).note)
            assertEquals(3, Garden.chronicle(progress).size)
            assertEquals("8 Minuten · 2 Sätze gesprochen · 3 neu", Garden.chronicle(progress).first())
            assertTrue(Garden.residents(progress).first { it.id == "biene" }.present)
            assertTrue(Garden.residents(progress).first { it.id == "igel" }.present)
            assertFalse(Garden.residents(progress).first { it.id == "amsel" }.present)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `the very first document version still opens`() {
        val (store, file) = storeWith("""{"onboarded":true}""")
        try {
            val progress = store.load()
            assertTrue(progress.onboarded)
            assertTrue(progress.sessions.isEmpty())
            assertEquals(0, progress.gardenRounds)
            assertTrue(Garden.beds(progress).all { it.stage == 0 })
            assertTrue(Garden.beds(progress).all { it.note == "Wartet auf dich" })
            assertTrue(Garden.chronicle(progress).isEmpty())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `unknown fields from a newer version are ignored`() {
        val (store, file) = storeWith(
            """{"onboarded":true,"gardenMood":"sonnig","skills":{"hoeren":{"rating":10.0,"observations":1,"farbe":"gruen"}}}"""
        )
        try {
            val progress = store.load()
            assertTrue(progress.onboarded)
            assertEquals(10.0, progress.skills.hoeren.rating, 0.001)
        } finally {
            file.delete()
        }
    }

    private fun twoSaves(): Pair<ProgressStore, File> {
        val (store, file) = storeWith(null)
        store.save(Progress(onboarded = true, sessions = listOf(SessionLog(1, 10, 4, 2))))
        store.save(
            Progress(
                onboarded = true,
                sessions = listOf(SessionLog(1, 10, 4, 2), SessionLog(2, 5, 1, 1))
            )
        )
        return store to file
    }

    @Test
    fun `a damaged file falls back to the last good state`() {
        val (_, file) = twoSaves()
        try {
            file.writeText("{ kaputt")
            val restored = ProgressStore(file).load()
            assertTrue(restored.onboarded)
            assertEquals(1, restored.sessions.size)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a damaged file without a backup starts empty instead of crashing`() {
        val (_, file) = storeWith("{ kaputt")
        try {
            val progress = ProgressStore(file).load()
            assertFalse(progress.onboarded)
            assertTrue(progress.sessions.isEmpty())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a save after the fallback keeps the good backup`() {
        val (_, file) = twoSaves()
        try {
            file.writeText("{ kaputt")
            val store = ProgressStore(file)
            val restored = store.load()
            assertEquals(1, restored.sessions.size)
            store.save(restored.copy(sessions = restored.sessions + SessionLog(3, 4, 1, 1)))
            assertEquals(2, ProgressStore(file).load().sessions.size)
            val backup = ProgressStore(File(file.parentFile, file.name + ".bak"))
            assertEquals(1, backup.load().sessions.size)
        } finally {
            file.delete()
        }
    }
}
