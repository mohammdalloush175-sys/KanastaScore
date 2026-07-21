package com.mohammdalloush.kanastascore.engine

import java.util.UUID

data class Team(
    val name: String,
    val players: List<String>
)

enum class CardRank {
    THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
}

enum class CanastaType {
    RED, BLACK
}

data class MeldInput(
    val id: String = UUID.randomUUID().toString(),
    val rank: CardRank,
    val naturalCount: Int,
    val twosCount: Int,
    val jokersCount: Int,
    val canastaType: CanastaType? = null
)

data class TeamRoundInput(
    val melds: List<MeldInput> = emptyList(),
    val redThreesCount: Int = 0,
    val exited: Boolean = false,
    val remainingPoints: Int = 0
)

data class ScoreBreakdown(
    val meldPoints: Int = 0,
    val redCanastaBonus: Int = 0,
    val blackCanastaBonus: Int = 0,
    val redThreesScore: Int = 0,
    val exitBonus: Int = 0,
    val remainingCardsPenalty: Int = 0,
    val noCanastaPenalty: Int = 0,
    val total: Int = 0,
    val details: List<String> = emptyList(),
    val hasValidCanasta: Boolean = false
)

data class RoundRecord(
    val id: String = UUID.randomUUID().toString(),
    val roundNumber: Int,
    val team1Input: TeamRoundInput,
    val team2Input: TeamRoundInput,
    val team1Result: ScoreBreakdown,
    val team2Result: ScoreBreakdown
)

data class GameState(
    val teams: List<Team>,
    val targetScore: Int = 5000,
    val rounds: List<RoundRecord> = emptyList(),
    val schemaVersion: Int = 1
)
