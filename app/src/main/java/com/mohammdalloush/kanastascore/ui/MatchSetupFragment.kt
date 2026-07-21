package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentMatchSetupBinding
import com.mohammdalloush.kanastascore.engine.Team
import com.mohammdalloush.kanastascore.utils.StorageHelper

class MatchSetupFragment : Fragment() {

    private var _binding: FragmentMatchSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        val targetScores = resources.getStringArray(R.array.target_scores)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, targetScores)
        binding.actvTargetScore.setAdapter(adapter)
        binding.actvTargetScore.setText(targetScores[2], false) // Default 5000

        if (mainActivity.viewModel.gameState.value != null) {
            binding.btnContinue.visibility = View.VISIBLE
            binding.btnContinue.setOnClickListener {
                mainActivity.replaceFragment(RoundInputFragment())
            }
        }

        binding.btnStart.setOnClickListener {
            if (mainActivity.viewModel.gameState.value != null) {
                showConfirmNewGameDialog()
            } else {
                startNewGame()
            }
        }
    }

    private fun startNewGame() {
        val p1 = binding.etPlayer1.text.toString()
        val p2 = binding.etPlayer2.text.toString()
        val p3 = binding.etPlayer3.text.toString()
        val p4 = binding.etPlayer4.text.toString()

        if (p1.isBlank() || p2.isBlank() || p3.isBlank() || p4.isBlank()) {
            binding.etPlayer1.error = getString(R.string.error_player_names_required)
            return
        }

        val team1Name = binding.etTeam1Name.text.toString().ifBlank { "$p1 & $p3" }
        val team2Name = binding.etTeam2Name.text.toString().ifBlank { "$p2 & $p4" }
        val targetScore = binding.actvTargetScore.text.toString().toIntOrNull() ?: 5000

        val team1 = Team(team1Name, listOf(p1, p3))
        val team2 = Team(team2Name, listOf(p2, p4))

        (activity as MainActivity).viewModel.startNewGame(listOf(team1, team2), targetScore)
        (activity as MainActivity).replaceFragment(RoundInputFragment())
    }

    private fun showConfirmNewGameDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_match)
            .setMessage(R.string.confirm_new_game)
            .setPositiveButton(R.string.yes) { _, _ -> startNewGame() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
