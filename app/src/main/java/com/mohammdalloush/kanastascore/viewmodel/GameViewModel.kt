package com.mohammdalloush.kanastascore.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mohammdalloush.kanastascore.engine.*

class GameViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _gameState = MutableLiveData<GameState?>()
    val gameState: LiveData<GameState?> = _gameState

    // Round Drafts
    val team1RoundInput = savedStateHandle.getLiveData<TeamRoundInput>("team1Input", TeamRoundInput())
    val team2RoundInput = savedStateHandle.getLiveData<TeamRoundInput>("team2Input", TeamRoundInput())
    
    val editingRoundId = savedStateHandle.getLiveData<String?>("editingRoundId", null)

    private val _appThemeMode = MutableLiveData<AppThemeMode>(AppThemeMode.SYSTEM)
    val appThemeMode: LiveData<AppThemeMode> = _appThemeMode

    fun setGameState(state: GameState?) {
        _gameState.value = state
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    fun startNewGame(teams: List<Team>, targetScore: Int) {
        val newState = GameState(teams = teams, targetScore = targetScore)
        setGameState(newState)
        clearRoundDraft()
    }

    fun startNewMatch() {
        setGameState(null)
        clearRoundDraft()
    }

    fun clearRoundDraft() {
        team1RoundInput.value = TeamRoundInput()
        team2RoundInput.value = TeamRoundInput()
        editingRoundId.value = null
    }

    fun updateTeam1Input(input: TeamRoundInput) {
        team1RoundInput.value = input
    }

    fun updateTeam2Input(input: TeamRoundInput) {
        team2RoundInput.value = input
    }

    fun setRedThrees(teamIndex: Int, count: Int) {
        val safeCount = count.coerceIn(0, 4)
        if (teamIndex == 0) {
            team1RoundInput.value = team1RoundInput.value?.copy(redThreesCount = safeCount)
            team2RoundInput.value = team2RoundInput.value?.copy(redThreesCount = 4 - safeCount)
        } else {
            team2RoundInput.value = team2RoundInput.value?.copy(redThreesCount = safeCount)
            team1RoundInput.value = team1RoundInput.value?.copy(redThreesCount = 4 - safeCount)
        }
    }

    fun setTeamExited(teamIndex: Int, exited: Boolean) {
        if (teamIndex == 0) {
            team1RoundInput.value = team1RoundInput.value?.copy(exited = exited)
            if (exited) {
                team2RoundInput.value = team2RoundInput.value?.copy(exited = false)
            }
        } else {
            team2RoundInput.value = team2RoundInput.value?.copy(exited = exited)
            if (exited) {
                team1RoundInput.value = team1RoundInput.value?.copy(exited = false)
            }
        }
    }

    fun addRound(record: RoundRecord) {
        val currentState = _gameState.value ?: return
        val newRounds = currentState.rounds.toMutableList()
        
        val existingIndex = newRounds.indexOfFirst { it.id == record.id }
        if (existingIndex >= 0) {
            newRounds[existingIndex] = record
        } else {
            newRounds.add(record)
        }

        // Re-calculate round numbers and cumulative totals
        val updatedRounds = resequenceRounds(newRounds)
        setGameState(currentState.copy(rounds = updatedRounds))
    }

    fun deleteRound(roundId: String) {
        val currentState = _gameState.value ?: return
        val newRounds = currentState.rounds.filter { it.id != roundId }
        val updatedRounds = resequenceRounds(newRounds)
        setGameState(currentState.copy(rounds = updatedRounds))
    }

    private fun resequenceRounds(rounds: List<RoundRecord>): List<RoundRecord> {
        return rounds.sortedBy { it.roundNumber }.mapIndexed { index, record ->
            record.copy(roundNumber = index + 1)
        }
    }

    fun getCumulativeScore(teamIndex: Int): Int {
        val rounds = _gameState.value?.rounds ?: return 0
        return rounds.sumOf { if (teamIndex == 0) it.team1Result.total else it.team2Result.total }
    }

    fun getMinMeldPoints(teamIndex: Int): Int {
        val score = getCumulativeScore(teamIndex)
        return when {
            score < Constants.THRESHOLD_LOW -> Constants.MIN_POINTS_LOW
            score < Constants.THRESHOLD_MEDIUM -> Constants.MIN_POINTS_MEDIUM
            else -> Constants.MIN_POINTS_HIGH
        }
    }
}
