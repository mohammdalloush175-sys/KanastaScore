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

        val hasCanasta = if (input.scoringMode == MeldScoringMode.DETAILED) {
            val ranks = mutableSetOf<CardRank>()
            for (meld in input.melds) {
                val error = validateMeld(meld)
                if (error != null) return error
                
                if (ranks.contains(meld.rank)) {
                    return "لا يمكن تكرار الباب من نفس الرتبة"
                }
                ranks.add(meld.rank)
            }
            input.melds.any { it.canastaType != null }
        } else {
            if (input.directInput.totalMeldPoints < 0 || input.directInput.redCanastasCount < 0 || input.directInput.blackCanastasCount < 0) {
                return "القيم لا يمكن أن تكون سالبة"
            }
            (input.directInput.redCanastasCount + input.directInput.blackCanastasCount) > 0
        }

        if (input.exited && !hasCanasta) {
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

        if (team1.redThreesCount + team2.redThreesCount > 4) {
            return "لا يمكن أن يتجاوز مجموع الثلاثات الحمراء للفريقين أربع ثلاثات."
        }

        return null
    }

    fun getWarnings(team1: TeamRoundInput, team2: TeamRoundInput): List<String> {
        val warnings = mutableListOf<String>()
        
        fun checkTeam(input: TeamRoundInput, teamName: String) {
            if (input.directInput.totalMeldPoints > 2000) {
                warnings.add("$teamName: مجموع نقاط الأبواب مرتفع جداً (${input.directInput.totalMeldPoints})")
            }
            if (input.remainingPoints > 1000) {
                warnings.add("$teamName: نقاط الحرق مرتفعة جداً (${input.remainingPoints})")
            }
            if (input.directInput.redCanastasCount + input.directInput.blackCanastasCount > 6) {
                warnings.add("$teamName: عدد الكنستات مرتفع بصورة غير معتادة")
            }
            val hasCanasta = (input.directInput.redCanastasCount + input.directInput.blackCanastasCount) > 0
            if (input.directInput.totalMeldPoints > 0 && !hasCanasta) {
                warnings.add("$teamName: الفريق لديه نقاط أبواب لكن لا يملك أي كنستا (سيتم تطبيق العقوبة)")
            }
        }

        checkTeam(team1, "الفريق 1")
        checkTeam(team2, "الفريق 2")
        
        val res1 = ScoreCalculator.calculateScore(team1)
        val res2 = ScoreCalculator.calculateScore(team2)
        
        if (res1.total < -1000) warnings.add("الفريق 1: نتيجة الجولة سالبة جداً (${res1.total})")
        if (res2.total < -1000) warnings.add("الفريق 2: نتيجة الجولة سالبة جداً (${res2.total})")
        if (res1.total < 0 && res2.total < 0) warnings.add("كلا الفريقين نتيجتهما سالبة في هذه الجولة")

        return warnings
    }
}
