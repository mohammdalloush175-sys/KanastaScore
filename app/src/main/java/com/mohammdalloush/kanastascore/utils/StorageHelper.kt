package com.mohammdalloush.kanastascore.utils

import android.content.Context
import com.google.gson.Gson
import com.mohammdalloush.kanastascore.engine.*

object StorageHelper {
    private const val PREFS_NAME = "canasta_score_preferences"
    private const val KEY_GAME_STATE_OLD = "active_game_state" // v2
    private const val KEY_MATCHES_STORE = "matches_store_v3"
    private const val KEY_THEME_MODE = "app_theme_mode"

    fun saveMatchesStore(context: Context, store: MatchesStore) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(store)
        prefs.edit().putString(KEY_MATCHES_STORE, json).apply()
    }

    fun loadMatchesStore(context: Context): MatchesStore {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check for v3 store
        val json = prefs.getString(KEY_MATCHES_STORE, null)
        if (json != null) {
            return try {
                Gson().fromJson(json, MatchesStore::class.java)
            } catch (e: Exception) {
                MatchesStore()
            }
        }

        // Migration from v2
        val oldJson = prefs.getString(KEY_GAME_STATE_OLD, null)
        if (oldJson != null) {
            return try {
                val oldState = Gson().fromJson(oldJson, GameState::class.java)
                val matchName = if (oldState.teams.size >= 2) {
                    "${oldState.teams[0].name} ضد ${oldState.teams[1].name}"
                } else "مباراة سابقة"
                
                val migratedMatch = SavedMatch(
                    matchName = matchName,
                    gameState = oldState,
                    updatedAt = System.currentTimeMillis()
                )
                val newStore = MatchesStore(matches = listOf(migratedMatch))
                saveMatchesStore(context, newStore)
                // Clear old key
                prefs.edit().remove(KEY_GAME_STATE_OLD).apply()
                newStore
            } catch (e: Exception) {
                MatchesStore()
            }
        }

        return MatchesStore()
    }

    fun saveThemeMode(context: Context, mode: AppThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun loadThemeMode(context: Context): AppThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(name!!)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun exportBackup(store: MatchesStore): String {
        val envelope = BackupEnvelope(matchesStore = store)
        return Gson().toJson(envelope)
    }

    fun importBackup(json: String): MatchesStore? {
        return try {
            val envelope = Gson().fromJson(json, BackupEnvelope::class.java)
            if (envelope.appName != "KanastaScore") return null
            envelope.matchesStore
        } catch (e: Exception) {
            null
        }
    }

    fun clearAllData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_MATCHES_STORE).remove(KEY_GAME_STATE_OLD).apply()
    }
}
