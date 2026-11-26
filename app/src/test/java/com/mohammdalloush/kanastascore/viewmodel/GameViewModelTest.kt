package com.mohammdalloush.kanastascore.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.mohammdalloush.kanastascore.engine.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun testRedThreesSuggestion() {
        val viewModel = GameViewModel(SavedStateHandle())
        
        // Initial state
        assertEquals(0, viewModel.team1RoundInput.value?.redThreesCount)
        assertEquals(0, viewModel.team2RoundInput.value?.redThreesCount)
        assertFalse(viewModel.team1RoundInput.value!!.isRedThreesManual)
        assertFalse(viewModel.team2RoundInput.value!!.isRedThreesManual)

        // Set Team 1 to 3 -> Team 2 should suggest 1
        viewModel.setRedThrees(0, 3)
        assertEquals(3, viewModel.team1RoundInput.value?.redThreesCount)
        assertEquals(1, viewModel.team2RoundInput.value?.redThreesCount)
        assertTrue(viewModel.team1RoundInput.value!!.isRedThreesManual)
        assertFalse(viewModel.team2RoundInput.value!!.isRedThreesManual)

        // Manually set Team 2 to 0 -> Team 2 becomes Manual
        viewModel.setRedThrees(1, 0)
        assertEquals(3, viewModel.team1RoundInput.value?.redThreesCount)
        assertEquals(0, viewModel.team2RoundInput.value?.redThreesCount)
        assertTrue(viewModel.team1RoundInput.value!!.isRedThreesManual)
        assertTrue(viewModel.team2RoundInput.value!!.isRedThreesManual)

        // Change Team 1 again -> Team 2 should NOT change because it's manual
        viewModel.setRedThrees(0, 2)
        assertEquals(2, viewModel.team1RoundInput.value?.redThreesCount)
        assertEquals(0, viewModel.team2RoundInput.value?.redThreesCount)
        
        // Reset
        viewModel.resetRedThrees()
        assertEquals(0, viewModel.team1RoundInput.value?.redThreesCount)
        assertEquals(0, viewModel.team2RoundInput.value?.redThreesCount)
        assertFalse(viewModel.team1RoundInput.value!!.isRedThreesManual)
        assertFalse(viewModel.team2RoundInput.value!!.isRedThreesManual)
    }

    @Test
    fun manualRoundStoresExactTotalsAndKeepsModeForTheNextRound() {
        val viewModel = newMatch()
        viewModel.useManualTotals = true
        viewModel.updateTeam1Input(TeamRoundInput(manualRoundTotal = 530))
        viewModel.updateTeam2Input(TeamRoundInput(manualRoundTotal = -85))

        assertTrue(viewModel.recordRound(viewModel.team1RoundInput.value!!, viewModel.team2RoundInput.value!!))

        val match = viewModel.getActiveMatch()!!
        val round = match.gameState.rounds.single()
        assertEquals(530, round.team1Result.total)
        assertEquals(-85, round.team2Result.total)
        assertEquals(530, viewModel.getCumulativeScore(0))
        assertEquals(-85, viewModel.getCumulativeScore(1))
        assertEquals(RoundDraft(useManualTotals = true), match.draft)
        assertEquals(null, viewModel.team1RoundInput.value?.manualRoundTotal)

        viewModel.undoLastRound()
        assertTrue(viewModel.getActiveMatch()!!.gameState.rounds.isEmpty())
        assertEquals(530, viewModel.team1RoundInput.value?.manualRoundTotal)
    }

    @Test
    fun manualRoundEditKeepsItsPositionAndUndoRestoresTheOriginalRound() {
        val viewModel = newMatch()
        viewModel.useManualTotals = true
        viewModel.recordRound(TeamRoundInput(manualRoundTotal = 200), TeamRoundInput(manualRoundTotal = 0))
        viewModel.recordRound(TeamRoundInput(manualRoundTotal = 300), TeamRoundInput(manualRoundTotal = 100))
        val original = viewModel.getActiveMatch()!!.gameState.rounds

        viewModel.editRound(original.first())
        assertEquals(original.first().id, viewModel.getActiveMatch()!!.draft.editingRoundId)
        viewModel.recordRound(TeamRoundInput(manualRoundTotal = 5000), TeamRoundInput(manualRoundTotal = 0))

        val updated = viewModel.getActiveMatch()!!
        assertEquals(listOf(5000, 300), updated.gameState.rounds.map { it.team1Result.total })
        assertEquals(MatchStatus.COMPLETED, updated.status)

        viewModel.undoLastRound()
        assertEquals(original, viewModel.getActiveMatch()!!.gameState.rounds)
        assertEquals(MatchStatus.ACTIVE, viewModel.getActiveMatch()!!.status)
    }

    @Test
    fun deletingAllMatchesClearsActiveRoundDraftAndUndo() {
        val viewModel = newMatch()
        viewModel.useManualTotals = true
        viewModel.recordRound(TeamRoundInput(manualRoundTotal = 50), TeamRoundInput(manualRoundTotal = 0))
        viewModel.updateTeam1Input(TeamRoundInput(manualRoundTotal = 100))

        viewModel.deleteAllMatches()
        viewModel.undoLastRound()

        assertTrue(viewModel.matchesStore.value!!.matches.isEmpty())
        assertEquals(null, viewModel.matchesStore.value!!.activeMatchId)
        assertEquals(null, viewModel.getActiveMatch())
        assertEquals(null, viewModel.lastRecordedRoundNumber.value)
        assertEquals(TeamRoundInput(), viewModel.team1RoundInput.value)
        assertFalse(viewModel.useManualTotals)
    }

    private fun newMatch(): GameViewModel =
        GameViewModel(SavedStateHandle()).apply {
            createNewMatch("Test", listOf(Team("A", emptyList()), Team("B", emptyList())), 5000)
        }
}
