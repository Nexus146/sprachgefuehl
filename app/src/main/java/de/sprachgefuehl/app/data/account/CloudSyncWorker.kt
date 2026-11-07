package de.sprachgefuehl.app.data.account

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.sprachgefuehl.app.data.ContentRepository
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.ProgressStore
import java.io.File

/**
 * Background retry for a sync that could not finish. It rebuilds the local repository from the
 * same progress file the app uses, so no account session has to be kept in memory.
 */
class CloudSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!SupabaseConfig.configured) return Result.success()
        val content = runCatching { ContentRepository(applicationContext).load() }
            .getOrElse { return Result.retry() }
        val store = ProgressStore(File(applicationContext.filesDir, "progress.json"))
        val learning = LearningRepository(content, store)
        val account = CloudAccountRepository(
            applicationContext,
            progressProvider = { learning.progress.value },
            importProgress = { progress -> learning.importProgress(progress) },
            autoSync = false
        )
        return try {
            account.syncNowAndWait()
            if (account.state.value.phase == SyncPhase.OFFLINE) Result.retry() else Result.success()
        } finally {
            account.close()
        }
    }
}
