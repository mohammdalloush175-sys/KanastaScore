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
}
