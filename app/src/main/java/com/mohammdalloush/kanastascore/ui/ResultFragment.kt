package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentResultBinding
import com.mohammdalloush.kanastascore.engine.RoundRecord
import com.mohammdalloush.kanastascore.engine.ScoreCalculator

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel
        val state = viewModel.gameState.value ?: return

        val team1Input = viewModel.team1RoundInput.value!!
        val team2Input = viewModel.team2RoundInput.value!!

        val res1 = ScoreCalculator.calculateScore(team1Input)
        val res2 = ScoreCalculator.calculateScore(team2Input)

        binding.tvTeam1Name.text = state.teams[0].name
        binding.tvTeam1ResultDetails.text = res1.details.joinToString("\n")
        
        val prev1 = viewModel.getCumulativeScore(0)
        binding.tvTeam1Progression.text = getString(R.string.previous_total, prev1) + " -> " + getString(R.string.new_total, prev1 + res1.total)

        binding.tvTeam2Name.text = state.teams[1].name
        binding.tvTeam2ResultDetails.text = res2.details.joinToString("\n")

        val prev2 = viewModel.getCumulativeScore(1)
        binding.tvTeam2Progression.text = getString(R.string.previous_total, prev2) + " -> " + getString(R.string.new_total, prev2 + res2.total)

        // Winner check
        val final1 = prev1 + res1.total
        val final2 = prev2 + res2.total
        if (final1 >= state.targetScore || final2 >= state.targetScore) {
            if (final1 != final2) {
                binding.tvWinner.visibility = View.VISIBLE
                val winnerName = if (final1 > final2) state.teams[0].name else state.teams[1].name
                binding.tvWinner.text = getString(R.string.winner_is, winnerName)
            }
        }

        binding.btnEdit.setOnClickListener {
            mainActivity.onBackPressed()
        }

        binding.btnRecord.setOnClickListener {
            binding.btnRecord.isEnabled = false
            val nextRoundNumber = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
            val record = RoundRecord(
                id = viewModel.editingRoundId.value ?: java.util.UUID.randomUUID().toString(),
                roundNumber = nextRoundNumber,
                team1Input = team1Input,
                team2Input = team2Input,
                team1Result = res1,
                team2Result = res2
            )
            viewModel.addRound(record)
            viewModel.clearRoundDraft()
            mainActivity.replaceFragment(RoundInputFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
