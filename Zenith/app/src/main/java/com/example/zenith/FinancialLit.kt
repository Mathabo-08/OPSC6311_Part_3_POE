package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // Import CardView
import androidx.core.content.ContextCompat

class FinancialLit : AppCompatActivity() {

    // Existing navigation buttons
    private lateinit var homeButton: LinearLayout
    private lateinit var budgetButton: LinearLayout
    private lateinit var expensesButton: LinearLayout
    private lateinit var goalsButton: LinearLayout
    private lateinit var notifButton: ImageButton

    // New CardView variables
    private lateinit var cardBudget: CardView
    private lateinit var cardCreditScore: CardView
    private lateinit var cardDebitCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_lit)

        initializeViews()
        setupNavigationButtons()
        setupCardClickListeners() // New function to set up card listeners
        updateActiveButtonState()
    }

    private fun initializeViews() {
        homeButton = findViewById(R.id.homeButton)
        budgetButton = findViewById(R.id.budgetButton)
        expensesButton = findViewById(R.id.expensesButton)
        goalsButton = findViewById(R.id.goalsButton)
        notifButton = findViewById(R.id.notifButton)

        // Initialize CardViews
        cardBudget = findViewById(R.id.cardBudget)
        cardCreditScore = findViewById(R.id.cardCreditScore)
        cardDebitCard = findViewById(R.id.cardDebitCard)
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

    // New function to set up click listeners for the cards
    private fun setupCardClickListeners() {
        cardBudget.setOnClickListener {
            // Navigate to the Budget details screen
            // You'll need to create BudgetActivity.kt and activity_budget.xml
            navigateTo(BudgetActivity::class.java)
        }

        cardCreditScore.setOnClickListener {
            // Navigate to the Credit Score details screen
            // You'll need to create CreditScoreActivity.kt and activity_credit_score.xml
            navigateTo(CreditScoreActivity::class.java)
        }

        cardDebitCard.setOnClickListener {
            // Navigate to the Debit Card details screen
            // You'll need to create DebitCardActivity.kt and activity_debit_card.xml
            navigateTo(DebitCardActivity::class.java)
        }
    }

    private fun navigateTo(target: Class<*>) {
        // This existing navigateTo function is perfect for re-use
        if (this::class.java != target) {
            val intent = Intent(this, target)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            // No need to call updateActiveButtonState() here for card clicks,
            // as they open new content screens, not bottom navigation tabs.
        }
    }

    private fun updateActiveButtonState() {
        val inactiveColor = ContextCompat.getColor(this, R.color.white) // Ensure R.color.white exists or define a color
        val activeColor = ContextCompat.getColor(this, R.color.YELLOW) // Ensure R.color.YELLOW exists or define a color

        findViewById<TextView>(R.id.nav_home_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_budget_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_expenses_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_goals_text).setTextColor(inactiveColor)

        when (this::class.java.simpleName) {
            "Home" -> findViewById<TextView>(R.id.nav_home_text).setTextColor(activeColor)
            "MonthlyBudget" -> findViewById<TextView>(R.id.nav_budget_text).setTextColor(activeColor)
            "Reports" -> findViewById<TextView>(R.id.nav_expenses_text).setTextColor(activeColor)
            "Goals" -> findViewById<TextView>(R.id.nav_goals_text).setTextColor(activeColor)
            // No need to handle card activities here as they are not part of the bottom navigation
        }
    }

    override fun onResume() {
        super.onResume()
        updateActiveButtonState()
    }
}
