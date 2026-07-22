package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentMatchEndBinding
import com.mohammdalloush.kanastascore.engine.MatchStatus
import java.text.SimpleDateFormat
import java.util.*

class MatchEndFragment : Fragment() {

    private var _binding: FragmentMatchEndBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchEndBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel
        val match = viewModel.getActiveMatch() ?: return
        val state = match.gameState

        binding.tvMatchName.text = match.matchName
        binding.tvTeam1Name.text = state.teams[0].name
        binding.tvTeam2Name.text = state.teams[1].name

        val total1 = state.rounds.sumOf { it.team1Result.total }
        val total2 = state.rounds.sumOf { it.team2Result.total }
        
        binding.tvTeam1Total.text = total1.toString()
        binding.tvTeam2Total.text = total2.toString()
        
        val winnerName = if (total1 > total2) state.teams[0].name else state.teams[1].name
        binding.tvWinnerName.text = winnerName

        // Stats
        val roundsCount = state.rounds.size
        val maxScore1 = state.rounds.maxOfOrNull { it.team1Result.total } ?: 0
        val maxScore2 = state.rounds.maxOfOrNull { it.team2Result.total } ?: 0
        
        val redCanastas1 = state.rounds.sumOf { it.team1Input.directInput.redCanastasCount }
        val blackCanastas1 = state.rounds.sumOf { it.team1Input.directInput.blackCanastasCount }
        val redCanastas2 = state.rounds.sumOf { it.team2Input.directInput.redCanastasCount }
        val blackCanastas2 = state.rounds.sumOf { it.team2Input.directInput.blackCanastasCount }

        val statsBuilder = StringBuilder()
        statsBuilder.append(getString(R.string.rounds_count_label, roundsCount)).append("\n\n")
        
        statsBuilder.append("${state.teams[0].name}:\n")
        statsBuilder.append("  ").append(getString(R.string.max_round_score_label, maxScore1)).append("\n")
        statsBuilder.append("  ").append(getString(R.string.total_canastas_label, redCanastas1, blackCanastas1)).append("\n\n")
        
        statsBuilder.append("${state.teams[1].name}:\n")
        statsBuilder.append("  ").append(getString(R.string.max_round_score_label, maxScore2)).append("\n")
        statsBuilder.append("  ").append(getString(R.string.total_canastas_label, redCanastas2, blackCanastas2))

        binding.tvMatchStats.text = statsBuilder.toString()
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date(match.updatedAt))

        // Buttons
        binding.btnRematch.setOnClickListener {
            viewModel.reMatch()
            mainActivity.replaceFragment(RoundInputFragment())
        }
        
        binding.btnHistory.setOnClickListener {
            mainActivity.replaceFragment(HistoryFragment(), true)
        }
        
        binding.btnShare.setOnClickListener {
            com.mohammdalloush.kanastascore.utils.ShareHelper.shareMatchResult(requireContext(), match)
        }
        
        binding.btnBackMatches.setOnClickListener {
            mainActivity.replaceFragment(MatchesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
