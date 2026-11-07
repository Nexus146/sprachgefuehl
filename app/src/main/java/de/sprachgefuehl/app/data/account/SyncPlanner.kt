package de.sprachgefuehl.app.data.account

enum class SyncAction {
    NOOP,
    UPLOAD,
    DOWNLOAD,
    CONFLICT
}

/**
 * The whole conflict policy without Android or network types, so it can be tested directly.
 *
 * [localRevision] is the revision this device last saw from the server. [localDirty] means the
 * local document changed since that moment. A dirty document on an older revision is never
 * overwritten silently: the learner chooses which side wins.
 */
object SyncPlanner {
    fun decide(
        cloudExists: Boolean,
        localOnboarded: Boolean,
        localDirty: Boolean,
        localRevision: Long,
        cloudRevision: Long
    ): SyncAction {
        if (!cloudExists) {
            return if (localOnboarded) SyncAction.UPLOAD else SyncAction.NOOP
        }
        // Nothing worth keeping on this device yet: a fresh install or a reset device. Pulling
        // the cloud copy is what the learner expects after signing in; asking which side wins
        // would be a question about an empty document.
        if (!localOnboarded) return SyncAction.DOWNLOAD
        if (!localDirty) {
            return if (localRevision < cloudRevision) SyncAction.DOWNLOAD else SyncAction.NOOP
        }
        return if (localRevision == cloudRevision) SyncAction.UPLOAD else SyncAction.CONFLICT
    }
}
