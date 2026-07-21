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
    fun testRedThreesScoreWithCanasta() {
        assertEquals(100, ScoreCalculator.calculateRedThreesScore(1, true))
        assertEquals(800, ScoreCalculator.calculateRedThreesScore(4, true))
    }

    @Test
    fun testRedThreesScoreWithoutCanasta() {
        assertEquals(-100, ScoreCalculator.calculateRedThreesScore(1, false))
        assertEquals(-800, ScoreCalculator.calculateRedThreesScore(4, false))
    }

    @Test
    fun testNoCanastaPenalty() {
        val input = TeamRoundInput(redThreesCount = 2, exited = false, remainingPoints = 50)
        val score = ScoreCalculator.calculateScore(input)
        assertEquals(-500, score.total) // -300 (no canasta) - 200 (2 red threes)
        assertEquals(false, score.hasValidCanasta)
    }

    @Test
    fun testFullScoreCalculation() {
        val melds = listOf(
            MeldInput(rank = CardRank.EIGHT, naturalCount = 7, twosCount = 0, jokersCount = 0, canastaType = CanastaType.RED), // 70 + 300 = 370
            MeldInput(rank = CardRank.ACE, naturalCount = 5, twosCount = 2, jokersCount = 0, canastaType = CanastaType.BLACK)  // 5*20 + 2*20 + 200 = 100 + 40 + 200 = 340
        )
        val input = TeamRoundInput(
            melds = melds,
            redThreesCount = 4, // 800
            exited = true,       // 100
            remainingPoints = 50 // -50
        )
        val score = ScoreCalculator.calculateScore(input)
        // 70 + 340 + 300 + 200 + 800 + 100 - 50 = 1760 ?
        // Wait: meldPoints = 70 + 140 = 210
        // redCanastaBonus = 300
        // blackCanastaBonus = 200
        // redThreesScore = 800
        // exitBonus = 100
        // remainingCardsPenalty = -50
        // Total = 210 + 300 + 200 + 800 + 100 - 50 = 1560
        assertEquals(1560, score.total)
    }
}
