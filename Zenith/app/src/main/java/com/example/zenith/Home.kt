package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize buttons
        val addExpenseButton: LinearLayout = findViewById(R.id.addExpenseButton)
        val spend_sumButton: LinearLayout = findViewById(R.id.spend_sumButton)
        val rewardsButton: LinearLayout = findViewById(R.id.rewardsButton)
        val viewReportsButton: LinearLayout = findViewById(R.id.viewReportsButton)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Initialize variable


        // Set click listeners for buttons
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, QuickExpenses::class.java)
            startActivity(intent)
        }

        spend_sumButton.setOnClickListener {
            val intent = Intent(this, SpendingSum::class.java)
            startActivity(intent)
        }

        rewardsButton.setOnClickListener{
            val intent = Intent(this,GameficationRewards::class.java)
            startActivity(intent)
        }

        viewReportsButton.setOnClickListener {
            val intent = Intent(this, Reports::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        // Must allow user to edit the Income TextView and update the Income number

        // To get the remaining amount, the income amount must minus the expenses amount
    }
}