package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Goals : AppCompatActivity() {

    // Declare UI elements for Income
    private lateinit var buttonTotalIncome: Button
    private lateinit var incomeInputLayout: LinearLayout
    private lateinit var editTextTotalIncome: EditText
    private lateinit var buttonSaveIncome: Button
    private lateinit var textViewTotalIncomeDisplay: TextView // TextView to display current income

    // Declare UI elements for Expenses
    private lateinit var buttonTotalExpenses: Button
    private lateinit var expensesInputLayout: LinearLayout
    private lateinit var editTextTotalExpenses: EditText
    private lateinit var buttonSaveExpenses: Button
    private lateinit var textViewTotalExpensesDisplay: TextView // TextView to display current expenses

    // Firebase instances
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals) // Make sure this matches your layout file name, e.g., activity_goals.xml

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            currentUserId = currentUser.uid
            // Get a reference to the specific user's data in Firebase Realtime Database
            // Path: users -> {currentUserId}
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(currentUserId!!)
        } else {
            // Handle the case where the user is not logged in.
            // In a real app, you would typically redirect them to a login/registration activity here.
            Toast.makeText(this, "User not logged in. Please log in to manage your goals.", Toast.LENGTH_LONG).show()
            finish() // Close this activity if no user is authenticated
            return
        }

        // Initialize UI elements from the layout XML
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val budgetButton: LinearLayout = findViewById(R.id.budgetButton)
        val expensesButton: LinearLayout = findViewById(R.id.expensesButton)
        val educationButton: LinearLayout = findViewById(R.id.educationButton)

        // Initialize Income-related UI elements
        buttonTotalIncome = findViewById(R.id.buttonTotalIncome)
        incomeInputLayout = findViewById(R.id.incomeInputLayout)
        editTextTotalIncome = findViewById(R.id.editTextTotalIncome)
        buttonSaveIncome = findViewById(R.id.buttonSaveIncome)
        textViewTotalIncomeDisplay = findViewById(R.id.textViewTotalIncomeDisplay)

        // Initialize Expenses-related UI elements
        buttonTotalExpenses = findViewById(R.id.buttonTotalExpenses)
        expensesInputLayout = findViewById(R.id.expensesInputLayout)
        editTextTotalExpenses = findViewById(R.id.editTextTotalExpenses)
        buttonSaveExpenses = findViewById(R.id.buttonSaveExpenses)
        textViewTotalExpensesDisplay = findViewById(R.id.textViewTotalExpensesDisplay)

        // Set click listeners for bottom navigation buttons
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        budgetButton.setOnClickListener {
            // Navigate to MonthlyBudget activity, assuming it exists for more detailed budget management
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

        // Set OnClickListener for the 'Total Income' button
        buttonTotalIncome.setOnClickListener {
            toggleIncomeInputVisibility()
        }

        // Set OnClickListener for the 'Save Income' button
        buttonSaveIncome.setOnClickListener {
            saveTotalIncomeToFirebase()
        }

        // Set OnClickListener for the 'Total Expenses' button
        buttonTotalExpenses.setOnClickListener {
            toggleExpensesInputVisibility()
        }

        // Set OnClickListener for the 'Save Expenses' button
        buttonSaveExpenses.setOnClickListener {
            saveTotalExpensesToFirebase()
        }

        // Initially ensure both income and expenses input layouts are hidden
        // This is a good practice even if `android:visibility="gone"` is in XML.
        incomeInputLayout.visibility = View.GONE
        expensesInputLayout.visibility = View.GONE

        // Load existing income and expenses from Firebase when the activity is created
        // These will also set up real-time listeners for updates to the display TextViews.
        loadTotalIncomeFromFirebase()
        loadTotalExpensesFromFirebase()
    }

    /**
     * Toggles the visibility of the income input layout.
     * When it becomes visible, it hides the expenses input layout and attempts to load existing income.
     */
    private fun toggleIncomeInputVisibility() {
        if (incomeInputLayout.visibility == View.GONE) {
            incomeInputLayout.visibility = View.VISIBLE
            expensesInputLayout.visibility = View.GONE // Ensure expenses input is hidden
            // No need to call loadTotalIncomeFromFirebase() here, as addValueEventListener
            // in onCreate() already keeps the EditText updated if visible.
        } else {
            incomeInputLayout.visibility = View.GONE
        }
    }

    /**
     * Toggles the visibility of the expenses input layout.
     * When it becomes visible, it hides the income input layout and attempts to load existing expenses.
     */
    private fun toggleExpensesInputVisibility() {
        if (expensesInputLayout.visibility == View.GONE) {
            expensesInputLayout.visibility = View.VISIBLE
            incomeInputLayout.visibility = View.GONE // Ensure income input is hidden
            // No need to call loadTotalExpensesFromFirebase() here, as addValueEventListener
            // in onCreate() already keeps the EditText updated if visible.
        } else {
            expensesInputLayout.visibility = View.GONE
        }
    }

    /**
     * Saves the entered total income amount to Firebase Realtime Database.
     * Includes input validation.
     */
    private fun saveTotalIncomeToFirebase() {
        val incomeStr = editTextTotalIncome.text.toString().trim()

        if (incomeStr.isEmpty()) {
            editTextTotalIncome.error = "Income cannot be empty"
            editTextTotalIncome.requestFocus()
            return
        }

        try {
            val totalIncome = incomeStr.toDouble()

            // Save the income under the current user's monthlyBudget node in Firebase
            // Path: users -> {currentUserId} -> monthlyBudget -> totalIncome
            mDatabase.child("monthlyBudget").child("totalIncome").setValue(totalIncome)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Total Income saved successfully!", Toast.LENGTH_SHORT).show()
                        incomeInputLayout.visibility = View.GONE // Hide the input field after successful save
                        // loadTotalIncomeFromFirebase() is automatically triggered by ValueEventListener
                        // to update the display TextView.
                    } else {
                        Toast.makeText(this, "Failed to save income: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: NumberFormatException) {
            editTextTotalIncome.error = "Please enter a valid number"
            editTextTotalIncome.requestFocus()
        }
    }

    /**
     * Saves the entered total expenses amount to Firebase Realtime Database.
     * Includes input validation.
     */
    private fun saveTotalExpensesToFirebase() {
        val expensesStr = editTextTotalExpenses.text.toString().trim()

        if (expensesStr.isEmpty()) {
            editTextTotalExpenses.error = "Expenses cannot be empty"
            editTextTotalExpenses.requestFocus()
            return
        }

        try {
            val totalExpenses = expensesStr.toDouble()

            // Save the expenses under the current user's monthlyBudget node in Firebase
            // Path: users -> {currentUserId} -> monthlyBudget -> totalExpenses
            mDatabase.child("monthlyBudget").child("totalExpenses").setValue(totalExpenses)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Total Expenses saved successfully!", Toast.LENGTH_SHORT).show()
                        expensesInputLayout.visibility = View.GONE // Hide the input field after successful save
                        // loadTotalExpensesFromFirebase() is automatically triggered by ValueEventListener
                        // to update the display TextView.
                    } else {
                        Toast.makeText(this, "Failed to save expenses: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: NumberFormatException) {
            editTextTotalExpenses.error = "Please enter a valid number"
            editTextTotalExpenses.requestFocus()
        }
    }

    /**
     * Loads the total income from Firebase Realtime Database and updates the UI.
     * Uses `addValueEventListener` for real-time updates to the display TextView
     * and to pre-fill the EditText if the input layout is visible.
     */
    private fun loadTotalIncomeFromFirebase() {
        // Use addValueEventListener to listen for real-time changes
        mDatabase.child("monthlyBudget").child("totalIncome").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val income = snapshot.getValue(Double::class.java)
                    if (income != null) {
                        // Update the EditText only if its layout is currently visible
                        if (incomeInputLayout.visibility == View.VISIBLE) {
                            editTextTotalIncome.setText(String.format("%.2f", income))
                        }
                        // Always update the display TextView
                        textViewTotalIncomeDisplay.text = "Current Income: R${String.format("%.2f", income)}"
                    }
                } else {
                    // If no income is set in Firebase, clear EditText and reset display
                    editTextTotalIncome.setText("")
                    editTextTotalIncome.hint = "Enter your monthly income"
                    textViewTotalIncomeDisplay.text = "Current Income: R0.00"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log and show an error if data loading fails
                Toast.makeText(this@Goals, "Failed to load income: ${error.message}", Toast.LENGTH_LONG).show()
                textViewTotalIncomeDisplay.text = "Current Income: Error" // Indicate error on UI
            }
        })
    }

    /**
     * Loads the total expenses from Firebase Realtime Database and updates the UI.
     * Uses `addValueEventListener` for real-time updates to the display TextView
     * and to pre-fill the EditText if the input layout is visible.
     */
    private fun loadTotalExpensesFromFirebase() {
        // Use addValueEventListener to listen for real-time changes
        mDatabase.child("monthlyBudget").child("totalExpenses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val expenses = snapshot.getValue(Double::class.java)
                    if (expenses != null) {
                        // Update the EditText only if its layout is currently visible
                        if (expensesInputLayout.visibility == View.VISIBLE) {
                            editTextTotalExpenses.setText(String.format("%.2f", expenses))
                        }
                        // Always update the display TextView
                        textViewTotalExpensesDisplay.text = "Current Expenses: R${String.format("%.2f", expenses)}"
                    }
                } else {
                    // If no expenses are set in Firebase, clear EditText and reset display
                    editTextTotalExpenses.setText("")
                    editTextTotalExpenses.hint = "Enter your monthly expenses"
                    textViewTotalExpensesDisplay.text = "Current Expenses: R0.00"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log and show an error if data loading fails
                Toast.makeText(this@Goals, "Failed to load expenses: ${error.message}", Toast.LENGTH_LONG).show()
                textViewTotalExpensesDisplay.text = "Current Expenses: Error" // Indicate error on UI
            }
        })
    }
}
