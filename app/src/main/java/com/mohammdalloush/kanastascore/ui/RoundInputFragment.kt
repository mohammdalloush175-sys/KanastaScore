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
import com.google.android.material.snackbar.Snackbar
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentRoundInputBinding
import com.mohammdalloush.kanastascore.databinding.LayoutTeamRoundInputBinding
import com.mohammdalloush.kanastascore.engine.*
import com.mohammdalloush.kanastascore.viewmodel.GameViewModel

class RoundInputFragment : Fragment() {

    private var _binding: FragmentRoundInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoundInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel

        val match = viewModel.getActiveMatch() ?: return
        val state = match.gameState

        setupHeader(state, viewModel)
        setupTeamSection(binding.sectionTeam1, 0, state.teams[0])
        setupTeamSection(binding.sectionTeam2, 1, state.teams[1])

        binding.btnPreview.setOnClickListener {
            val t1 = viewModel.team1RoundInput.value!!
            val t2 = viewModel.team2RoundInput.value!!
            
            val error = ScoreValidator.validateRound(t1, t2)
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            } else {
                val warnings = ScoreValidator.getWarnings(t1, t2)
                if (warnings.isNotEmpty()) {
                    showWarningsDialog(warnings) {
                        mainActivity.replaceFragment(ResultFragment(), true)
                    }
                } else {
                    mainActivity.replaceFragment(ResultFragment(), true)
                }
            }
        }

        // Observe for exclusive changes
        viewModel.team1RoundInput.observe(viewLifecycleOwner) { 
            updateExitUI(binding.sectionTeam1, it, 0)
            updateRedThreesUI(binding.sectionTeam1, it)
            validateCurrentInputs()
        }
        viewModel.team2RoundInput.observe(viewLifecycleOwner) {
            updateExitUI(binding.sectionTeam2, it, 1)
            updateRedThreesUI(binding.sectionTeam2, it)
            validateCurrentInputs()
        }

        viewModel.lastRecordedRoundNumber.observe(viewLifecycleOwner) { roundNumber ->
            if (roundNumber != null) {
                showUndoSnackbar(roundNumber)
            }
        }
    }

    private fun showUndoSnackbar(roundNumber: Int) {
        val viewModel = (activity as MainActivity).viewModel
        Snackbar.make(binding.root, getString(R.string.round_recorded_msg, roundNumber), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo_label) {
                viewModel.undoLastRound()
                Toast.makeText(requireContext(), R.string.undo_label, Toast.LENGTH_SHORT).show()
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        viewModel.clearUndo()
                    }
                }
            })
            .show()
        viewModel.consumeRecordEvent() // Clear so it doesn't show again on rotation
    }

    private fun showWarningsDialog(warnings: List<String>, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.review_data_title)
            .setMessage(warnings.joinToString("\n\n"))
            .setPositiveButton(R.string.continue_preview) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.edit_round, null)
            .show()
    }

    private fun setupHeader(state: GameState, viewModel: GameViewModel) {
        val nextRoundNumber = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
        binding.tvRoundTitle.text = getString(R.string.round_number_label, nextRoundNumber)
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
        
        // Legacy Migration: If input is DETAILED, migrate it to DIRECT_TOTAL summary
        val currentInput = inputLiveData.value!!
        if (currentInput.scoringMode == MeldScoringMode.DETAILED) {
            val totalMeldPoints = currentInput.melds.sumOf { ScoreCalculator.calculateMeldPoints(it) }
            val redCanastas = currentInput.melds.count { it.canastaType == CanastaType.RED }
            val blackCanastas = currentInput.melds.count { it.canastaType == CanastaType.BLACK }
            
            val migratedInput = currentInput.copy(
                scoringMode = MeldScoringMode.DIRECT_TOTAL,
                directInput = DirectMeldScoreInput(
                    totalMeldPoints = totalMeldPoints,
                    redCanastasCount = redCanastas,
                    blackCanastasCount = blackCanastas
                )
            )
            inputLiveData.value = migratedInput
            viewModel.saveCurrentDraft()
        }
        
        val initialInput = inputLiveData.value!!

        sectionBinding.tvTeamName.text = team.name

        // Direct Mode Inputs
        sectionBinding.etDirectTotal.setText(initialInput.directInput.totalMeldPoints.toString())
        setupTextWatcher(sectionBinding.etDirectTotal) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(totalMeldPoints = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
            viewModel.saveCurrentDraft()
        }
        
        sectionBinding.etRedCanastas.setText(initialInput.directInput.redCanastasCount.toString())
        setupTextWatcher(sectionBinding.etRedCanastas) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(redCanastasCount = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
            viewModel.saveCurrentDraft()
        }
        
        sectionBinding.etBlackCanastas.setText(initialInput.directInput.blackCanastasCount.toString())
        setupTextWatcher(sectionBinding.etBlackCanastas) {
            val current = inputLiveData.value!!
            inputLiveData.value = current.copy(directInput = current.directInput.copy(blackCanastasCount = it.toIntOrNull() ?: 0))
            updateMeldTotal(sectionBinding, inputLiveData.value!!)
            viewModel.saveCurrentDraft()
        }

        // Shortcuts for Meld Points
        sectionBinding.btnMeldPlus50.setOnClickListener { addMeldPoints(sectionBinding, 50) }
        sectionBinding.btnMeldPlus100.setOnClickListener { addMeldPoints(sectionBinding, 100) }
        sectionBinding.btnMeldPlus200.setOnClickListener { addMeldPoints(sectionBinding, 200) }
        sectionBinding.btnMeldClear.setOnClickListener {
            val current = sectionBinding.etDirectTotal.text.toString().toIntOrNull() ?: 0
            if (current > 500) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_clear)
                    .setMessage(R.string.confirm_clear_meld_msg)
                    .setPositiveButton(R.string.yes) { _, _ -> sectionBinding.etDirectTotal.setText("0") }
                    .setNegativeButton(R.string.no, null)
                    .show()
            } else {
                sectionBinding.etDirectTotal.setText("0")
            }
        }

        // Shortcuts for Canastas
        sectionBinding.btnRedPlus.setOnClickListener { incrementEditText(sectionBinding.etRedCanastas) }
        sectionBinding.btnRedMinus.setOnClickListener { decrementEditText(sectionBinding.etRedCanastas) }
        sectionBinding.btnBlackPlus.setOnClickListener { incrementEditText(sectionBinding.etBlackCanastas) }
        sectionBinding.btnBlackMinus.setOnClickListener { decrementEditText(sectionBinding.etBlackCanastas) }

        // Red Threes Toggle Group
        updateThreesToggle(sectionBinding, initialInput.redThreesCount)
        sectionBinding.toggleGroupThrees.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val count = when (checkedId) {
                    sectionBinding.btnThrees0.id -> 0
                    sectionBinding.btnThrees1.id -> 1
                    sectionBinding.btnThrees2.id -> 2
                    sectionBinding.btnThrees3.id -> 3
                    sectionBinding.btnThrees4.id -> 4
                    else -> 0
                }
                if (count != inputLiveData.value?.redThreesCount) {
                    viewModel.setRedThrees(teamIndex, count)
                }
            }
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
            viewModel.saveCurrentDraft()
        }

        // Shortcuts for Burning
        sectionBinding.btnBurnPlus25.setOnClickListener { addBurnPoints(sectionBinding, 25) }
        sectionBinding.btnBurnPlus50.setOnClickListener { addBurnPoints(sectionBinding, 50) }
        sectionBinding.btnBurnPlus100.setOnClickListener { addBurnPoints(sectionBinding, 100) }
        sectionBinding.btnBurnClear.setOnClickListener {
            val current = sectionBinding.etRemainingPoints.text.toString().toIntOrNull() ?: 0
            if (current > 300) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_clear)
                    .setMessage(R.string.confirm_clear_burn_msg)
                    .setPositiveButton(R.string.yes) { _, _ -> sectionBinding.etRemainingPoints.setText("0") }
                    .setNegativeButton(R.string.no, null)
                    .show()
            } else {
                sectionBinding.etRemainingPoints.setText("0")
            }
        }

        updateMeldTotal(sectionBinding, initialInput)
    }

    private fun updateExitUI(sectionBinding: LayoutTeamRoundInputBinding, input: TeamRoundInput, teamIndex: Int) {
        val otherInput = if (teamIndex == 0) (activity as MainActivity).viewModel.team2RoundInput.value else (activity as MainActivity).viewModel.team1RoundInput.value
        
        sectionBinding.switchExited.isChecked = input.exited
        
        if (otherInput?.exited == true) {
            sectionBinding.switchExited.isEnabled = false
            val otherTeamName = (activity as MainActivity).viewModel.getActiveMatch()?.gameState?.teams?.get(if (teamIndex == 0) 1 else 0)?.name ?: ""
            sectionBinding.tvExitedHint.isVisible = true
            sectionBinding.tvExitedHint.text = getString(R.string.team_exited_hint, otherTeamName)
        } else {
            sectionBinding.switchExited.isEnabled = true
            sectionBinding.tvExitedHint.isVisible = false
        }
    }

    private fun updateRedThreesUI(sectionBinding: LayoutTeamRoundInputBinding, input: TeamRoundInput) {
        updateThreesToggle(sectionBinding, input.redThreesCount)
        sectionBinding.tvRedThreesStatus.text = if (input.isRedThreesManual) {
            getString(R.string.red_threes_manual)
        } else {
            getString(R.string.red_threes_suggested)
        }
    }

    private fun validateCurrentInputs() {
        val viewModel = (activity as MainActivity).viewModel
        val t1 = viewModel.team1RoundInput.value ?: return
        val t2 = viewModel.team2RoundInput.value ?: return
        
        val error = ScoreValidator.validateRound(t1, t2)
        if (error != null && error.contains(getString(R.string.error_red_threes_sum))) {
            binding.btnPreview.isEnabled = false
            binding.tvErrorMsg.isVisible = true
            binding.tvErrorMsg.text = error
        } else {
            binding.btnPreview.isEnabled = true
            binding.tvErrorMsg.isVisible = false
        }
    }

    private fun updateThreesToggle(sectionBinding: LayoutTeamRoundInputBinding, count: Int) {
        val buttonId = when (count) {
            0 -> sectionBinding.btnThrees0.id
            1 -> sectionBinding.btnThrees1.id
            2 -> sectionBinding.btnThrees2.id
            3 -> sectionBinding.btnThrees3.id
            4 -> sectionBinding.btnThrees4.id
            else -> sectionBinding.btnThrees0.id
        }
        if (sectionBinding.toggleGroupThrees.checkedButtonId != buttonId) {
            sectionBinding.toggleGroupThrees.check(buttonId)
        }
    }

    private fun addMeldPoints(sectionBinding: LayoutTeamRoundInputBinding, amount: Int) {
        val current = sectionBinding.etDirectTotal.text.toString().toIntOrNull() ?: 0
        sectionBinding.etDirectTotal.setText((current + amount).toString())
    }

    private fun addBurnPoints(sectionBinding: LayoutTeamRoundInputBinding, amount: Int) {
        val current = sectionBinding.etRemainingPoints.text.toString().toIntOrNull() ?: 0
        sectionBinding.etRemainingPoints.setText((current + amount).toString())
    }

    private fun incrementEditText(editText: android.widget.EditText) {
        val current = editText.text.toString().toIntOrNull() ?: 0
        editText.setText((current + 1).toString())
    }

    private fun decrementEditText(editText: android.widget.EditText) {
        val current = editText.text.toString().toIntOrNull() ?: 0
        if (current > 0) {
            editText.setText((current - 1).toString())
        }
    }

    private fun updateMeldTotal(sectionBinding: LayoutTeamRoundInputBinding, input: TeamRoundInput) {
        val total = input.directInput.totalMeldPoints
        sectionBinding.tvMeldTotal.text = getString(R.string.total_meld_points, total)
        
        val hasCanasta = (input.directInput.redCanastasCount + input.directInput.blackCanastasCount) > 0
        sectionBinding.tvDirectModeWarning.isVisible = !hasCanasta
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
