package com.example.zenith

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class QuickExpenses : AppCompatActivity() {

    private lateinit var homeButton: LinearLayout
    private lateinit var budgetButton: LinearLayout
    private lateinit var goalsButton: LinearLayout
    private lateinit var educationButton: LinearLayout
    private lateinit var dropDown: ImageView
    private lateinit var categoryTextView: TextView
    private lateinit var amountEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var saveExpenseButton: Button
    private lateinit var attachPhotoButton: Button // Declare the attachPhotoButton

    // Firebase references
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference // Firebase Storage reference

    // Uri of the selected image from the gallery
    private var selectedImageUri: Uri? = null

    // ActivityResultLauncher for gallery image picking
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Get the URI of the selected image
                result.data?.data?.let { uri ->
                    // Validate the file type (PNG or JPG) before setting the URI
                    if (isImageUriValid(uri)) {
                        selectedImageUri = uri
                        Toast.makeText(this, "Image selected: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedImageUri = null // Clear previously selected URI if invalid
                        Toast.makeText(this, "Invalid image format. Please select a PNG or JPG file.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show()
            }
        }

    // Permission request launcher (simplified for gallery)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery() // If permission granted, open gallery
            } else {
                Toast.makeText(this, "Permission denied. Cannot attach photo from gallery.", Toast.LENGTH_LONG).show()
            }
        }

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

        // Initialize Firebase Database reference
        // "expenses" will be the root node where all your expense data is stored
        database = FirebaseDatabase.getInstance().getReference("expenses")
        // Initialize Firebase Storage reference
        // "receipt_photos" will be the folder within your Storage bucket
        storageRef = FirebaseStorage.getInstance().reference.child("receipt_photos")

        initializeViews()
        setupNavigationButtons()
        setupCategoryDropdown()
        setupDateSelection()
        setupSaveButton()
        setupAttachPhotoButton() // Setup the attach photo button
        updateActiveButtonState()
    }

    private fun initializeViews() {
        homeButton = findViewById(R.id.homeButton)
        budgetButton = findViewById(R.id.budgetButton)
        goalsButton = findViewById(R.id.goalsButton)
        educationButton = findViewById(R.id.educationButton)
        dropDown = findViewById(R.id.drop_down_cate)
        categoryTextView = findViewById(R.id.categoryTextView)
        amountEditText = findViewById(R.id.amountEditText)
        dateTextView = findViewById(R.id.dateTextView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveExpenseButton = findViewById(R.id.saveExpenseButton)
        attachPhotoButton = findViewById(R.id.attachPhotoButton) // Initialize the button
    }

    private fun setupAttachPhotoButton() {
        attachPhotoButton.setOnClickListener {
            checkGalleryPermissionAndOpen()
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        // Determine the correct permission based on Android version
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Check if permission is already granted
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery() // Permission already granted, proceed to open gallery
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Explain to the user why the permission is needed (optional, but good UX)
                Toast.makeText(this, "This app needs gallery access to attach receipt photos.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission) // Request permission
            }
            else -> {
                // Request the permission directly
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Set the type to only show image files
        // We will perform further filtering for PNG/JPG after selection
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    /**
     * Checks if the selected image URI corresponds to a PNG or JPEG/JPG format.
     */
    private fun isImageUriValid(uri: Uri?): Boolean {
        uri ?: return false // If URI is null, it's not valid
        try {
            contentResolver.getType(uri)?.let { mimeType ->
                return mimeType == "image/png" || mimeType == "image/jpeg"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error checking image type: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun setupCategoryDropdown() {
        dropDown.setOnClickListener {
            showCategoryPopupMenu(it)
        }

        val categoryInputLayout = findViewById<LinearLayout>(R.id.categoryInputLayout)
        categoryInputLayout.setOnClickListener {
            showCategoryPopupMenu(it)
        }
    }

    private fun showCategoryPopupMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)

        categories.forEachIndexed { index, category ->
            popup.menu.add(0, index, 0, category)
        }

        popup.setOnMenuItemClickListener { item: MenuItem ->
            if (categories[item.itemId] == "None") {
                categoryTextView.text = ""
                categoryTextView.hint = "Select Category"
                categoryTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            } else {
                categoryTextView.text = categories[item.itemId]
                categoryTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
            true
        }

        popup.show()
    }

    private fun setupDateSelection() {
        val dateInputLayout = findViewById<LinearLayout>(R.id.dateInputLayout)
        dateInputLayout.setOnClickListener {
            showDatePicker()
        }
        // Make the TextView itself clickable as well
        dateTextView.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateTextView.text = dateFormat.format(selectedDate.time)
                dateTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setupSaveButton() {
        saveExpenseButton.setOnClickListener {
            saveExpenseWithPhoto() // Call the function responsible for saving to Firebase
        }
    }

    /**
     * Gathers user input, validates it, and saves the expense to Firebase Realtime Database.
     * If a photo is selected, it uploads the photo to Firebase Storage first.
     */
    private fun saveExpenseWithPhoto() {
        val amountStr = amountEditText.text.toString().trim()
        val date = dateTextView.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val category = categoryTextView.text.toString().trim()

        // --- Input Validation ---
        if (amountStr.isEmpty()) {
            amountEditText.error = "Amount is required"
            amountEditText.requestFocus()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            amountEditText.error = "Invalid amount (e.g., use 100.50)"
            amountEditText.requestFocus()
            return
        }

        if (date.isEmpty() || date == "Select date") {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty() || category == "Select category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a unique key for the new expense entry regardless of photo
        val expenseId = database.push().key
        if (expenseId == null) {
            Toast.makeText(this, "Failed to generate a unique ID for the expense.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if an image was selected AND if it's valid
        if (selectedImageUri != null) {
            // Upload the image to Firebase Storage
            uploadImageToFirebaseStorage(expenseId) { imageUrl ->
                // This callback receives the download URL after upload
                saveExpenseToFirebase(expenseId, amount, date, category, description, imageUrl)
            }
        } else {
            // No image selected or selected image was invalid, save expense directly without a photo URL
            saveExpenseToFirebase(expenseId, amount, date, category, description, null)
        }
    }

    /**
     * Uploads the selected image to Firebase Storage.
     * Calls the onComplete callback with the download URL or null on failure.
     */
    private fun uploadImageToFirebaseStorage(expenseId: String, onComplete: (String?) -> Unit) {
        selectedImageUri?.let { uri ->
            val photoRef = storageRef.child("$expenseId.jpg") // Using .jpg as a common extension for photos
            photoRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL
                    photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
                        onComplete(downloadUri.toString()) // Pass the URL back
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to get photo URL: ${e.message}", Toast.LENGTH_LONG).show()
                        onComplete(null) // Indicate failure
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload photo: ${e.message}", Toast.LENGTH_LONG).show()
                    onComplete(null) // Indicate failure
                }
        } ?: run {
            onComplete(null) // No URI, so no photo to upload
        }
    }

    /**
     * Saves the expense data (and optional photo URL) to Firebase Realtime Database.
     */
    private fun saveExpenseToFirebase(
        expenseId: String,
        amount: Double,
        date: String,
        category: String,
        description: String,
        photoUrl: String?
    ) {
        // Create an Expense object with the gathered data and photo URL
        val expense = Expense(expenseId, amount, date, category, description, photoUrl)

        // Save the expense object to Firebase Realtime Database
        database.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                clearInputFields() // Clear input fields for the next entry
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Clears all input fields and resets hints after a successful save.
     */
    private fun clearInputFields() {
        amountEditText.text.clear()
        descriptionEditText.text.clear()
        dateTextView.text = "Select date"
        dateTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        categoryTextView.text = "Select category"
        categoryTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        selectedImageUri = null // Clear selected image URI
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