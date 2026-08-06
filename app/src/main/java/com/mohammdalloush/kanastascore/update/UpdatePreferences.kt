package com.mohammdalloush.kanastascore.update

import android.content.Context

class UpdatePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "github_update_preferences",
        Context.MODE_PRIVATE,
    )

    var automaticChecksEnabled: Boolean
        get() = preferences.getBoolean(KEY_AUTOMATIC_CHECKS, true)
        set(value) = preferences.edit().putBoolean(KEY_AUTOMATIC_CHECKS, value).apply()

    var lastCheckAt: Long
        get() = preferences.getLong(KEY_LAST_CHECK, 0L)
        set(value) = preferences.edit().putLong(KEY_LAST_CHECK, value).apply()

    var lastNotifiedTag: String?
        get() = preferences.getString(KEY_LAST_NOTIFIED_TAG, null)
        set(value) = preferences.edit().putString(KEY_LAST_NOTIFIED_TAG, value).apply()

    companion object {
        private const val KEY_AUTOMATIC_CHECKS = "automatic_checks_enabled"
        private const val KEY_LAST_CHECK = "last_check_at"
        private const val KEY_LAST_NOTIFIED_TAG = "last_notified_tag"
    }
}
