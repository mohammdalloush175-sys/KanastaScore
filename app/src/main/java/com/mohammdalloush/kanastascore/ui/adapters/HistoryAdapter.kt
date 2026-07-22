package com.mohammdalloush.kanastascore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.ItemHistoryRoundBinding
import com.mohammdalloush.kanastascore.engine.RoundRecord

class HistoryAdapter(
    private var rounds: List<RoundRecord>,
    private val onEdit: (RoundRecord) -> Unit,
    private val onDelete: (RoundRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryRoundBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val round = rounds[position]
        val binding = holder.binding
        val context = holder.itemView.context

        binding.tvRoundTitle.text = context.getString(R.string.round_item_title, round.roundNumber)
        
        binding.tvTeam1RoundScore.text = (if (round.team1Result.total >= 0) "+" else "") + round.team1Result.total.toString()
        binding.tvTeam1RoundScore.setTextColor(
            if (round.team1Result.total >= 0) context.getColor(R.color.positiveScore)
            else context.getColor(R.color.negativeScore)
        )

        binding.tvTeam2RoundScore.text = (if (round.team2Result.total >= 0) "+" else "") + round.team2Result.total.toString()
        binding.tvTeam2RoundScore.setTextColor(
            if (round.team2Result.total >= 0) context.getColor(R.color.positiveScore)
            else context.getColor(R.color.negativeScore)
        )

        // Cumulative calculation up to this round
        var cum1 = 0
        var cum2 = 0
        for (i in 0..position) {
            cum1 += rounds[i].team1Result.total
            cum2 += rounds[i].team2Result.total
        }
        binding.tvTeam1Cumulative.text = context.getString(R.string.previous_total, cum1)
        binding.tvTeam2Cumulative.text = context.getString(R.string.previous_total, cum2)

        binding.root.setOnClickListener { onEdit(round) }
        binding.btnDelete.setOnClickListener { onDelete(round) }
    }

    override fun getItemCount(): Int = rounds.size

    fun updateData(newRounds: List<RoundRecord>) {
        rounds = newRounds
        notifyDataSetChanged()
    }
}
