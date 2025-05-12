package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class Goals : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Initialize buttons
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val budgetButton: LinearLayout = findViewById(R.id.budgetButton)
        val expensesButton: LinearLayout = findViewById(R.id.expensesButton)
        val educationButton: LinearLayout = findViewById(R.id.educationButton)

        // Set click listeners for buttons
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        budgetButton.setOnClickListener {
            startActivity(Intent(this, MonthlyBudget::class.java))
        }

        expensesButton.setOnClickListener {
            val intent = Intent(this, Reports::class.java)
            startActivity(intent)
        }

        educationButton.setOnClickListener {
            val intent = Intent(this, FinancialLit::class.java)
            startActivity(intent)
        }
    }
}