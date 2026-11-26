package com.mohammdalloush.kanastascore.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mohammdalloush.kanastascore.engine.*

class GameViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _matchesStore = MutableLiveData<MatchesStore>(MatchesStore())
    val matchesStore: LiveData<MatchesStore> = _matchesStore

    val activeMatchId = MutableLiveData<String?>(null)

    // Current Round Draft (Temporary state for the active match)
    val team1RoundInput = MutableLiveData<TeamRoundInput>(TeamRoundInput())
    val team2RoundInput = MutableLiveData<TeamRoundInput>(TeamRoundInput())
    
    var useManualTotals = false

    val editingRoundId = MutableLiveData<String?>(null)

    private val _isRecordingRound = MutableLiveData<Boolean>(false)
    val isRecordingRound: LiveData<Boolean> = _isRecordingRound

    private var lastUndoableRound: RoundRecord? = null
    private var replacedRound: RoundRecord? = null
    private var lastUndoableMatchId: String? = null

    private val _lastRecordedRoundNumber = MutableLiveData<Int?>(null)
    val lastRecordedRoundNumber: LiveData<Int?> = _lastRecordedRoundNumber

    private val _appThemeMode = MutableLiveData<AppThemeMode>(AppThemeMode.SYSTEM)
    val appThemeMode: LiveData<AppThemeMode> = _appThemeMode

    fun setMatchesStore(store: MatchesStore) {
        _matchesStore.value = store
        if (activeMatchId.value == null && store.activeMatchId != null) {
            setActiveMatch(store.activeMatchId)
        }
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    fun setActiveMatch(matchId: String?) {
        // Auto-save current draft before switching
        saveCurrentDraft()
        
        activeMatchId.value = matchId
        val match = _matchesStore.value?.matches?.find { it.id == matchId }
        if (match != null) {
            useManualTotals = match.draft.useManualTotals
            team1RoundInput.value = match.draft.team1Input
            team2RoundInput.value = match.draft.team2Input
            editingRoundId.value = match.draft.editingRoundId
        } else {
            useManualTotals = false
            clearRoundDraft()
        }
        updateStoreActiveId(matchId)
    }

    private fun updateStoreActiveId(matchId: String?) {
        val current = _matchesStore.value ?: return
        _matchesStore.value = current.copy(activeMatchId = matchId)
    }

    fun createNewMatch(matchName: String, teams: List<Team>, targetScore: Int): String {
        val newMatch = SavedMatch(
            matchName = matchName,
            gameState = GameState(teams = teams, targetScore = targetScore)
        )
        val current = _matchesStore.value ?: MatchesStore()
        val newList = current.matches.toMutableList().apply { add(0, newMatch) }
        _matchesStore.value = current.copy(matches = newList)
        setActiveMatch(newMatch.id)
        return newMatch.id
    }

    fun deleteMatch(matchId: String) {
        val current = _matchesStore.value ?: return
        val newList = current.matches.filter { it.id != matchId }
        if (activeMatchId.value == matchId) {
            activeMatchId.value = null
            clearRoundDraft()
        }
        _matchesStore.value = current.copy(matches = newList)
    }

    fun startNewMatch() {
        // This is used when user confirms they want to "Abandon" or "Start Fresh"
        // But usually, it just means going to setup screen.
        // For this app, it means we don't care about activeMatchId anymore while setting up.
        activeMatchId.value = null
        useManualTotals = false
        clearRoundDraft()
    }

    fun clearRoundDraft() {
        team1RoundInput.value = TeamRoundInput()
        team2RoundInput.value = TeamRoundInput()
        editingRoundId.value = null
    }

    fun saveCurrentDraft() {
        val matchId = activeMatchId.value ?: return
        val currentStore = _matchesStore.value ?: return
        val matchIndex = currentStore.matches.indexOfFirst { it.id == matchId }
        if (matchIndex >= 0) {
            val updatedMatch = currentStore.matches[matchIndex].copy(
                draft = RoundDraft(
                    team1Input = team1RoundInput.value!!,
                    team2Input = team2RoundInput.value!!,
                    useManualTotals = useManualTotals,
                    editingRoundId = editingRoundId.value
                ),
                updatedAt = System.currentTimeMillis()
            )
            val newList = currentStore.matches.toMutableList()
            newList[matchIndex] = updatedMatch
            _matchesStore.value = currentStore.copy(matches = newList)
        }
    }

    fun updateTeam1Input(input: TeamRoundInput) {
        team1RoundInput.value = input
        saveCurrentDraft()
    }

    fun updateTeam2Input(input: TeamRoundInput) {
        team2RoundInput.value = input
        saveCurrentDraft()
    }

    fun setRedThrees(teamIndex: Int, count: Int) {
        val safeCount = count.coerceIn(0, 4)
        if (teamIndex == 0) {
            val t1 = team1RoundInput.value ?: TeamRoundInput()
            val t2 = team2RoundInput.value ?: TeamRoundInput()
            
            team1RoundInput.value = t1.copy(redThreesCount = safeCount, isRedThreesManual = true)
            
            // Suggest for Team 2 only if it's not manually edited
            if (!t2.isRedThreesManual) {
                team2RoundInput.value = t2.copy(redThreesCount = 4 - safeCount, isRedThreesManual = false)
            }
        } else {
            val t1 = team1RoundInput.value ?: TeamRoundInput()
            val t2 = team2RoundInput.value ?: TeamRoundInput()
            
            team2RoundInput.value = t2.copy(redThreesCount = safeCount, isRedThreesManual = true)
            
            // Suggest for Team 1 only if it's not manually edited
            if (!t1.isRedThreesManual) {
                team1RoundInput.value = t1.copy(redThreesCount = 4 - safeCount, isRedThreesManual = false)
            }
        }
        saveCurrentDraft()
    }

    fun resetRedThrees() {
        team1RoundInput.value = team1RoundInput.value?.copy(redThreesCount = 0, isRedThreesManual = false)
        team2RoundInput.value = team2RoundInput.value?.copy(redThreesCount = 0, isRedThreesManual = false)
        saveCurrentDraft()
    }

    fun setTeamExited(teamIndex: Int, exited: Boolean) {
        if (teamIndex == 0) {
            team1RoundInput.value = team1RoundInput.value?.copy(exited = exited)
            if (exited) team2RoundInput.value = team2RoundInput.value?.copy(exited = false)
        } else {
            team2RoundInput.value = team2RoundInput.value?.copy(exited = exited)
            if (exited) team1RoundInput.value = team1RoundInput.value?.copy(exited = false)
        }
        saveCurrentDraft()
    }

    fun editRound(round: RoundRecord) {
        editingRoundId.value = round.id
        useManualTotals = round.team1Input.manualRoundTotal != null
        team1RoundInput.value = round.team1Input
        team2RoundInput.value = round.team2Input
        saveCurrentDraft()
    }

    fun recordRound(team1: TeamRoundInput, team2: TeamRoundInput): Boolean {
        if (_isRecordingRound.value == true) return false
        val match = getActiveMatch() ?: return false
        val editing = match.gameState.rounds.find { it.id == editingRoundId.value }
        val roundNumber = editing?.roundNumber
            ?: ((match.gameState.rounds.maxOfOrNull { it.roundNumber } ?: 0) + 1)
        val record = RoundRecord(
            id = editing?.id ?: java.util.UUID.randomUUID().toString(),
            roundNumber = roundNumber,
            team1Input = team1,
            team2Input = team2,
            team1Result = ScoreCalculator.calculateScore(team1),
            team2Result = ScoreCalculator.calculateScore(team2)
        )
        addRound(record)
        clearRoundDraft()
        return true
    }

    fun addRound(record: RoundRecord) {
        val matchId = activeMatchId.value ?: return
        val currentStore = _matchesStore.value ?: return
        
        if (_isRecordingRound.value == true) return
        _isRecordingRound.value = true

        val matchIndex = currentStore.matches.indexOfFirst { it.id == matchId }
        if (matchIndex >= 0) {
            val match = currentStore.matches[matchIndex]
            val newRounds = match.gameState.rounds.toMutableList()
            
            val existingIndex = newRounds.indexOfFirst { it.id == record.id }
            if (existingIndex >= 0) {
                newRounds[existingIndex] = record
            } else {
                newRounds.add(record)
            }

            val updatedRounds = resequenceRounds(newRounds)
            val newStatus = if (checkIfGameEnded(match.gameState.copy(rounds = updatedRounds))) {
                MatchStatus.COMPLETED
            } else {
                MatchStatus.ACTIVE
            }

            val updatedMatch = match.copy(
                gameState = match.gameState.copy(rounds = updatedRounds),
                status = newStatus,
                draft = RoundDraft(useManualTotals = useManualTotals),
                updatedAt = System.currentTimeMillis()
            )
            
            lastUndoableRound = record
            replacedRound = match.gameState.rounds.getOrNull(existingIndex)
            lastUndoableMatchId = matchId

            val newList = currentStore.matches.toMutableList()
            newList[matchIndex] = updatedMatch
            _matchesStore.value = currentStore.copy(matches = newList)
            _lastRecordedRoundNumber.value = record.roundNumber
        }
        _isRecordingRound.value = false
    }

    fun undoLastRound() {
        val round = lastUndoableRound ?: return
        val matchId = lastUndoableMatchId ?: return
        val currentStore = _matchesStore.value ?: return
        
        val matchIndex = currentStore.matches.indexOfFirst { it.id == matchId }
        if (matchIndex >= 0) {
            val match = currentStore.matches[matchIndex]
            val newRounds = match.gameState.rounds.filter { it.id != round.id } + listOfNotNull(replacedRound)
            val updatedRounds = resequenceRounds(newRounds)
            
            // Restore draft from the undone round
            useManualTotals = round.team1Input.manualRoundTotal != null
            editingRoundId.value = replacedRound?.id
            team1RoundInput.value = round.team1Input
            team2RoundInput.value = round.team2Input
            
            val updatedMatch = match.copy(
                gameState = match.gameState.copy(rounds = updatedRounds),
                status = if (checkIfGameEnded(match.gameState.copy(rounds = updatedRounds))) {
                    MatchStatus.COMPLETED
                } else {
                    MatchStatus.ACTIVE
                },
                draft = RoundDraft(round.team1Input, round.team2Input, useManualTotals, replacedRound?.id),
                updatedAt = System.currentTimeMillis()
            )
            val newList = currentStore.matches.toMutableList()
            newList[matchIndex] = updatedMatch
            _matchesStore.value = currentStore.copy(matches = newList)
            
            clearUndo()
        }
    }

    fun clearUndo() {
        lastUndoableRound = null
        replacedRound = null
        lastUndoableMatchId = null
    }

    fun consumeRecordEvent() {
        _lastRecordedRoundNumber.value = null
    }

    fun reMatch() {
        val currentMatch = getActiveMatch() ?: return
        val newMatchName = "إعادة: ${currentMatch.matchName}"
        val newMatch = SavedMatch(
            matchName = newMatchName,
            gameState = GameState(
                teams = currentMatch.gameState.teams,
                targetScore = currentMatch.gameState.targetScore
            )
        )
        val currentStore = _matchesStore.value ?: MatchesStore()
        val newList = currentStore.matches.toMutableList().apply { add(0, newMatch) }
        _matchesStore.value = currentStore.copy(matches = newList)
        setActiveMatch(newMatch.id)
    }

    private fun checkIfGameEnded(state: GameState): Boolean {
        val s1 = state.rounds.sumOf { it.team1Result.total }
        val s2 = state.rounds.sumOf { it.team2Result.total }
        return (s1 >= state.targetScore || s2 >= state.targetScore) && s1 != s2
    }

    fun deleteRound(roundId: String) {
        val matchId = activeMatchId.value ?: return
        val currentStore = _matchesStore.value ?: return
        val matchIndex = currentStore.matches.indexOfFirst { it.id == matchId }
        if (matchIndex >= 0) {
            val match = currentStore.matches[matchIndex]
            val newRounds = match.gameState.rounds.filter { it.id != roundId }
            val updatedRounds = resequenceRounds(newRounds)
            
            val updatedMatch = match.copy(
                gameState = match.gameState.copy(rounds = updatedRounds),
                updatedAt = System.currentTimeMillis()
            )
            val newList = currentStore.matches.toMutableList()
            newList[matchIndex] = updatedMatch
            _matchesStore.value = currentStore.copy(matches = newList)
        }
    }

    private fun resequenceRounds(rounds: List<RoundRecord>): List<RoundRecord> {
        return rounds.sortedBy { it.roundNumber }.mapIndexed { index, record ->
            record.copy(roundNumber = index + 1)
        }
    }

    fun getActiveMatch(): SavedMatch? {
        val id = activeMatchId.value ?: return null
        return _matchesStore.value?.matches?.find { it.id == id }
    }

    fun getCumulativeScore(teamIndex: Int): Int {
        val match = getActiveMatch() ?: return 0
        return match.gameState.rounds.sumOf { if (teamIndex == 0) it.team1Result.total else it.team2Result.total }
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
