package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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

        val nextRoundNumber = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        binding.toolbar.title = getString(R.string.result_title, nextRoundNumber)
        binding.toolbar.inflateMenu(R.menu.menu_result)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_new_match -> {
                    showNewMatchDialog()
                    true
                }
                R.id.action_theme -> {
                    (activity as MainActivity).showThemeSelectionDialog()
                    true
                }
                else -> false
            }
        }

        val team1Input = viewModel.team1RoundInput.value!!
        val team2Input = viewModel.team2RoundInput.value!!

        val res1 = ScoreCalculator.calculateScore(team1Input)
        val res2 = ScoreCalculator.calculateScore(team2Input)

        // Team 1 UI
        binding.tvTeam1Name.text = state.teams[0].name
        binding.tvTeam1SummaryName.text = state.teams[0].name
        binding.tvTeam1RoundTotal.text = (if (res1.total >= 0) "+" else "") + res1.total.toString()
        binding.tvTeam1RoundTotal.setTextColor(
            if (res1.total >= 0) requireContext().getColor(R.color.positiveScore)
            else requireContext().getColor(R.color.negativeScore)
        )
        binding.tvTeam1ResultDetails.text = res1.details.joinToString("\n")
        val prev1 = viewModel.getCumulativeScore(0)
        binding.tvTeam1Progression.text = getString(R.string.total_progression, prev1, prev1 + res1.total)

        // Team 2 UI
        binding.tvTeam2Name.text = state.teams[1].name
        binding.tvTeam2SummaryName.text = state.teams[1].name
        binding.tvTeam2RoundTotal.text = (if (res2.total >= 0) "+" else "") + res2.total.toString()
        binding.tvTeam2RoundTotal.setTextColor(
            if (res2.total >= 0) requireContext().getColor(R.color.positiveScore)
            else requireContext().getColor(R.color.negativeScore)
        )
        binding.tvTeam2ResultDetails.text = res2.details.joinToString("\n")
        val prev2 = viewModel.getCumulativeScore(1)
        binding.tvTeam2Progression.text = getString(R.string.total_progression, prev2, prev2 + res2.total)

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
            mainActivity.onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRecord.setOnClickListener {
            binding.btnRecord.isEnabled = false
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

    private fun showNewMatchDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_new_match_title)
            .setMessage(R.string.confirm_new_match_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                val activity = activity as MainActivity
                activity.viewModel.startNewMatch()
                activity.replaceFragment(MatchSetupFragment())
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
