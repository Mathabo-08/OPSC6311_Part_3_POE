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

        // Set click listeners for buttons
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
    }
}