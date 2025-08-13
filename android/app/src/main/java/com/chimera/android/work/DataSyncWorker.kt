package com.chimera.android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chimera.android.data.repository.RankingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val rankingRepository: RankingRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting background data sync")
            
            // Clean up old cached data
            rankingRepository.clearOldCache()
            
            Timber.d("Background data sync completed")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Background data sync failed")
            Result.retry()
        }
    }
}