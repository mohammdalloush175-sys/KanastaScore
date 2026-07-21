package com.mohammdalloush.kanastascore.engine

object ScoreValidator {

    fun validateMeld(meld: MeldInput): String? {
        if (meld.naturalCount < 0 || meld.twosCount < 0 || meld.jokersCount < 0) {
            return "القيم لا يمكن أن تكون سالبة"
        }
        
        val wildCount = meld.twosCount + meld.jokersCount
        val totalCards = meld.naturalCount + wildCount

        if (meld.naturalCount < 2) {
            return "يجب أن يحتوي الباب على ورقتين طبيعيتين على الأقل"
        }

        if (wildCount > 2) {
            return "مجموع أوراق 2 والجواكر لا يزيد على ورقتين بريتين"
        }

        if (wildCount > meld.naturalCount) {
            return "عدد الأوراق البرية لا يزيد على عدد الأوراق الطبيعية"
        }

        if (meld.canastaType != null && totalCards < 7) {
            return "الكنستا يجب أن تكون 7 أوراق على الأقل"
        }

        if (meld.canastaType == CanastaType.RED && wildCount > 0) {
            return "الكنستا الحمراء لا تحتوي على أوراق برية"
        }

        if (meld.canastaType == CanastaType.BLACK && wildCount == 0) {
            return "الكنستا السوداء يجب أن تحتوي على ورقة برية"
        }

        return null
    }

    fun validateTeamInput(input: TeamRoundInput): String? {
        if (input.redThreesCount !in 0..4) {
            return "عدد الثلاثات الحمراء يجب أن يكون بين 0 و 4"
        }
        if (input.remainingPoints < 0) {
            return "نقاط الحرق لا يمكن أن تكون سالبة"
        }

        val ranks = mutableSetOf<CardRank>()
        for (meld in input.melds) {
            val error = validateMeld(meld)
            if (error != null) return error
            
            if (ranks.contains(meld.rank)) {
                return "لا يمكن تكرار الباب من نفس الرتبة"
            }
            ranks.add(meld.rank)
        }

        if (input.exited && input.melds.none { it.canastaType != null }) {
            return "لا يمكن الخروج دون وجود كنستا صالحة"
        }

        return null
    }

    fun validateRound(team1: TeamRoundInput, team2: TeamRoundInput): String? {
        val error1 = validateTeamInput(team1)
        if (error1 != null) return "الفريق 1: $error1"

        val error2 = validateTeamInput(team2)
        if (error2 != null) return "الفريق 2: $error2"

        if (team1.exited && team2.exited) {
            return "لا يمكن للفريقين الخروج في نفس الجولة"
        }

        return null
    }
}
