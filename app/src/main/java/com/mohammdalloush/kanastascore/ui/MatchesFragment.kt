package com.mohammdalloush.kanastascore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentMatchesBinding
import com.mohammdalloush.kanastascore.engine.MatchStatus
import com.mohammdalloush.kanastascore.ui.adapters.MatchesAdapter

class MatchesFragment : Fragment() {

    private var _binding: FragmentMatchesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val viewModel = mainActivity.viewModel

        val adapter = MatchesAdapter(
            viewModel.matchesStore.value?.matches ?: emptyList(),
            onAction = { match ->
                viewModel.setActiveMatch(match.id)
                if (match.status == MatchStatus.ACTIVE) {
                    mainActivity.replaceFragment(RoundInputFragment())
                } else {
                    mainActivity.replaceFragment(HistoryFragment())
                }
            },
            onDelete = { match ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_match)
                    .setMessage(R.string.confirm_delete_match)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.deleteMatch(match.id)
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        )
        binding.rvMatches.adapter = adapter

        viewModel.matchesStore.observe(viewLifecycleOwner) { store ->
            val matches = store.matches
            adapter.updateData(matches)
            binding.emptyState.isVisible = matches.isEmpty()
            binding.rvMatches.isVisible = matches.isNotEmpty()
        }

        binding.btnStartFirst.setOnClickListener {
            mainActivity.replaceFragment(MatchSetupFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
