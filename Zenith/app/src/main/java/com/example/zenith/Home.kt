package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // Import for RecyclerView layout manager
import androidx.recyclerview.widget.RecyclerView // Import for RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : AppCompatActivity() {

    // Declare UI elements
    private lateinit var textViewHomeTotalIncome: TextView
    private lateinit var textViewHomeTotalExpenses: TextView
    private lateinit var textViewHomeRemainingBalance: TextView

    private lateinit var savingsGoalsRecyclerView: RecyclerView // Declare RecyclerView
    private lateinit var savingsGoalsAdapter: SavingsGoalsAdapter // Declare Adapter
    private val savingsGoalsList = mutableListOf<SavingsGoal>() // List to hold savings goals

    // Firebase instances
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var savingsGoalsDatabase: DatabaseReference // New database reference for savings goals
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            currentUserId = currentUser.uid
            // Get a reference to the specific user's data in Firebase Realtime Database
            // Path: users -> {currentUserId}
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(currentUserId!!)
            // New reference for user-specific savings goals
            // Path: users -> {currentUserId} -> SavingsGoals
            savingsGoalsDatabase = mDatabase.child("SavingsGoals")
        } else {
            // Handle the case where the user is not logged in.
            Toast.makeText(this, "User not logged in. Please log in to view your dashboard.", Toast.LENGTH_LONG).show()
            finish() // Close this activity if no user is authenticated
            return
        }

        // Initialize UI elements from the layout XML
        val addExpenseButton: LinearLayout = findViewById(R.id.addExpenseButton)
        val savingsButton: LinearLayout = findViewById(R.id.savingsButton)
        val rewardsButton: LinearLayout = findViewById(R.id.rewardsButton)
        val viewReportsButton: LinearLayout = findViewById(R.id.viewReportsButton)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Initialize the TextViews for displaying financial data on the home screen
        textViewHomeTotalIncome = findViewById(R.id.textViewHomeTotalIncome)
        textViewHomeTotalExpenses = findViewById(R.id.textViewHomeTotalExpenses)
        textViewHomeRemainingBalance = findViewById(R.id.remainingAmount)

        // Initialize RecyclerView for savings goals
        savingsGoalsRecyclerView = findViewById(R.id.savingsGoalsRecyclerView)
        savingsGoalsRecyclerView.layoutManager = LinearLayoutManager(this)
        savingsGoalsAdapter = SavingsGoalsAdapter(savingsGoalsList)
        savingsGoalsRecyclerView.adapter = savingsGoalsAdapter


        // Set click listeners for buttons
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, QuickExpenses::class.java)
            startActivity(intent)
        }

        savingsButton.setOnClickListener {
            val intent = Intent(this, Savings::class.java)
            startActivity(intent)
        }

        rewardsButton.setOnClickListener {
            val intent = Intent(this, GameficationRewards::class.java)
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

        // Load and update all financial data (income, expenses, remaining balance) from Firebase
        loadFinancialDataForHome()
        // Load and display savings goals from Firebase
        loadSavingsGoals()
    }

    /**
     * Loads total income and total expenses from Firebase Realtime Database and calculates
     * and updates the remaining balance on the Home screen.
     * Uses `addValueEventListener` on the parent 'monthlyBudget' node for real-time updates.
     */
    private fun loadFinancialDataForHome() {
        mDatabase.child("monthlyBudget").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get income
                val income = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
                textViewHomeTotalIncome.text = "R${String.format("%.2f", income)}"

                // Get expenses
                val expenses = snapshot.child("totalExpenses").getValue(Double::class.java) ?: 0.0
                textViewHomeTotalExpenses.text = "R${String.format("%.2f", expenses)}"

                // Calculate remaining balance
                val remainingBalance = income - expenses
                textViewHomeRemainingBalance.text = "R${String.format("%.2f", remainingBalance)}"

                // You can add color logic here for remaining balance if it's negative
                if (remainingBalance < 0) {
                    textViewHomeRemainingBalance.setTextColor(resources.getColor(R.color.red, theme))
                } else {
                    textViewHomeRemainingBalance.setTextColor(resources.getColor(R.color.green, theme))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log and show an error if data loading fails
                Toast.makeText(this@Home, "Failed to load financial data: ${error.message}", Toast.LENGTH_LONG).show()
                textViewHomeTotalIncome.text = "Error"
                textViewHomeTotalExpenses.text = "Error"
                textViewHomeRemainingBalance.text = "Error"
            }
        })
    }

    /**
     * Loads savings goals for the current user from Firebase Realtime Database.
     */
    private fun loadSavingsGoals() {
        // We are now listening to the user's specific SavingsGoals node
        savingsGoalsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savingsGoalsList.clear() // Clear existing data to avoid duplicates
                if (snapshot.exists()) {
                    for (goalSnapshot in snapshot.children) {
                        val savingsGoal = goalSnapshot.getValue(SavingsGoal::class.java)
                        if (savingsGoal != null) {
                            savingsGoalsList.add(savingsGoal)
                        }
                    }
                } else {
                    // Optional: Show a message if no savings goals are found
                    // Toast.makeText(this@Home, "No savings goals found.", Toast.LENGTH_SHORT).show()
                }
                savingsGoalsAdapter.notifyDataSetChanged() // Notify adapter that data has changed
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Failed to load savings goals: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}