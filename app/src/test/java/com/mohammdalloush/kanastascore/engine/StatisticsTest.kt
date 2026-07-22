package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsTest {

    @Test
    fun testEmptyStats() {
        val store = MatchesStore(matches = emptyList())
        assertEquals(0, store.matches.size)
    }

    @Test
    fun testScoreComponents() {
        val input = TeamRoundInput(
            directInput = DirectMeldScoreInput(redCanastasCount = 2, blackCanastasCount = 3),
            redThreesCount = 4,
            exited = true,
            remainingPoints = 0
        )
        val score = ScoreCalculator.calculateScore(input)
        assertEquals(2 * 300, score.redCanastaBonus)
        assertEquals(3 * 200, score.blackCanastaBonus)
        assertEquals(800, score.redThreesScore)
        assertEquals(100, score.exitBonus)
    }
}
