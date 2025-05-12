package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class QuickExpenses : AppCompatActivity() {

    private lateinit var homeButton: LinearLayout
    private lateinit var budgetButton: LinearLayout
    private lateinit var expensesButton: LinearLayout
    private lateinit var goalsButton: LinearLayout
    private lateinit var educationButton: LinearLayout
    private lateinit var dropDown: ImageView
    private lateinit var categoryTextView: TextView

    // List of expense categories
    private val categories = listOf(
        "Transportaion",
        "Food",
        "Housing",
        "Clothing",
        "None"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_expenses)

        initializeViews()
        setupNavigationButtons()
        setupCategoryDropdown()
        updateActiveButtonState()
    }

    private fun initializeViews() {
        homeButton = findViewById(R.id.homeButton)
        budgetButton = findViewById(R.id.budgetButton)
        expensesButton = findViewById(R.id.expensesButton)
        goalsButton = findViewById(R.id.goalsButton)
        educationButton = findViewById(R.id.educationButton)
        dropDown = findViewById(R.id.drop_down_cate)
        categoryTextView = findViewById(R.id.categoryTextView)
    }

    private fun setupCategoryDropdown() {
        dropDown.setOnClickListener {
            showCategoryPopupMenu(it)
        }

        // Also make the whole category input layout clickable
        val categoryInputLayout = findViewById<LinearLayout>(R.id.categoryInputLayout)
        categoryInputLayout.setOnClickListener {
            showCategoryPopupMenu(it)
        }
    }

    private fun showCategoryPopupMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)

        // Add all categories to the menu
        categories.forEachIndexed { index, category ->
            popup.menu.add(0, index, 0, category)
        }

        popup.setOnMenuItemClickListener { item: MenuItem ->
            if (categories[item.itemId] == "None") {
                // Reset to default hint text
                categoryTextView.text = ""
                categoryTextView.hint = "Select Category"
                categoryTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            } else {
                // Update with selected category
                categoryTextView.text = categories[item.itemId]
                categoryTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
            true
        }

        popup.show()
    }

    private fun setupNavigationButtons() {
        homeButton.setOnClickListener {
            navigateTo(Home::class.java)
        }

        budgetButton.setOnClickListener {
            navigateTo(MonthlyBudget::class.java)
        }

        goalsButton.setOnClickListener {
            navigateTo(Goals::class.java)
        }

        educationButton.setOnClickListener {
            navigateTo(FinancialLit::class.java)
        }
    }

    private fun navigateTo(target: Class<*>) {
        if (this::class.java != target) {
            val intent = Intent(this, target)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun updateActiveButtonState() {
        // Reset all buttons to inactive state
        val inactiveColor = ContextCompat.getColor(this, R.color.white)
        val activeColor = ContextCompat.getColor(this, R.color.YELLOW)

        findViewById<TextView>(R.id.nav_home_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_budget_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_goals_text).setTextColor(inactiveColor)
        findViewById<TextView>(R.id.nav_education_text).setTextColor(inactiveColor)
    }

    override fun onResume() {
        super.onResume()
        updateActiveButtonState()
    }
}