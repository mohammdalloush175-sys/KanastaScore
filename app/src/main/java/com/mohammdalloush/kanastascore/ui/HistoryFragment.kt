package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentHistoryBinding
import com.mohammdalloush.kanastascore.ui.adapters.HistoryAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupToolbar(mainActivity)

        val state = viewModel.gameState.value
        if (state != null) {
            binding.tvTeam1Name.text = state.teams[0].name
            binding.tvTeam1Total.text = viewModel.getCumulativeScore(0).toString()
            binding.tvTeam2Name.text = state.teams[1].name
            binding.tvTeam2Total.text = viewModel.getCumulativeScore(1).toString()
            binding.emptyState.isVisible = state.rounds.isEmpty()
        }

        val adapter = HistoryAdapter(
            viewModel.gameState.value?.rounds ?: emptyList(),
            onEdit = { round ->
                viewModel.editingRoundId.value = round.id
                viewModel.updateTeam1Input(round.team1Input)
                viewModel.updateTeam2Input(round.team2Input)
                mainActivity.replaceFragment(RoundInputFragment())
            },
            onDelete = { round ->
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_round_confirm)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.deleteRound(round.id)
                        val updatedRounds = viewModel.gameState.value?.rounds ?: emptyList()
                        (binding.rvHistory.adapter as HistoryAdapter).updateData(updatedRounds)
                        binding.emptyState.isVisible = updatedRounds.isEmpty()
                        // Update summary
                        binding.tvTeam1Total.text = viewModel.getCumulativeScore(0).toString()
                        binding.tvTeam2Total.text = viewModel.getCumulativeScore(1).toString()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        )
        binding.rvHistory.adapter = adapter

        binding.btnBackToRound.setOnClickListener {
            mainActivity.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupToolbar(activity: MainActivity) {
        binding.toolbar.setNavigationOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbar.inflateMenu(R.menu.menu_history)
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
