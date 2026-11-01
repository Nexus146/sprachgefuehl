package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.BackupBundle
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.ProgressStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupBundleTest {

    private fun newStore(): ProgressStore =
        ProgressStore(File.createTempFile("sprachgefuehl-backup", ".json").apply { delete() })

    private fun progressJson(): String {
        val store = newStore()
        return store.encode(
            store.load().copy(
                onboarded = true,
                profile = Profile(name = "Hildegard", dailyMinutes = 15)
            )
        )
    }

    private fun recording(name: String, bytes: ByteArray): File {
        val dir = File.createTempFile("recordings", "").apply { delete(); mkdirs() }
        return File(dir, name).apply { writeBytes(bytes) }
    }

    private fun writeBundle(json: String, recordings: List<File>): ByteArray {
        val out = ByteArrayOutputStream()
        BackupBundle.write(out, json, recordings)
        return out.toByteArray()
    }

    @Test
    fun `a bundle keeps the progress and the recordings`() {
        val first = recording("rec_1.m4a", byteArrayOf(1, 2, 3, 4))
        val second = recording("rec_2.m4a", byteArrayOf(9, 8, 7))

        val content = BackupBundle.read(writeBundle(progressJson(), listOf(first, second)))
        assertNotNull(content)
        val decoded = newStore().decode(content!!.progressJson)
        assertNotNull(decoded)
        assertEquals("Hildegard", decoded!!.profile.name)
        assertEquals(15, decoded.profile.dailyMinutes)
        assertEquals(setOf("rec_1.m4a", "rec_2.m4a"), content.recordings.keys)
        assertEquals(4, content.recordings.getValue("rec_1.m4a").size)
    }

    @Test
    fun `a plain progress document from an older version still loads`() {
        val json = progressJson()
        val content = BackupBundle.read(json.toByteArray())
        assertNotNull(content)
        assertTrue(content!!.recordings.isEmpty())
        assertEquals(json, content.progressJson)
    }

    @Test
    fun `foreign files are rejected`() {
        assertNull(BackupBundle.read(ByteArray(0)))
        assertNull(BackupBundle.read("das ist keine Sicherung".toByteArray()))
        assertNull(BackupBundle.read("kein JSON".toByteArray()))
    }

    @Test
    fun `a zip without the progress document is not a backup`() {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(BackupBundle.RECORDINGS_PREFIX + "rec_1.m4a"))
            zip.write(byteArrayOf(1))
            zip.closeEntry()
        }
        assertNull(BackupBundle.read(out.toByteArray()))
    }

    @Test
    fun `entries that want to leave the recordings folder are ignored`() {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(BackupBundle.PROGRESS_ENTRY))
            zip.write(progressJson().toByteArray())
            zip.closeEntry()
            listOf("../progress.json", "unterordner/rec.m4a", "rec_ok.m4a").forEach { name ->
                zip.putNextEntry(ZipEntry(BackupBundle.RECORDINGS_PREFIX + name))
                zip.write(byteArrayOf(5, 6))
                zip.closeEntry()
            }
        }
        val content = BackupBundle.read(out.toByteArray())
        assertNotNull(content)
        assertEquals(setOf("rec_ok.m4a"), content!!.recordings.keys)
    }

    @Test
    fun `the written file is really a zip`() {
        val bytes = writeBundle(progressJson(), listOf(recording("rec_1.m4a", byteArrayOf(1))))
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries += entry.name
                zip.closeEntry()
            }
        }
        assertTrue(BackupBundle.PROGRESS_ENTRY in entries)
        assertTrue(BackupBundle.RECORDINGS_PREFIX + "rec_1.m4a" in entries)
    }
}
