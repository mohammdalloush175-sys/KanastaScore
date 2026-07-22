package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
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

        setupToolbar(mainActivity)
        setupHeader(state, viewModel)
        setupTeamSection(binding.sectionTeam1, 0, state.teams[0])
        setupTeamSection(binding.sectionTeam2, 1, state.teams[1])

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

        // Observe for exclusive exit
        viewModel.team1RoundInput.observe(viewLifecycleOwner) { 
            updateExitUI(binding.sectionTeam1, it, 0)
        }
        viewModel.team2RoundInput.observe(viewLifecycleOwner) {
            updateExitUI(binding.sectionTeam2, it, 1)
        }
    }

    private fun setupToolbar(activity: MainActivity) {
        binding.toolbar.inflateMenu(R.menu.menu_round_input)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_history -> {
                    activity.replaceFragment(HistoryFragment(), true)
                    true
                }
                R.id.action_new_match -> {
                    showNewMatchDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showNewMatchDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_new_match_title)
            .setMessage(R.string.confirm_new_match_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                val activity = activity as MainActivity
                activity.viewModel.setGameState(null)
                activity.viewModel.clearRoundDraft()
                activity.replaceFragment(MatchSetupFragment())
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun setupHeader(state: GameState, viewModel: GameViewModel) {
        val nextRoundNumber = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
        binding.toolbar.title = getString(R.string.round_number_label, nextRoundNumber)
        
        binding.tvTargetInfo.text = getString(R.string.target_score_info, state.targetScore)

        val t1Score = viewModel.getCumulativeScore(0)
        val t1Min = viewModel.getMinMeldPoints(0)
        binding.tvTeam1Summary.text = getString(R.string.team_name_score_label, state.teams[0].name, t1Score)
        binding.tvTeam1Min.text = getString(R.string.min_points_info, t1Min)

        val t2Score = viewModel.getCumulativeScore(1)
        val t2Min = viewModel.getMinMeldPoints(1)
        binding.tvTeam2Summary.text = getString(R.string.team_name_score_label, state.teams[1].name, t2Score)
        binding.tvTeam2Min.text = getString(R.string.min_points_info, t2Min)
    }

    private fun setupTeamSection(sectionBinding: LayoutTeamRoundInputBinding, teamIndex: Int, team: Team) {
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel
        val inputLiveData = if (teamIndex == 0) viewModel.team1RoundInput else viewModel.team2RoundInput
        val initialInput = inputLiveData.value!!

        sectionBinding.tvTeamName.text = team.name

        // Scoring Mode Toggle
        sectionBinding.toggleScoringMode.check(
            if (initialInput.scoringMode == MeldScoringMode.DETAILED) R.id.btn_mode_detailed else R.id.btn_mode_direct
        )
        sectionBinding.toggleScoringMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newMode = if (checkedId == R.id.btn_mode_detailed) MeldScoringMode.DETAILED else MeldScoringMode.DIRECT_TOTAL
                inputLiveData.value = inputLiveData.value?.copy(scoringMode = newMode)
                updateModeVisibility(sectionBinding, newMode)
            }
        }
        updateModeVisibility(sectionBinding, initialInput.scoringMode)

        // Detailed Mode
        val adapter = MeldAdapter(
            initialInput.melds.toMutableList(),
            onMeldChanged = {
                val current = inputLiveData.value!!
                val newMelds = if (teamIndex == 0) team1Adapter.getMelds() else team2Adapter.getMelds()
                inputLiveData.value = current.copy(melds = newMelds)
                updateMeldTotal(sectionBinding, inputLiveData.value!!)
            },
            onMeldDeleted = { pos ->
                val current = inputLiveData.value!!
                val newMelds = current.melds.toMutableList().apply { removeAt(pos) }
                inputLiveData.value = current.copy(melds = newMelds)
                if (teamIndex == 0) team1Adapter.updateData(newMelds) else team2Adapter.updateData(newMelds)
                updateMeldTotal(sectionBinding, inputLiveData.value!!)
            }
        )
        if (teamIndex == 0) team1Adapter = adapter else team2Adapter = adapter
        sectionBinding.rvMelds.adapter = adapter
        sectionBinding.tvNoMelds.isVisible = initialInput.melds.isEmpty()

        sectionBinding.btnAddMeld.setOnClickListener {
            val current = inputLiveData.value!!
            val newMelds = current.melds.toMutableList().apply { 
                add(MeldInput(rank = CardRank.THREE, naturalCount = 0, twosCount = 0, jokersCount = 0)) 
            }
            inputLiveData.value = current.copy(melds = newMelds)
            adapter.updateData(newMelds)
            sectionBinding.tvNoMelds.isVisible = false
        }

        // Direct Mode
        sectionBinding.etDirectTotal.setText(initialInput.directInput.totalMeldPoints.toString())
        setupTextWatcher(sectionBinding.etDirectTotal) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(totalMeldPoints = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
        }
        sectionBinding.etRedCanastas.setText(initialInput.directInput.redCanastasCount.toString())
        setupTextWatcher(sectionBinding.etRedCanastas) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(redCanastasCount = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
        }
        sectionBinding.etBlackCanastas.setText(initialInput.directInput.blackCanastasCount.toString())
        setupTextWatcher(sectionBinding.etBlackCanastas) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(blackCanastasCount = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
        }

        // Red Threes
        val options = resources.getStringArray(R.array.red_threes_options)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        sectionBinding.actvRedThrees.setAdapter(spinnerAdapter)
        sectionBinding.actvRedThrees.setText(initialInput.redThreesCount.toString(), false)
        sectionBinding.actvRedThrees.setOnItemClickListener { _, _, index, _ ->
            inputLiveData.value = inputLiveData.value!!.copy(redThreesCount = index)
        }

        // Exit
        sectionBinding.switchExited.isChecked = initialInput.exited
        sectionBinding.switchExited.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != inputLiveData.value?.exited) {
                viewModel.setTeamExited(teamIndex, isChecked)
            }
        }

        // Remaining
        sectionBinding.etRemainingPoints.setText(initialInput.remainingPoints.toString())
        setupTextWatcher(sectionBinding.etRemainingPoints) {
            inputLiveData.value = inputLiveData.value!!.copy(remainingPoints = it.toIntOrNull() ?: 0)
        }

        updateMeldTotal(sectionBinding, initialInput)
    }

    private fun updateModeVisibility(sectionBinding: LayoutTeamRoundInputBinding, mode: MeldScoringMode) {
        sectionBinding.layoutDetailedMode.isVisible = (mode == MeldScoringMode.DETAILED)
        sectionBinding.layoutDirectMode.isVisible = (mode == MeldScoringMode.DIRECT_TOTAL)
    }

    private fun updateExitUI(sectionBinding: LayoutTeamRoundInputBinding, input: TeamRoundInput, teamIndex: Int) {
        val otherInput = if (teamIndex == 0) (activity as MainActivity).viewModel.team2RoundInput.value else (activity as MainActivity).viewModel.team1RoundInput.value
        
        sectionBinding.switchExited.isChecked = input.exited
        
        if (otherInput?.exited == true) {
            sectionBinding.switchExited.isEnabled = false
            val otherTeamName = (activity as MainActivity).viewModel.gameState.value?.teams?.get(if (teamIndex == 0) 1 else 0)?.name ?: ""
            sectionBinding.tvExitedHint.isVisible = true
            sectionBinding.tvExitedHint.text = getString(R.string.team_exited_hint, otherTeamName)
        } else {
            sectionBinding.switchExited.isEnabled = true
            sectionBinding.tvExitedHint.isVisible = false
        }
    }

    private fun updateMeldTotal(sectionBinding: LayoutTeamRoundInputBinding, input: TeamRoundInput) {
        val total = if (input.scoringMode == MeldScoringMode.DETAILED) {
            input.melds.sumOf { ScoreCalculator.calculateMeldPoints(it) }
        } else {
            input.directInput.totalMeldPoints
        }
        sectionBinding.tvMeldTotal.text = getString(R.string.total_meld_points, total)
        
        // Show warning in direct mode if no canasta
        if (input.scoringMode == MeldScoringMode.DIRECT_TOTAL) {
            val hasCanasta = (input.directInput.redCanastasCount + input.directInput.blackCanastasCount) > 0
            sectionBinding.tvDirectModeWarning.isVisible = !hasCanasta
        }
    }

    private fun setupTextWatcher(editText: android.widget.EditText, onTextChange: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTextChange(s?.toString() ?: "")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
