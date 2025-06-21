package com.example.zenith

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class SavingsGoalsAdapter(private val savingsGoalsList: List<SavingsGoal>) :
    RecyclerView.Adapter<SavingsGoalsAdapter.SavingsGoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsGoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal, parent, false)
        return SavingsGoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavingsGoalViewHolder, position: Int) {
        val currentGoal = savingsGoalsList[position]
        holder.goalTitleTextView.text = currentGoal.title

        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA")) // For South African Rand (R)
        holder.monthlyAmountTextView.text = "Monthly Amount: ${format.format(currentGoal.monthlyAmount ?: 0.0)}"
        holder.durationTextView.text = "Duration: ${currentGoal.duration ?: 0} months"
    }

    override fun getItemCount(): Int {
        return savingsGoalsList.size
    }

    class SavingsGoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalTitleTextView: TextView = itemView.findViewById(R.id.goalTitleTextView)
        val monthlyAmountTextView: TextView = itemView.findViewById(R.id.monthlyAmountTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
    }
}