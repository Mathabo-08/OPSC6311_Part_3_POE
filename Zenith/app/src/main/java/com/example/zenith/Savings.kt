package com.example.zenith

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Savings : AppCompatActivity() {

    // Declare EditText and Button variables
    private lateinit var editTitle: EditText
    private lateinit var editAmount: EditText
    private lateinit var editDuration: EditText
    private lateinit var setButton: Button
    private lateinit var backButton: Button

    // Declare Firebase Database reference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)

        // Initialize Firebase Database
        // Replace "your_project_id" with your actual Firebase project ID if needed,
        // though typically Firebase handles this automatically with google-services.json
        database = FirebaseDatabase.getInstance().getReference("SavingsGoals")

        // Initialize your views from the XML layout
        editTitle = findViewById(R.id.sav_editTitle)
        editAmount = findViewById(R.id.editAmount)
        editDuration = findViewById(R.id.editDuration)
        setButton = findViewById(R.id.updateBudButton) // Assuming "Set" button has this ID
        backButton = findViewById(R.id.backButton)

        // Set OnClickListener for the "Set" button
        setButton.setOnClickListener {
            saveSavingsGoal()
        }

        // Set OnClickListener for the "Back" button (optional, implement as needed)
        backButton.setOnClickListener {
            finish() // This will simply close the current activity
        }
    }

    private fun saveSavingsGoal() {
        val title = editTitle.text.toString().trim()
        val monthlyAmountStr = editAmount.text.toString().trim()
        val durationStr = editDuration.text.toString().trim()

        // Input validation
        if (title.isEmpty()) {
            editTitle.error = "Title is required"
            editTitle.requestFocus()
            return
        }
        if (monthlyAmountStr.isEmpty()) {
            editAmount.error = "Monthly Amount is required"
            editAmount.requestFocus()
            return
        }
        if (durationStr.isEmpty()) {
            editDuration.error = "Duration is required"
            editDuration.requestFocus()
            return
        }

        val monthlyAmount = monthlyAmountStr.toDoubleOrNull()
        val duration = durationStr.toIntOrNull()

        if (monthlyAmount == null || monthlyAmount <= 0) {
            editAmount.error = "Please enter a valid positive monthly amount"
            editAmount.requestFocus()
            return
        }
        if (duration == null || duration <= 0) {
            editDuration.error = "Please enter a valid positive duration in months"
            editDuration.requestFocus()
            return
        }

        // Create a unique ID for each savings goal
        val goalId = database.push().key

        if (goalId != null) {
            val savingsGoal = SavingsGoal(title, monthlyAmount, duration)

            database.child(goalId).setValue(savingsGoal)
                .addOnSuccessListener {
                    Toast.makeText(this, "Savings Goal saved successfully!", Toast.LENGTH_SHORT).show()
                    // Clear the input fields after successful save
                    editTitle.text.clear()
                    editAmount.text.clear()
                    editDuration.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save Savings Goal: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Could not generate a unique ID for the goal.", Toast.LENGTH_SHORT).show()
        }
    }
}
