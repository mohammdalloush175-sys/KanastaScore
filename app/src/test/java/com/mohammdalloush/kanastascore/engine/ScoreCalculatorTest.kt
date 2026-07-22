package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun testCardValues() {
        assertEquals(5, ScoreCalculator.cardValue(CardRank.THREE))
        assertEquals(5, ScoreCalculator.cardValue(CardRank.SEVEN))
        assertEquals(10, ScoreCalculator.cardValue(CardRank.EIGHT))
        assertEquals(10, ScoreCalculator.cardValue(CardRank.KING))
        assertEquals(20, ScoreCalculator.cardValue(CardRank.ACE))
    }

    @Test
    fun testMeldPoints() {
        val meld = MeldInput(rank = CardRank.EIGHT, naturalCount = 3, twosCount = 1, jokersCount = 1)
        // 3*10 + 1*20 + 1*50 = 30 + 20 + 50 = 100
        assertEquals(100, ScoreCalculator.calculateMeldPoints(meld))
    }

    @Test
    fun testDirectScoringMode() {
        val input = TeamRoundInput(
            scoringMode = MeldScoringMode.DIRECT_TOTAL,
            directInput = DirectMeldScoreInput(totalMeldPoints = 420, redCanastasCount = 1, blackCanastasCount = 1),
            redThreesCount = 2,
            exited = true,
            remainingPoints = 50
        )
        val score = ScoreCalculator.calculateScore(input)
        // 420 (melds) + 300 (red) + 200 (black) + 200 (2 red threes) + 100 (exit) - 50 (penalty) = 1170
        assertEquals(1170, score.total)
        assertEquals(true, score.hasValidCanasta)
    }

    @Test
    fun testNoCanastaPenaltyDetailed() {
        val input = TeamRoundInput(
            scoringMode = MeldScoringMode.DETAILED,
            melds = emptyList(),
            redThreesCount = 2,
            remainingPoints = 50
        )
        val score = ScoreCalculator.calculateScore(input)
        assertEquals(-500, score.total) // -300 (no canasta) - 200 (2 red threes)
    }

    @Test
    fun testNoCanastaPenaltyDirect() {
        val input = TeamRoundInput(
            scoringMode = MeldScoringMode.DIRECT_TOTAL,
            directInput = DirectMeldScoreInput(totalMeldPoints = 500, redCanastasCount = 0, blackCanastasCount = 0),
            redThreesCount = 1
        )
        val score = ScoreCalculator.calculateScore(input)
        assertEquals(-400, score.total) // -300 (no canasta) - 100 (1 red three)
    }
}
