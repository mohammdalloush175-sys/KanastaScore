package com.mohammdalloush.kanastascore.update

import android.app.job.JobParameters
import android.app.job.JobService
import com.mohammdalloush.kanastascore.BuildConfig
import java.util.concurrent.Executors

class UpdateJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val preferences = UpdatePreferences(applicationContext)
            if (preferences.automaticChecksEnabled) {
                val result = GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
                preferences.lastCheckAt = System.currentTimeMillis()
                if (result is UpdateCheckResult.UpdateAvailable &&
                    preferences.lastNotifiedTag != result.release.tagName
                ) {
                    val shown = UpdateNotificationManager.showUpdateAvailable(
                        applicationContext,
                        result.release,
                    )
                    if (shown) preferences.lastNotifiedTag = result.release.tagName
                }
            }
            jobFinished(params, false)
            executor.shutdown()
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = true
}
