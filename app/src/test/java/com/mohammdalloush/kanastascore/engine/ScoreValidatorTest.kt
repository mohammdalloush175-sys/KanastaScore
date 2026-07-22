package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScoreValidatorTest {

    @Test
    fun testExclusiveExit() {
        val team1 = TeamRoundInput(exited = true, directInput = DirectMeldScoreInput(redCanastasCount = 1))
        val team2 = TeamRoundInput(exited = true, directInput = DirectMeldScoreInput(redCanastasCount = 1))
        
        assertNotNull(ScoreValidator.validateRound(team1, team2))
    }

    @Test
    fun testValidateDirectMode() {
        // No canasta but exited
        val input = TeamRoundInput(
            scoringMode = MeldScoringMode.DIRECT_TOTAL,
            directInput = DirectMeldScoreInput(totalMeldPoints = 100, redCanastasCount = 0, blackCanastasCount = 0),
            exited = true
        )
        assertNotNull(ScoreValidator.validateTeamInput(input))
        
        // Legal direct
        val legalInput = TeamRoundInput(
            scoringMode = MeldScoringMode.DIRECT_TOTAL,
            directInput = DirectMeldScoreInput(totalMeldPoints = 100, redCanastasCount = 1, blackCanastasCount = 0),
            exited = true
        )
        assertNull(ScoreValidator.validateTeamInput(legalInput))
    }
}
