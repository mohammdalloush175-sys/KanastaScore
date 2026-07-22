package com.mohammdalloush.kanastascore.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.ItemMeldBinding
import com.mohammdalloush.kanastascore.engine.CanastaType
import com.mohammdalloush.kanastascore.engine.CardRank
import com.mohammdalloush.kanastascore.engine.MeldInput

class MeldAdapter(
    private var melds: MutableList<MeldInput>,
    private val onMeldChanged: () -> Unit,
    private val onMeldDeleted: (Int) -> Unit
) : RecyclerView.Adapter<MeldAdapter.MeldViewHolder>() {

    fun getMelds(): List<MeldInput> = melds

    class MeldViewHolder(val binding: ItemMeldBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeldViewHolder {
        val binding = ItemMeldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeldViewHolder, position: Int) {
        val meld = melds[position]
        val context = holder.itemView.context
        val binding = holder.binding

        val ranks = context.resources.getStringArray(R.array.card_ranks)
        val rankAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, ranks)
        binding.actvRank.setAdapter(rankAdapter)
        binding.actvRank.setText(ranks[meld.rank.ordinal], false)

        binding.etNatural.setText(meld.naturalCount.toString())
        binding.etTwos.setText(meld.twosCount.toString())
        binding.etJokers.setText(meld.jokersCount.toString())

        val canastaTypes = context.resources.getStringArray(R.array.canasta_types)
        val canastaAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, canastaTypes)
        binding.actvCanastaType.setAdapter(canastaAdapter)
        
        val typeString = when (meld.canastaType) {
            CanastaType.BLACK -> canastaTypes[1]
            CanastaType.RED -> canastaTypes[2]
            null -> canastaTypes[0]
        }
        binding.actvCanastaType.setText(typeString, false)

        // Listeners
        binding.btnDeleteMeld.setOnClickListener { onMeldDeleted(holder.adapterPosition) }

        binding.actvRank.setOnItemClickListener { _, _, index, _ ->
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(rank = CardRank.entries[index])
            onMeldChanged()
        }

        binding.actvCanastaType.setOnItemClickListener { _, _, index, _ ->
            val type = when (index) {
                1 -> CanastaType.BLACK
                2 -> CanastaType.RED
                else -> null
            }
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(canastaType = type)
            onMeldChanged()
        }

        setupTextWatcher(binding.etNatural) { 
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(naturalCount = it.toIntOrNull() ?: 0)
            onMeldChanged()
        }
        setupTextWatcher(binding.etTwos) {
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(twosCount = it.toIntOrNull() ?: 0)
            onMeldChanged()
        }
        setupTextWatcher(binding.etJokers) {
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(jokersCount = it.toIntOrNull() ?: 0)
            onMeldChanged()
            updateMeldPoints(binding, melds[holder.adapterPosition])
        }

        binding.actvRank.setOnItemClickListener { _, _, index, _ ->
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(rank = CardRank.entries[index])
            onMeldChanged()
            updateMeldPoints(binding, melds[holder.adapterPosition])
        }

        setupTextWatcher(binding.etNatural) { 
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(naturalCount = it.toIntOrNull() ?: 0)
            onMeldChanged()
            updateMeldPoints(binding, melds[holder.adapterPosition])
        }
        setupTextWatcher(binding.etTwos) {
            melds[holder.adapterPosition] = melds[holder.adapterPosition].copy(twosCount = it.toIntOrNull() ?: 0)
            onMeldChanged()
            updateMeldPoints(binding, melds[holder.adapterPosition])
        }

        updateMeldPoints(binding, meld)
    }

    private fun updateMeldPoints(binding: ItemMeldBinding, meld: MeldInput) {
        val points = (meld.naturalCount * com.mohammdalloush.kanastascore.engine.ScoreCalculator.cardValue(meld.rank)) +
                (meld.twosCount * 20) + (meld.jokersCount * 50)
        binding.tvMeldPointsLabel.text = binding.root.context.getString(R.string.meld_points_label, points)
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

    override fun getItemCount(): Int = melds.size

    fun updateData(newMelds: List<MeldInput>) {
        melds = newMelds.toMutableList()
        notifyDataSetChanged()
    }
}
