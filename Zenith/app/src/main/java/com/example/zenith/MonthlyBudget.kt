package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MonthlyBudget : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_budget)

        // Initialize navigation buttons
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val expensesButton: LinearLayout = findViewById(R.id.expensesButton)
        val goalsButton: LinearLayout = findViewById(R.id.goalsButton)
        val educationButton: LinearLayout = findViewById(R.id.educationButton)

        // Initialize update budget button
        val updateBudgetButton: Button = findViewById(R.id.updateBudgetButton)

        // Set click listeners for navigation
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        expensesButton.setOnClickListener {
            startActivity(Intent(this, Reports::class.java))
        }

        goalsButton.setOnClickListener {
            startActivity(Intent(this, Goals::class.java))
        }

        educationButton.setOnClickListener {
            startActivity(Intent(this, FinancialLit::class.java))
        }

        // Show form when update budget is clicked
        updateBudgetButton.setOnClickListener {
            showBudgetUpdateForm()
        }
    }

    private fun showBudgetUpdateForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_budget, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.editTitle)
        val amountInput = dialogView.findViewById<EditText>(R.id.editAmount)

        AlertDialog.Builder(this)
            .setTitle("Update Budget Category")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val title = titleInput.text.toString().trim()
                val amount = amountInput.text.toString().trim()

                if (title.isNotEmpty() && amount.isNotEmpty()) {
                    Toast.makeText(this, "Budget updated: $title = R$amount", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
