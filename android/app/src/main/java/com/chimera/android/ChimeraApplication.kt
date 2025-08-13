package com.chimera.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chimera.android.work.DataSyncWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ChimeraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Schedule background data sync
        schedulePeriodicDataSync()
        
        Timber.d("Chimera Application initialized")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun schedulePeriodicDataSync() {
        val dataSyncWork = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval = 6, // Every 6 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1, // 1 hour flex window
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .addTag("data_sync")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_data_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            dataSyncWork
        )
    }
}