package de.sprachgefuehl.app.data.account

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * A failed upload is not lost: WorkManager retries it with a network constraint when the phone
 * has a connection again. The work is unique, so a second failure replaces the pending retry
 * instead of stacking jobs.
 */
object SyncScheduler {
    private const val WORK_NAME = "sprachgefuehl-cloud-sync"

    fun enqueue(context: Context, delayMinutes: Long = 15L) {
        if (!SupabaseConfig.configured) return
        val request = OneTimeWorkRequestBuilder<CloudSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
