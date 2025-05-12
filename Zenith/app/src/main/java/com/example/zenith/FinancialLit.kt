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
        val inactiveColor = ContextCompat.getColor(this, R.color.white)
        val activeColor = ContextCompat.getColor(this, R.color.YELLOW)

        findViewById<TextView>(R.id.nav_home_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_budget_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_expenses_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_goals_text).setTextColor(inactiveColor)

        when (this::class.java.simpleName) {
            "Home" -> findViewById<TextView>(R.id.nav_home_text).setTextColor(activeColor)
            "MonthlyBudget" -> findViewById<TextView>(R.id.nav_budget_text).setTextColor(activeColor)
            "Reports" -> findViewById<TextView>(R.id.nav_expenses_text).setTextColor(activeColor)
            "Goals" -> findViewById<TextView>(R.id.nav_goals_text).setTextColor(activeColor)
        }
    }

    override fun onResume() {
        super.onResume()
        updateActiveButtonState()
    }
}