package com.mohammdalloush.kanastascore.update

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import com.mohammdalloush.kanastascore.BuildConfig
import kotlin.concurrent.thread

object UpdateScheduler {
    private const val JOB_ID = 18472
    private const val CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L

    fun schedule(context: Context) {
        val preferences = UpdatePreferences(context)
        val scheduler = context.getSystemService(JobScheduler::class.java)
        if (!preferences.automaticChecksEnabled) {
            scheduler.cancel(JOB_ID)
            return
        }
        val job = JobInfo.Builder(
            JOB_ID,
            ComponentName(context, UpdateJobService::class.java),
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(CHECK_INTERVAL_MS)
            .build()
        scheduler.schedule(job)
    }

    fun cancel(context: Context) {
        context.getSystemService(JobScheduler::class.java).cancel(JOB_ID)
    }

    fun checkNowInBackground(context: Context) {
        val appContext = context.applicationContext
        val preferences = UpdatePreferences(appContext)
        if (!preferences.automaticChecksEnabled) return
        thread(name = "github-update-check", isDaemon = true) {
            val result = GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
            preferences.lastCheckAt = System.currentTimeMillis()
            if (result is UpdateCheckResult.UpdateAvailable &&
                preferences.lastNotifiedTag != result.release.tagName
            ) {
                val shown = UpdateNotificationManager.showUpdateAvailable(appContext, result.release)
                if (shown) preferences.lastNotifiedTag = result.release.tagName
            }
        }
    }
}
