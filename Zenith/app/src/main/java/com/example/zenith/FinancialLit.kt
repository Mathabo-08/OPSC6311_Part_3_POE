package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class FinancialLit : AppCompatActivity() {

    private lateinit var homeButton: LinearLayout
    private lateinit var budgetButton: LinearLayout
    private lateinit var expensesButton: LinearLayout
    private lateinit var goalsButton: LinearLayout
    private lateinit var notifButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_lit)

        initializeViews()
        setupNavigationButtons()
        updateActiveButtonState()
    }

    private fun initializeViews() {
        homeButton = findViewById(R.id.homeButton)
        budgetButton = findViewById(R.id.budgetButton)
        expensesButton = findViewById(R.id.expensesButton)
        goalsButton = findViewById(R.id.goalsButton)
        notifButton = findViewById(R.id.notifButton)
    }

    private fun setupNavigationButtons() {
        homeButton.setOnClickListener {
            navigateTo(Home::class.java)
        }

        budgetButton.setOnClickListener {
            navigateTo(MonthlyBudget::class.java)
        }

        expensesButton.setOnClickListener {
            navigateTo(Reports::class.java)
        }

        goalsButton.setOnClickListener {
            navigateTo(Goals::class.java)
        }

    }


    private fun navigateTo(target: Class<*>) {
        if (this::class.java != target) {
            val intent = Intent(this, target)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            updateActiveButtonState()
        }
    }

    private fun updateActiveButtonState() {
        // Reset all buttons to inactive state
        val inactiveColor = ContextCompat.getColor(this, R.color.white)
        val activeColor = ContextCompat.getColor(this, R.color.YELLOW)

        homeButton.findViewById<TextView>(R.id.homeButton).setTextColor(inactiveColor)
        budgetButton.findViewById<TextView>(R.id.budgetButton).setTextColor(inactiveColor)
        expensesButton.findViewById<TextView>(R.id.expensesButton).setTextColor(inactiveColor)
        goalsButton.findViewById<TextView>(R.id.goalsButton).setTextColor(inactiveColor)

        // Set current button as active
        when (this::class.java.simpleName) {
            "Home" -> homeButton.findViewById<TextView>(R.id.homeButton).setTextColor(activeColor)
            "MonthlyBudget" -> budgetButton.findViewById<TextView>(R.id.budgetButton).setTextColor(activeColor)
            "Reports" -> expensesButton.findViewById<TextView>(R.id.expensesButton).setTextColor(activeColor)
            "Goals" -> goalsButton.findViewById<TextView>(R.id.goalsButton).setTextColor(activeColor)
        }
    }

    override fun onResume() {
        super.onResume()
        updateActiveButtonState()
    }
}