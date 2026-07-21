package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentRoundInputBinding
import com.mohammdalloush.kanastascore.databinding.LayoutTeamRoundInputBinding
import com.mohammdalloush.kanastascore.engine.*
import com.mohammdalloush.kanastascore.ui.adapters.MeldAdapter
import com.mohammdalloush.kanastascore.viewmodel.GameViewModel

class RoundInputFragment : Fragment() {

    private var _binding: FragmentRoundInputBinding? = null
    private val binding get() = _binding!!

    private lateinit var team1Adapter: MeldAdapter
    private lateinit var team2Adapter: MeldAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoundInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel

        val state = viewModel.gameState.value ?: return

        setupHeader(state, viewModel)
        setupTeamSection(binding.sectionTeam1, 0, state.teams[0])
        setupTeamSection(binding.sectionTeam2, 1, state.teams[1])

        binding.btnHistory.setOnClickListener {
            mainActivity.replaceFragment(HistoryFragment(), true)
        }

        binding.btnPreview.setOnClickListener {
            val error = ScoreValidator.validateRound(
                viewModel.team1RoundInput.value!!,
                viewModel.team2RoundInput.value!!
            )
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            } else {
                mainActivity.replaceFragment(ResultFragment(), true)
            }
        }
    }

    private fun setupHeader(state: GameState, viewModel: GameViewModel) {
        val nextRoundNumber = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
        binding.tvRoundNumber.text = getString(R.string.round_number_label, nextRoundNumber)
        binding.tvTargetScore.text = getString(R.string.target_score_info, state.targetScore)

        val t1Score = viewModel.getCumulativeScore(0)
        val t1Min = viewModel.getMinMeldPoints(0)
        binding.tvTeam1Total.text = getString(R.string.team_summary_header, state.teams[0].name, t1Score, t1Min)

        val t2Score = viewModel.getCumulativeScore(1)
        val t2Min = viewModel.getMinMeldPoints(1)
        binding.tvTeam2Total.text = getString(R.string.team_summary_header, state.teams[1].name, t2Score, t2Min)
    }

    private fun setupTeamSection(sectionBinding: LayoutTeamRoundInputBinding, teamIndex: Int, team: Team) {
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel
        val inputData = if (teamIndex == 0) viewModel.team1RoundInput else viewModel.team2RoundInput

        sectionBinding.tvTeamName.text = team.name

        val adapter = MeldAdapter(
            inputData.value!!.melds.toMutableList(),
            onMeldChanged = {
                val current = inputData.value!!
                val newMelds = if (teamIndex == 0) team1Adapter.getMelds() else team2Adapter.getMelds()
                inputData.value = current.copy(melds = newMelds)
                updateMeldTotal(sectionBinding, newMelds)
            },
            onMeldDeleted = { pos ->
                val current = inputData.value!!
                val newMelds = current.melds.toMutableList().apply { removeAt(pos) }
                inputData.value = current.copy(melds = newMelds)
                if (teamIndex == 0) team1Adapter.updateData(newMelds) else team2Adapter.updateData(newMelds)
                updateMeldTotal(sectionBinding, newMelds)
            }
        )

        if (teamIndex == 0) team1Adapter = adapter else team2Adapter = adapter
        sectionBinding.rvMelds.adapter = adapter

        sectionBinding.btnAddMeld.setOnClickListener {
            val current = inputData.value!!
            val newMelds = current.melds.toMutableList().apply { 
                add(MeldInput(rank = CardRank.THREE, naturalCount = 0, twosCount = 0, jokersCount = 0)) 
            }
            inputData.value = current.copy(melds = newMelds)
            adapter.updateData(newMelds)
        }

        val options = resources.getStringArray(R.array.red_threes_options)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        sectionBinding.actvRedThrees.setAdapter(spinnerAdapter)
        sectionBinding.actvRedThrees.setText(inputData.value!!.redThreesCount.toString(), false)
        sectionBinding.actvRedThrees.setOnItemClickListener { _, _, index, _ ->
            inputData.value = inputData.value!!.copy(redThreesCount = index)
        }

        sectionBinding.switchExited.isChecked = inputData.value!!.exited
        sectionBinding.switchExited.setOnCheckedChangeListener { _, isChecked ->
            inputData.value = inputData.value!!.copy(exited = isChecked)
        }

        sectionBinding.etRemainingPoints.setText(inputData.value!!.remainingPoints.toString())
        sectionBinding.etRemainingPoints.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                inputData.value = inputData.value!!.copy(remainingPoints = s?.toString()?.toIntOrNull() ?: 0)
            }
        })

        updateMeldTotal(sectionBinding, inputData.value!!.melds)
    }

    private fun updateMeldTotal(sectionBinding: LayoutTeamRoundInputBinding, melds: List<MeldInput>) {
        val total = melds.sumOf { ScoreCalculator.calculateMeldPoints(it) }
        sectionBinding.tvMeldTotal.text = getString(R.string.total_meld_points, total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
