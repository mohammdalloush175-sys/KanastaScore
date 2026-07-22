package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentStatisticsBinding
import com.mohammdalloush.kanastascore.engine.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = (activity as MainActivity).viewModel
        val store = viewModel.matchesStore.value ?: return
        
        if (store.matches.isEmpty()) {
            binding.tvNoStats.isVisible = true
            return
        }

        calculateAndShowStats(store)
    }

    private fun calculateAndShowStats(store: MatchesStore) {
        val allMatches = store.matches
        val allRounds = allMatches.flatMap { it.gameState.rounds }
        
        if (allRounds.isEmpty()) {
            binding.tvNoStats.isVisible = true
            return
        }

        // Global Stats
        val totalMatches = allMatches.size
        val completedMatches = allMatches.count { it.status == MatchStatus.COMPLETED }
        val totalRounds = allRounds.size
        val maxRoundScore = allRounds.maxOf { maxOf(it.team1Result.total, it.team2Result.total) }
        val avgRoundScore = allRounds.flatMap { listOf(it.team1Result.total, it.team2Result.total) }.average()
        
        val totalRedCanastas = allRounds.sumOf { it.team1Input.directInput.redCanastasCount + it.team2Input.directInput.redCanastasCount }
        val totalBlackCanastas = allRounds.sumOf { it.team1Input.directInput.blackCanastasCount + it.team2Input.directInput.blackCanastasCount }

        val globalText = StringBuilder()
        globalText.append(getString(R.string.stat_total_matches, totalMatches)).append("\n")
        globalText.append("المباريات المكتملة: $completedMatches").append("\n")
        globalText.append(getString(R.string.stat_total_rounds, totalRounds)).append("\n")
        globalText.append(getString(R.string.stat_max_round_score, maxRoundScore)).append("\n")
        globalText.append(getString(R.string.stat_avg_round_score, avgRoundScore)).append("\n")
        globalText.append(getString(R.string.stat_total_canastas, totalRedCanastas, totalBlackCanastas))
        
        binding.tvGlobalStats.text = globalText.toString()

        // Team Stats
        val teamStats = mutableMapOf<String, TeamStatsData>()
        
        allMatches.forEach { match ->
            val t1 = match.gameState.teams[0].name
            val t2 = match.gameState.teams[1].name
            
            val stats1 = teamStats.getOrPut(t1) { TeamStatsData() }
            val stats2 = teamStats.getOrPut(t2) { TeamStatsData() }
            
            stats1.matchesCount++
            stats2.matchesCount++
            
            val total1 = match.gameState.rounds.sumOf { it.team1Result.total }
            val total2 = match.gameState.rounds.sumOf { it.team2Result.total }
            
            if (match.status == MatchStatus.COMPLETED) {
                if (total1 > total2) stats1.wins++ else if (total2 > total1) stats2.wins++
            }
            
            match.gameState.rounds.forEach { round ->
                stats1.totalPoints += round.team1Result.total
                stats1.roundsCount++
                stats2.totalPoints += round.team2Result.total
                stats2.roundsCount++
            }
        }
        
        binding.containerTeamStats.removeAllViews()
        teamStats.entries.sortedByDescending { it.value.wins }.forEach { (name, data) ->
            addTeamStatRow(name, data)
        }
    }

    private fun addTeamStatRow(name: String, data: TeamStatsData) {
        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_team_stat_row, binding.containerTeamStats, false)
        val tvName = row.findViewById<TextView>(R.id.tv_team_name)
        val tvDetails = row.findViewById<TextView>(R.id.tv_team_details)
        
        tvName.text = name
        
        val winRate = if (data.matchesCount > 0) (data.wins.toFloat() / data.matchesCount * 100) else 0f
        val avg = if (data.roundsCount > 0) (data.totalPoints.toFloat() / data.roundsCount) else 0f
        
        val details = "${getString(R.string.stat_team_win_rate, data.wins, winRate)} | ${getString(R.string.stat_team_avg, avg)}"
        tvDetails.text = details
        
        binding.containerTeamStats.addView(row)
    }

    class TeamStatsData {
        var matchesCount = 0
        var wins = 0
        var totalPoints = 0
        var roundsCount = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
