package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScoreValidatorTest {

    @Test
    fun testValidateMeld() {
        // Legal
        assertNull(ScoreValidator.validateMeld(MeldInput(rank = CardRank.FOUR, naturalCount = 3, twosCount = 0, jokersCount = 0)))
        
        // Less than 2 natural
        assertNotNull(ScoreValidator.validateMeld(MeldInput(rank = CardRank.FOUR, naturalCount = 1, twosCount = 2, jokersCount = 0)))
        
        // More than 2 wild
        assertNotNull(ScoreValidator.validateMeld(MeldInput(rank = CardRank.FOUR, naturalCount = 3, twosCount = 2, jokersCount = 1)))
        
        // Red canasta with wild
        assertNotNull(ScoreValidator.validateMeld(MeldInput(rank = CardRank.FOUR, naturalCount = 7, twosCount = 1, jokersCount = 0, canastaType = CanastaType.RED)))
        
        // Black canasta without wild
        assertNotNull(ScoreValidator.validateMeld(MeldInput(rank = CardRank.FOUR, naturalCount = 7, twosCount = 0, jokersCount = 0, canastaType = CanastaType.BLACK)))
    }

    @Test
    fun testValidateRound() {
        val team1 = TeamRoundInput(
            melds = listOf(MeldInput(rank = CardRank.FOUR, naturalCount = 3, twosCount = 0, jokersCount = 0)),
            exited = true // Error: exited without canasta
        )
        val team2 = TeamRoundInput()
        
        assertNotNull(ScoreValidator.validateRound(team1, team2))
        
        val team1Ok = TeamRoundInput(
            melds = listOf(MeldInput(rank = CardRank.FOUR, naturalCount = 7, twosCount = 0, jokersCount = 0, canastaType = CanastaType.RED)),
            exited = true
        )
        assertNull(ScoreValidator.validateRound(team1Ok, team2))
    }
}
