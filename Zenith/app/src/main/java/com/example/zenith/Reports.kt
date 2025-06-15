package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Reports : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // Initialize buttons
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val budgetButton: LinearLayout = findViewById(R.id.budgetButton)
        val goalsButton: LinearLayout = findViewById(R.id.goalsButton)
        val educationButton: ImageView = findViewById(R.id.educationButton)

        // Initialize the "Recent Transactions" layout.
        // IMPORTANT: Ensure you have an ID 'recentTransactionsLayout' for this section in your activity_reports.xml
        val recentTransactionsLayout: LinearLayout = findViewById(R.id.recentTransactionsLayout)

        // Set click listeners for bottom navigation buttons
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        goalsButton.setOnClickListener {
            startActivity(Intent(this, Goals::class.java))
        }

        budgetButton.setOnClickListener {
            startActivity(Intent(this,MonthlyBudget::class.java))
        }

        educationButton.setOnClickListener {
            startActivity(Intent(this, FinancialLit::class.java))
        }

        // Set click listener for the "Recent Transactions" section
        recentTransactionsLayout.setOnClickListener {
            // This intent will start the TransactionHistoryActivity when the layout is clicked
            startActivity(Intent(this, TransactionHistory::class.java))
            // Optional: Add a short toast message to confirm navigation
            Toast.makeText(this, "Opening Transaction History", Toast.LENGTH_SHORT).show()
        }
    }
}
