package com.mohammdalloush.kanastascore.engine

import com.mohammdalloush.kanastascore.engine.Constants.BONUS_BLACK_CANASTA
import com.mohammdalloush.kanastascore.engine.Constants.BONUS_EXIT
import com.mohammdalloush.kanastascore.engine.Constants.BONUS_RED_CANASTA
import com.mohammdalloush.kanastascore.engine.Constants.PENALTY_NO_CANASTA
import com.mohammdalloush.kanastascore.engine.Constants.VALUE_3_TO_7
import com.mohammdalloush.kanastascore.engine.Constants.VALUE_8_TO_K
import com.mohammdalloush.kanastascore.engine.Constants.VALUE_ACE
import com.mohammdalloush.kanastascore.engine.Constants.VALUE_JOKER
import com.mohammdalloush.kanastascore.engine.Constants.VALUE_TWO

object ScoreCalculator {

    fun cardValue(rank: CardRank): Int {
        return when (rank) {
            CardRank.THREE, CardRank.FOUR, CardRank.FIVE, CardRank.SIX, CardRank.SEVEN -> VALUE_3_TO_7
            CardRank.EIGHT, CardRank.NINE, CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> VALUE_8_TO_K
            CardRank.ACE -> VALUE_ACE
        }
    }

    fun calculateMeldPoints(meld: MeldInput): Int {
        return (meld.naturalCount * cardValue(meld.rank)) +
                (meld.twosCount * VALUE_TWO) +
                (meld.jokersCount * VALUE_JOKER)
    }

    fun calculateRedThreesScore(count: Int, hasCanasta: Boolean): Int {
        if (count == 0) return 0
        val baseScore = when (count) {
            1 -> 100
            2 -> 200
            3 -> 300
            4 -> 800
            else -> 0
        }
        return if (hasCanasta) baseScore else -baseScore
    }

    fun calculateScore(input: TeamRoundInput): ScoreBreakdown {
        val details = mutableListOf<String>()
        val hasCanasta = if (input.scoringMode == MeldScoringMode.DETAILED) {
            input.melds.any { it.canastaType != null }
        } else {
            (input.directInput.redCanastasCount + input.directInput.blackCanastasCount) > 0
        }

        if (!hasCanasta) {
            val redThreesPenalty = calculateRedThreesScore(input.redThreesCount, false)
            val total = PENALTY_NO_CANASTA + redThreesPenalty
            
            details.add("لا توجد كنستا صالحة ($PENALTY_NO_CANASTA)")
            if (input.redThreesCount > 0) {
                details.add("عقوبة الثلاثات الحمراء (${redThreesPenalty})")
            }
            details.add("المجموع: $total")

            return ScoreBreakdown(
                noCanastaPenalty = PENALTY_NO_CANASTA,
                redThreesScore = redThreesPenalty,
                total = total,
                details = details,
                hasValidCanasta = false
            )
        }

        var meldPointsTotal = 0
        var redCanastaBonus = 0
        var blackCanastaBonus = 0

        if (input.scoringMode == MeldScoringMode.DETAILED) {
            input.melds.forEach { meld ->
                val points = calculateMeldPoints(meld)
                meldPointsTotal += points
                when (meld.canastaType) {
                    CanastaType.RED -> redCanastaBonus += BONUS_RED_CANASTA
                    CanastaType.BLACK -> blackCanastaBonus += BONUS_BLACK_CANASTA
                    null -> {}
                }
            }
            details.add("نقاط الأبواب: $meldPointsTotal")
        } else {
            meldPointsTotal = input.directInput.totalMeldPoints
            redCanastaBonus = input.directInput.redCanastasCount * BONUS_RED_CANASTA
            blackCanastaBonus = input.directInput.blackCanastasCount * BONUS_BLACK_CANASTA
            
            details.add("مجموع نقاط الأبواب: $meldPointsTotal")
            if (input.directInput.redCanastasCount > 0) {
                details.add("عدد الكنستات الحمراء: ${input.directInput.redCanastasCount}")
            }
            if (input.directInput.blackCanastasCount > 0) {
                details.add("عدد الكنستات السوداء: ${input.directInput.blackCanastasCount}")
            }
        }

        if (redCanastaBonus > 0) details.add("مكافأة كنستا حمراء: +$redCanastaBonus")
        if (blackCanastaBonus > 0) details.add("مكافأة كنستا سوداء: +$blackCanastaBonus")

        val redThreesScore = calculateRedThreesScore(input.redThreesCount, true)
        val exitBonus = if (input.exited) BONUS_EXIT else 0
        val remainingCardsPenalty = -input.remainingPoints

        val total = meldPointsTotal + redCanastaBonus + blackCanastaBonus + redThreesScore + exitBonus + remainingCardsPenalty

        if (redThreesScore > 0) details.add("الثلاثات الحمراء: +$redThreesScore")
        if (exitBonus > 0) details.add("مكافأة الخروج: +$exitBonus")
        if (remainingCardsPenalty < 0) details.add("الحرق: $remainingCardsPenalty")
        details.add("المجموع: $total")

        return ScoreBreakdown(
            meldPoints = meldPointsTotal,
            redCanastaBonus = redCanastaBonus,
            blackCanastaBonus = blackCanastaBonus,
            redThreesScore = redThreesScore,
            exitBonus = exitBonus,
            remainingCardsPenalty = remainingCardsPenalty,
            total = total,
            details = details,
            hasValidCanasta = true
        )
    }
}
