package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class GameficationRewards : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamefication_rewards)

        // Initialize buttons
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val budgetButton: LinearLayout = findViewById(R.id.budgetButton)
        val expensesButton: LinearLayout = findViewById(R.id.expensesButton)
        val goalsButton: LinearLayout = findViewById(R.id.goalsButton)
        val educationButton: LinearLayout = findViewById(R.id.educationButton)

        // Set click listeners for buttons
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        budgetButton.setOnClickListener {
            val intent = Intent(this, MonthlyBudget::class.java)
            startActivity(intent)
        }

        expensesButton.setOnClickListener {
            val intent = Intent(this, Reports::class.java)
            startActivity(intent)
        }

        goalsButton.setOnClickListener {
            val intent = Intent(this, Goals::class.java)
            startActivity(intent)
        }

        educationButton.setOnClickListener {
            val intent = Intent(this, FinancialLit::class.java)
            startActivity(intent)
        }
    }
}