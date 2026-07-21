package com.mohammdalloush.kanastascore.utils

import android.content.Context
import com.google.gson.Gson
import com.mohammdalloush.kanastascore.engine.GameState

object StorageHelper {
    private const val PREFS_NAME = "canasta_score_preferences"
    private const val KEY_GAME_STATE = "active_game_state"

    fun saveGameState(context: Context, state: GameState) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(state)
        prefs.edit().putString(KEY_GAME_STATE, json).apply()
    }

    fun loadGameState(context: Context): GameState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_GAME_STATE, null) ?: return null
        return try {
            Gson().fromJson(json, GameState::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearGameState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_GAME_STATE).apply()
    }
}
