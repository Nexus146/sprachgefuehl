package de.sprachgefuehl.app.account

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.RecordingRef
import de.sprachgefuehl.app.data.account.RecordingPaths
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class RecordingPathsTest {

    @Test
    fun `object names stay inside the user's folder`() {
        assertEquals("user-1/rec_123.m4a", RecordingPaths.objectName("user-1", "rec_123"))
        assertEquals("recording.m4a", RecordingPaths.fileName("../../outside"))
        assertEquals("rec_1.m4a", RecordingPaths.fileName("rec_1"))
    }

    @Test
    fun `a cloud restore rewrites absolute paths for this device`() {
        val directory = File(System.getProperty("java.io.tmpdir"), "sprachgefuehl-recordings-test")
        val progress = Progress(
            onboarded = true,
            recordings = listOf(
                RecordingRef(id = "rec_1", label = "Meine Stimme", path = "C:\\old\\rec_1.m4a", day = 3)
            )
        )

        val remapped = RecordingPaths.remap(progress, directory)

        assertEquals(File(directory, "rec_1.m4a").absolutePath, remapped.recordings.single().path)
        assertEquals("Meine Stimme", remapped.recordings.single().label)
    }
}
