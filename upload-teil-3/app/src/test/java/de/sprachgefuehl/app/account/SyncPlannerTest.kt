package de.sprachgefuehl.app.account

import de.sprachgefuehl.app.data.account.SyncAction
import de.sprachgefuehl.app.data.account.SyncPlanner
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPlannerTest {

    @Test
    fun `first local progress is uploaded`() {
        assertEquals(
            SyncAction.UPLOAD,
            SyncPlanner.decide(
                cloudExists = false,
                localOnboarded = true,
                localDirty = true,
                localRevision = 0,
                cloudRevision = 0
            )
        )
    }

    @Test
    fun `fresh install downloads the cloud document`() {
        assertEquals(
            SyncAction.DOWNLOAD,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = false,
                localDirty = false,
                localRevision = 0,
                cloudRevision = 4
            )
        )
    }

    @Test
    fun `a fresh install with an untouched document downloads instead of asking`() {
        // On a real fresh install there is no stored hash yet, so the document counts as dirty.
        // That must still not become a conflict question about an empty local state.
        assertEquals(
            SyncAction.DOWNLOAD,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = false,
                localDirty = true,
                localRevision = 0,
                cloudRevision = 4
            )
        )
    }

    @Test
    fun `real local progress meets a foreign cloud state as a conflict`() {
        assertEquals(
            SyncAction.CONFLICT,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = true,
                localDirty = true,
                localRevision = 0,
                cloudRevision = 3
            )
        )
    }

    @Test
    fun `a local change on the known revision is uploaded`() {
        assertEquals(
            SyncAction.UPLOAD,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = true,
                localDirty = true,
                localRevision = 4,
                cloudRevision = 4
            )
        )
    }

    @Test
    fun `two changed sides become a conflict`() {
        assertEquals(
            SyncAction.CONFLICT,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = true,
                localDirty = true,
                localRevision = 4,
                cloudRevision = 5
            )
        )
    }

    @Test
    fun `an unchanged device behind the cloud downloads`() {
        assertEquals(
            SyncAction.DOWNLOAD,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = true,
                localDirty = false,
                localRevision = 4,
                cloudRevision = 5
            )
        )
    }

    @Test
    fun `unchanged and current is a no-op`() {
        assertEquals(
            SyncAction.NOOP,
            SyncPlanner.decide(
                cloudExists = true,
                localOnboarded = true,
                localDirty = false,
                localRevision = 5,
                cloudRevision = 5
            )
        )
    }

    @Test
    fun `nothing to sync yet is a no-op`() {
        assertEquals(
            SyncAction.NOOP,
            SyncPlanner.decide(
                cloudExists = false,
                localOnboarded = false,
                localDirty = false,
                localRevision = 0,
                cloudRevision = 0
            )
        )
    }
}
