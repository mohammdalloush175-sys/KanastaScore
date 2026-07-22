package com.mohammdalloush.kanastascore.ui.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.ItemMatchBinding
import com.mohammdalloush.kanastascore.engine.MatchStatus
import com.mohammdalloush.kanastascore.engine.SavedMatch

class MatchesAdapter(
    private var matches: List<SavedMatch>,
    private val onAction: (SavedMatch) -> Unit,
    private val onDelete: (SavedMatch) -> Unit
) : RecyclerView.Adapter<MatchesAdapter.MatchViewHolder>() {

    class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        val binding = holder.binding
        val context = holder.itemView.context

        binding.tvMatchName.text = match.matchName
        binding.tvUpdatedAt.text = context.getString(R.string.last_updated, DateUtils.getRelativeTimeSpanString(match.updatedAt))
        
        binding.tvStatus.text = if (match.status == MatchStatus.ACTIVE) context.getString(R.string.match_status_active) else context.getString(R.string.match_status_completed)
        binding.tvStatus.setBackgroundColor(if (match.status == MatchStatus.ACTIVE) context.getColor(R.color.primaryContainer) else context.getColor(R.color.surfaceVariant))
        binding.tvStatus.setTextColor(if (match.status == MatchStatus.ACTIVE) context.getColor(R.color.primary) else context.getColor(R.color.onSurfaceVariant))

        val state = match.gameState
        binding.tvTeam1Name.text = state.teams[0].name
        binding.tvTeam2Name.text = state.teams[1].name
        
        val s1 = state.rounds.sumOf { it.team1Result.total }
        val s2 = state.rounds.sumOf { it.team2Result.total }
        binding.tvTeam1Score.text = s1.toString()
        binding.tvTeam2Score.text = s2.toString()
        
        val roundNum = (state.rounds.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
        binding.tvRoundInfo.text = "الجولة $roundNum\n(${state.targetScore})"

        binding.btnAction.text = if (match.status == MatchStatus.ACTIVE) context.getString(R.string.continue_label) else context.getString(R.string.view_results)
        binding.btnAction.setOnClickListener { onAction(match) }
        binding.btnDelete.setOnClickListener { onDelete(match) }
        binding.root.setOnClickListener { onAction(match) }
    }

    override fun getItemCount(): Int = matches.size

    fun updateData(newList: List<SavedMatch>) {
        matches = newList
        notifyDataSetChanged()
    }
}
