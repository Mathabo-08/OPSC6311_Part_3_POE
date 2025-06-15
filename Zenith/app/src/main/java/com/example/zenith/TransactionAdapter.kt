package com.example.zenith

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactionList: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionName: TextView = itemView.findViewById(R.id.transactionName)
        val transactionDateTime: TextView = itemView.findViewById(R.id.transactionDateTime)
        val transactionPlace: TextView = itemView.findViewById(R.id.transactionPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = transactionList[position]
        holder.transactionName.text = currentItem.name
        holder.transactionDateTime.text = "${currentItem.date} at ${currentItem.time}"
        holder.transactionPlace.text = currentItem.place
    }

    override fun getItemCount() = transactionList.size
}