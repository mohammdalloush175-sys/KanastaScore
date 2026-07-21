package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
                        (binding.rvHistory.adapter as HistoryAdapter).updateData(viewModel.gameState.value?.rounds ?: emptyList())
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        )
        binding.rvHistory.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
