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
import com.mohammdalloush.kanastascore.databinding.FragmentResultBinding
import com.mohammdalloush.kanastascore.engine.*

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
        val match = viewModel.getActiveMatch() ?: return
        val state = match.gameState

        val team1Input = viewModel.team1RoundInput.value!!
        val team2Input = viewModel.team2RoundInput.value!!

        val res1 = ScoreCalculator.calculateScore(team1Input)
        val res2 = ScoreCalculator.calculateScore(team2Input)

        setupTeamUI(0, state.teams[0].name, team1Input, res1, binding.tvTeam1Name, binding.tvTeam1SummaryName, 
            binding.tvTeam1RoundTotal, binding.containerTeam1Details, binding.tvTeam1Progression, 
            binding.cardTeam1Warning, viewModel.getCumulativeScore(0))
            
        setupTeamUI(1, state.teams[1].name, team2Input, res2, binding.tvTeam2Name, binding.tvTeam2SummaryName, 
            binding.tvTeam2RoundTotal, binding.containerTeam2Details, binding.tvTeam2Progression, 
            binding.cardTeam2Warning, viewModel.getCumulativeScore(1))

        // Winner check
        val prev1 = viewModel.getCumulativeScore(0)
        val prev2 = viewModel.getCumulativeScore(1)
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

        viewModel.isRecordingRound.observe(viewLifecycleOwner) { isRecording ->
            binding.btnRecord.isEnabled = !isRecording
        }

        binding.btnRecord.setOnClickListener {
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
            
            val updatedMatch = viewModel.getActiveMatch()
            if (updatedMatch?.status == MatchStatus.COMPLETED) {
                mainActivity.replaceFragment(MatchEndFragment())
            } else {
                mainActivity.replaceFragment(RoundInputFragment())
            }
        }
    }

    private fun setupTeamUI(
        teamIndex: Int,
        teamName: String,
        input: TeamRoundInput,
        res: ScoreBreakdown,
        nameTv: TextView,
        summaryNameTv: TextView,
        totalTv: TextView,
        detailsContainer: LinearLayout,
        progressionTv: TextView,
        warningCard: View,
        prevTotal: Int
    ) {
        nameTv.text = teamName
        summaryNameTv.text = teamName
        
        totalTv.text = (if (res.total >= 0) "+" else "") + res.total.toString()
        totalTv.setTextColor(
            if (res.total >= 0) requireContext().getColor(R.color.positiveScore)
            else requireContext().getColor(R.color.negativeScore)
        )

        warningCard.isVisible = !res.hasValidCanasta
        
        detailsContainer.removeAllViews()
        
        if (!res.hasValidCanasta) {
            addDetailRow(detailsContainer, getString(R.string.no_canasta_penalty_label), res.noCanastaPenalty, false)
            if (input.redThreesCount > 0) {
                addDetailRow(detailsContainer, getString(R.string.red_threes_penalty_label), res.redThreesScore, false)
            }
            // Add extra info rows for no canasta
            addDetailInfoRow(detailsContainer, getString(R.string.no_meld_points_calc))
            addDetailInfoRow(detailsContainer, getString(R.string.no_burning_calc))
        } else {
            addDetailRow(detailsContainer, getString(R.string.meld_points_detail), res.meldPoints, true)
            
            if (input.directInput.redCanastasCount > 0) {
                addDetailRow(detailsContainer, getString(R.string.red_canasta_count_label), input.directInput.redCanastasCount, null)
                addDetailRow(detailsContainer, getString(R.string.red_canasta_bonus_label), res.redCanastaBonus, true)
            }
            
            if (input.directInput.blackCanastasCount > 0) {
                addDetailRow(detailsContainer, getString(R.string.black_canasta_count_label), input.directInput.blackCanastasCount, null)
                addDetailRow(detailsContainer, getString(R.string.black_canasta_bonus_label), res.blackCanastaBonus, true)
            }
            
            if (input.redThreesCount > 0) {
                addDetailRow(detailsContainer, getString(R.string.red_threes_count_label), input.redThreesCount, null)
                addDetailRow(detailsContainer, getString(R.string.red_threes_bonus_label), res.redThreesScore, res.redThreesScore >= 0)
            }
            
            if (res.exitBonus > 0) {
                addDetailRow(detailsContainer, getString(R.string.exit_bonus_label), res.exitBonus, true)
            }
            
            if (res.remainingCardsPenalty < 0) {
                addDetailRow(detailsContainer, getString(R.string.burning_penalty_label), res.remainingCardsPenalty, false)
            }
        }
        
        // Final Round Total Row
        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.spacing_8)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_4)
            }
            setBackgroundColor(requireContext().getColor(R.color.divider))
        }
        detailsContainer.addView(divider)
        addDetailRow(detailsContainer, getString(R.string.round_total_label), res.total, res.total >= 0, isBold = true)

        progressionTv.text = getString(R.string.total_progression, prevTotal, prevTotal + res.total)
    }

    private fun addDetailInfoRow(container: LinearLayout, message: String) {
        val tv = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.spacing_4)
            }
            text = "• $message"
            textSize = 12f
            alpha = 0.7f
        }
        container.addView(tv)
    }

    private fun addDetailRow(container: LinearLayout, label: String, value: Int, isPositive: Boolean?, isBold: Boolean = false) {
        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_score_detail_row, container, false)
        val tvLabel: TextView = row.findViewById(R.id.tv_label)
        val tvValue: TextView = row.findViewById(R.id.tv_value)
        
        tvLabel.text = label
        
        val valueText = when {
            isPositive == null -> value.toString()
            value > 0 -> "+$value"
            else -> value.toString()
        }
        tvValue.text = valueText
        
        if (isPositive != null) {
            tvValue.setTextColor(
                if (isPositive) requireContext().getColor(R.color.positiveScore)
                else requireContext().getColor(R.color.negativeScore)
            )
        }
        
        if (isBold) {
            tvLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall)
            tvValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall)
            tvValue.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        container.addView(row)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
