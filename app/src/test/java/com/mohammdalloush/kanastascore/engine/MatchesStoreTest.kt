package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MatchesStoreTest {

    @Test
    fun testStoreStructure() {
        val team1 = Team("Team A", emptyList())
        val team2 = Team("Team B", emptyList())
        val state = GameState(teams = listOf(team1, team2))
        
        val match1 = SavedMatch(matchName = "Match 1", gameState = state)
        val match2 = SavedMatch(matchName = "Match 2", gameState = state)
        
        val store = MatchesStore(matches = listOf(match1, match2), activeMatchId = match1.id)
        
        assertEquals(2, store.matches.size)
        assertEquals(match1.id, store.activeMatchId)
        assertNotEquals(match1.id, match2.id)
    }
}
