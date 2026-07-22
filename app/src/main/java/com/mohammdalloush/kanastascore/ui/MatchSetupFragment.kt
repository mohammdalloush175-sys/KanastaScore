package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentMatchSetupBinding
import com.mohammdalloush.kanastascore.engine.AppThemeMode
import com.mohammdalloush.kanastascore.engine.Team

class MatchSetupFragment : Fragment() {

    private var _binding: FragmentMatchSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.contentLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel
        
        val targetScores = resources.getStringArray(R.array.target_scores)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, targetScores)
        binding.actvTargetScore.setAdapter(adapter)
        binding.actvTargetScore.setText(targetScores[2], false) // Default 5000

        if (viewModel.gameState.value != null) {
            binding.btnContinue.visibility = View.VISIBLE
            binding.btnContinue.setOnClickListener {
                mainActivity.replaceFragment(RoundInputFragment())
            }
        }

        binding.btnStart.setOnClickListener {
            if (viewModel.gameState.value != null) {
                showConfirmNewGameDialog()
            } else {
                startNewGame()
            }
        }

        binding.btnTheme.setOnClickListener {
            (activity as MainActivity).showThemeSelectionDialog()
        }
    }

    private fun startNewGame() {
        val t1Name = binding.etTeam1Name.text.toString().trim()
        val t2Name = binding.etTeam2Name.text.toString().trim()

        if (t1Name.isBlank()) {
            binding.etTeam1Name.error = getString(R.string.team_1_name)
            return
        }
        if (t2Name.isBlank()) {
            binding.etTeam2Name.error = getString(R.string.team_2_name)
            return
        }
        if (t1Name == t2Name) {
            Toast.makeText(requireContext(), R.string.error_same_team_names, Toast.LENGTH_SHORT).show()
            return
        }

        val targetScore = binding.actvTargetScore.text.toString().toIntOrNull() ?: 5000

        val team1 = Team(t1Name, emptyList())
        val team2 = Team(t2Name, emptyList())

        (activity as MainActivity).viewModel.startNewGame(listOf(team1, team2), targetScore)
        (activity as MainActivity).replaceFragment(RoundInputFragment())
    }

    private fun showConfirmNewGameDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_new_match_title)
            .setMessage(R.string.confirm_new_match_message)
            .setPositiveButton(R.string.yes) { _, _ -> 
                binding.etTeam1Name.text?.clear()
                binding.etTeam2Name.text?.clear()
                startNewGame() 
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
