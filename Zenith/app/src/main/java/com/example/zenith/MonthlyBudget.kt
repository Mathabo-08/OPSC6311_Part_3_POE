package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MonthlyBudget : AppCompatActivity() {

    // Firebase instances for Authentication and Firestore Database
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userId: String? = null // Stores the current authenticated user's ID

    // ListenerRegistration to manage the real-time Firestore listener for budgets.
    // This allows us to stop listening when the activity is not active to save resources.
    private var budgetListener: ListenerRegistration? = null

    // UI elements for the overall budget summary (from activity_monthly_budget.xml)
    private lateinit var totalAllocatedAmountTextView: TextView
    // REMOVED: totalSpentAmountTextView, totalRemainingAmountTextView, budgetProgressBar
    private lateinit var overallOverspentWarningTextView: TextView // Keeping this for the bottom warning

    // RecyclerView and its Adapter for displaying the list of budget categories
    private lateinit var budgetsRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter
    // The mutable list that holds the budget data, which the adapter uses
    private val budgetList = mutableListOf<BudgetItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_budget)

        // Initialize Firebase Authentication and Firestore instances
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // --- Initialize UI elements from activity_monthly_budget.xml ---
        // Overall Budget Summary TextViews and ProgressBar
        totalAllocatedAmountTextView = findViewById(R.id.totalAllocatedAmount)
        // REMOVED: findViewById(R.id.totalSpentAmount), findViewById(R.id.totalRemainingAmount), findViewById(R.id.budgetProgressBar)
        overallOverspentWarningTextView = findViewById(R.id.overallOverspentWarning)

        // RecyclerView setup
        budgetsRecyclerView = findViewById(R.id.budgetsRecyclerView)
        // LinearLayoutManager displays items in a vertical list
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize the adapter with our budgetList
        budgetAdapter = BudgetAdapter(budgetList)
        // Set the adapter to the RecyclerView
        budgetsRecyclerView.adapter = budgetAdapter

        // Set a click listener for individual budget items in the RecyclerView.
        // When a budget item card is clicked, it will open the update form
        // and pre-fill it with the clicked item's data for editing.
        budgetAdapter.setOnItemClickListener { budgetItem ->
            Toast.makeText(this, "Editing: ${budgetItem.category}", Toast.LENGTH_SHORT).show()
            showBudgetUpdateForm(budgetItem) // Pass the budgetItem to pre-fill the form
        }

        // --- Firebase Authentication State Listener ---
        // This listener checks the current user's authentication state.
        // It's crucial for performing Firestore operations that require a user ID.
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in (either previously or through anonymous sign-in)
                userId = user.uid
                Log.d("MonthlyBudget", "User ID: $userId")
                // Once authenticated, start listening for budget data
                startListeningForBudgets()
            } else {
                // No user is signed in. For this app, we sign in anonymously.
                // In a real application, you might redirect to a login/registration screen.
                Log.d("MonthlyBudget", "No user signed in. Signing in anonymously...")
                auth.signInAnonymously()
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d("MonthlyBudget", "Anonymous sign-in successful.")
                            userId = auth.currentUser?.uid // Get the new anonymous user's ID
                            startListeningForBudgets() // Start listening after successful anonymous sign-in
                        } else {
                            // Log and show an error if anonymous sign-in fails
                            Log.e("MonthlyBudget", "Anonymous sign-in failed: ${task.exception?.message}", task.exception)
                            Toast.makeText(this, "Authentication failed. Please restart the app.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        // --- Initialize and set click listeners for navigation buttons ---
        // These are your existing navigation buttons from the XML layout.
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val expensesButton: LinearLayout = findViewById(R.id.expensesButton)
        val goalsButton: LinearLayout = findViewById(R.id.goalsButton)
        val educationButton: LinearLayout = findViewById(R.id.educationButton)

        homeButton.setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        expensesButton.setOnClickListener { startActivity(Intent(this, Reports::class.java)) }
        goalsButton.setOnClickListener { startActivity(Intent(this, Goals::class.java)) }
        educationButton.setOnClickListener { startActivity(Intent(this, FinancialLit::class.java)) }

        // --- Initialize and set click listener for the main "Update Budget" button ---
        // This button opens the form to add a new budget or edit an existing one.
        val updateBudgetButton: Button = findViewById(R.id.updateBudgetButton)
        updateBudgetButton.setOnClickListener {
            // Call the form without passing a BudgetItem, indicating a new budget entry
            showBudgetUpdateForm()
        }
    }

    /**
     * Starts a real-time listener to Firestore for budget data specific to the current user.
     * This listener ensures the app's budget list automatically updates whenever
     * budget data changes in Firestore (e.g., adding a new category, updating an amount, adding an expense).
     */
    private fun startListeningForBudgets() {
        // Ensure userId is available before attempting to listen to Firestore
        if (userId == null) {
            Log.d("MonthlyBudget", "Cannot start listener: userId is null. Waiting for authentication.")
            return
        }

        // Remove any previously attached listener to prevent duplicate data processing or memory leaks
        budgetListener?.remove()

        // Get a reference to the 'budgets' sub-collection for the current user
        val budgetsCollectionRef = db.collection("users").document(userId!!).collection("budgets")

        // Attach a real-time listener to the collection.
        // orderBy("category", Query.Direction.ASCENDING) ensures budgets are displayed alphabetically.
        budgetListener = budgetsCollectionRef.orderBy("category", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle any errors during the listen operation
                    Log.w("MonthlyBudget", "Listen failed for budgets.", e)
                    Toast.makeText(this, "Error loading budgets.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // If snapshots are not null and contain documents (i.e., there's data)
                if (snapshots != null && !snapshots.isEmpty) {
                    val newBudgetList = mutableListOf<BudgetItem>()
                    var totalAllocated = 0.0
                    var totalSpent = 0.0 // Still calculate totalSpent to potentially use for the warning at the bottom

                    // Iterate through each document received in the snapshot
                    for (doc in snapshots.documents) {
                        // Convert each Firestore document into our BudgetItem data class.
                        // .copy(id = doc.id) is used to ensure the BudgetItem's 'id' field
                        // is populated with the Firestore document ID (which is the category name).
                        val budgetItem = doc.toObject(BudgetItem::class.java)?.copy(id = doc.id)
                        if (budgetItem != null) {
                            newBudgetList.add(budgetItem)
                            totalAllocated += budgetItem.allocatedAmount
                            totalSpent += budgetItem.spentAmount // Still sum spent for overall warning
                        }
                    }
                    // Update the RecyclerView with the new list of budgets
                    budgetAdapter.updateList(newBudgetList)
                    // Update the overall budget summary at the top of the screen
                    // Now we pass both allocated and spent, but the function will only use allocated for the header
                    updateOverallBudgetSummary(totalAllocated, totalSpent)
                } else {
                    // No budget data found for the user (collection is empty or doesn't exist)
                    Log.d("MonthlyBudget", "No budget data found for user: $userId")
                    budgetAdapter.updateList(emptyList()) // Clear the RecyclerView display
                    updateOverallBudgetSummary(0.0, 0.0) // Reset overall summary to zeros
                }
            }
    }

    /**
     * Updates the TextView for total allocated amount.
     * It also manages the overall overspent warning, even though the spent/remaining aren't in the header.
     */
    private fun updateOverallBudgetSummary(totalAllocated: Double, totalSpent: Double) {
        // Only display the total allocated amount in the header
        totalAllocatedAmountTextView.text = "R ${String.format("%.2f", totalAllocated)}"

        // REMOVED: totalSpentAmountTextView.text, totalRemainingAmountTextView.text, and budgetProgressBar logic from here.

        // Show or hide the overall overspent warning message (still useful at the bottom)
        // It's shown if total spent exceeds total allocated AND total allocated is greater than 0
        if (totalSpent > totalAllocated && totalAllocated > 0) {
            val overspentAmount = totalSpent - totalAllocated
            overallOverspentWarningTextView.text = "Overall Budget Overspent: R${String.format("%.2f", overspentAmount)}."
            overallOverspentWarningTextView.visibility = View.VISIBLE
        } else {
            overallOverspentWarningTextView.visibility = View.GONE
        }
    }

    /**
     * Displays an AlertDialog containing the budget update form (dialog_update_budget.xml).
     * This function can be used to either add a new budget category or edit an existing one.
     *
     * @param budgetItem Optional. If a BudgetItem object is provided, the form will be pre-filled
     * with its data, and the category title will be disabled for editing.
     * If null (default), it's treated as a new budget entry.
     */
    private fun showBudgetUpdateForm(budgetItem: BudgetItem? = null) {
        // Inflate the custom dialog layout (your provided dialog_update_budget.xml)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_budget, null)

        // Get references to the EditText fields and Buttons inside the dialog
        val titleInput = dialogView.findViewById<EditText>(R.id.editTitle)
        val amountInput = dialogView.findViewById<EditText>(R.id.editAmount)
        val updateBudButton: Button = dialogView.findViewById(R.id.updateBudButton)
        val backButton: Button = dialogView.findViewById(R.id.backButton)

        // Create the AlertDialog builder and set the custom view
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        // Create the dialog instance
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show() // Display the dialog to the user

        // Pre-fill the form if an existing budgetItem is passed (i.e., for editing)
        budgetItem?.let {
            titleInput.setText(it.category)
            amountInput.setText(String.format("%.2f", it.allocatedAmount))
            titleInput.isEnabled = false // Disable editing of category name for existing budgets
            alertDialog.setTitle("Edit Budget Category") // Set dialog title for editing mode
        } ?: run {
            // If no budgetItem is passed, it's a new entry
            alertDialog.setTitle("Add New Budget Category") // Set dialog title for adding new mode
        }

        // Set click listener for the "Update" button inside the dialog
        updateBudButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val amountString = amountInput.text.toString().trim()

            if (title.isNotEmpty() && amountString.isNotEmpty()) {
                try {
                    val amount = amountString.toDouble()
                    // Call the function to save/update budget data in Firestore
                    saveBudgetData(title, amount)
                    alertDialog.dismiss() // Dismiss the dialog after successful operation
                } catch (e: NumberFormatException) {
                    // Handle case where amount entered is not a valid number
                    Toast.makeText(this, "Please enter a valid number for amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Prompt user to fill both fields if any are empty
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for the "Back" button inside the dialog
        backButton.setOnClickListener {
            alertDialog.dismiss() // Simply dismiss the dialog without saving
        }
    }

    /**
     * Saves or updates a budget category's allocated amount in Firestore.
     * This function intelligently handles both creating new budget documents
     * and updating the 'allocatedAmount' for existing ones.
     *
     * @param category The name of the budget category (e.g., "Groceries", "Dining Out").
     * This will also be used as the Firestore document ID.
     * @param allocatedAmount The new allocated amount for this category.
     */
    private fun saveBudgetData(category: String, allocatedAmount: Double) {
        // Ensure the user is authenticated before proceeding with Firestore operations
        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please wait.", Toast.LENGTH_SHORT).show()
            Log.e("MonthlyBudget", "Cannot save budget: userId is null.")
            return
        }

        // Create a document reference for the specific budget category for the current user
        // The category name itself serves as the document ID.
        val budgetDocRef = db.collection("users").document(userId!!).collection("budgets").document(category)

        // First, check if a document for this category already exists in Firestore
        budgetDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // If the document exists, update only the 'allocatedAmount' field.
                // This ensures 'spentAmount' is preserved if it has been updated elsewhere.
                budgetDocRef.update("allocatedAmount", allocatedAmount)
                    .addOnSuccessListener {
                        Log.d("MonthlyBudget", "Budget for '$category' successfully updated!")
                        Toast.makeText(this, "Budget updated: $category = R${String.format("%.2f", allocatedAmount)}", Toast.LENGTH_LONG).show()
                        // The UI will automatically refresh due to the addSnapshotListener
                    }
                    .addOnFailureListener { e ->
                        Log.w("MonthlyBudget", "Error updating budget for '$category'", e)
                        Toast.makeText(this, "Error updating budget: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // If the document does NOT exist, create a new one.
                // For a new budget category, the 'spentAmount' is initialized to 0.0.
                val budgetData = BudgetItem(
                    id = category, // Use category name as the document ID for BudgetItem model
                    category = category,
                    allocatedAmount = allocatedAmount,
                    spentAmount = 0.0 // New budgets start with no amount spent
                )
                budgetDocRef.set(budgetData) // Use set() to create a new document
                    .addOnSuccessListener {
                        Log.d("MonthlyBudget", "New budget for '$category' successfully created!")
                        Toast.makeText(this, "New budget added: $category = R${String.format("%.2f", allocatedAmount)}", Toast.LENGTH_LONG).show()
                        // The UI will automatically refresh due to the addSnapshotListener
                    }
                    .addOnFailureListener { e ->
                        Log.w("MonthlyBudget", "Error creating new budget for '$category'", e)
                        Toast.makeText(this, "Error adding new budget: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            // Handle errors when trying to get the document (e.g., network issues, permissions)
            Log.e("MonthlyBudget", "Error checking existing document for '$category'", e)
            Toast.makeText(this, "Error checking budget existence: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This function provides an example of how to add an expense to a specific budget category.
     * In a real application, this would typically be called from your expense tracking module
     * or another activity when a user records a new expense.
     * It uses a Firestore transaction to safely update the 'spentAmount' field.
     *
     * @param category The budget category to which the expense belongs.
     * @param amount The amount of the expense to be added.
     */
    fun addExpenseToCategory(category: String, amount: Double) {
        // Ensure the user is authenticated
        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Cannot add expense.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the document reference
    }
}