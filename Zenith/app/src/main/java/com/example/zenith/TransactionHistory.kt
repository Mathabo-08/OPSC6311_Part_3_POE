package com.example.zenith

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionHistory : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter // We'll create this adapter
    private val transactionList = mutableListOf<Transaction>() // We'll create this data class
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        recyclerView = findViewById(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchTransactions()
    }

    private fun fetchTransactions() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Order by timestamp, newest first
                .get()
                .addOnSuccessListener { documents ->
                    transactionList.clear()
                    for (document in documents) {
                        try {
                            val name = document.getString("name") ?: "N/A"
                            val date = document.getString("date") ?: "N/A"
                            val time = document.getString("time") ?: "N/A"
                            val place = document.getString("place") ?: "N/A"
                            // You might also want to fetch amount or other relevant fields
                            val transaction = Transaction(name, date, time, place)
                            transactionList.add(transaction)
                        } catch (e: Exception) {
                            Log.e("TransactionHistory", "Error parsing transaction document: ${document.id}", e)
                        }
                    }
                    transactionAdapter.notifyDataSetChanged()
                    if (transactionList.isEmpty()) {
                        Toast.makeText(this, "No transactions found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching transactions: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("TransactionHistory", "Error fetching transactions", exception)
                }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            Log.e("TransactionHistory", "User is null when fetching transactions.")
        }
    }
}