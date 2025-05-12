package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Initialize variables
        val emailField: EditText = findViewById(R.id.emailEditText)
        val passwordField: EditText = findViewById(R.id.passwordEditText)
        val termsCheckBox: CheckBox = findViewById(R.id.termsCheckBox)
        val registerButton: Button = findViewById(R.id.registerButton)
        val backButton: Button = findViewById(R.id.backButton)
        val loginTextView: TextView = findViewById(R.id.loginTextView)

        // Logic for the register button when it is clicked
        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // Validation
            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, "You must accept the Terms & Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validEmail(email)) {
                emailField.error = "Invalid email format"
                return@setOnClickListener
            }

            if (!validPassword(password)) {
                passwordField.error = "Password must be at least 8 characters with upper-case, lower-case, number, and one of ! # _"
                return@setOnClickListener
            }

            // Disable the register button to prevent multiple requests
            registerButton.isEnabled = false

            // Firebase registration
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Re-enable the register button after request completes
                    registerButton.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                        // Pass email and password to Login screen for autofill
                        val intent = Intent(this, Login::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("password", password)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Logic for the login field when it is clicked
        loginTextView.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        // Button to return to the welcome screen
        backButton.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
    }

    // Helper method to validate email format
    private fun validEmail(email: String): Boolean {
        return email.contains("@")
    }

    // Helper method to validate password strength
    private fun validPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!#_])[A-Za-z\\d!#_]{8,}$")
        return regex.matches(password)
    }
}
