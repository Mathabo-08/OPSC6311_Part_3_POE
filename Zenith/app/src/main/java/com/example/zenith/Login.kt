package com.example.zenith

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Initialize variables
        val emailField: EditText = findViewById(R.id.emailEditText)
        val passwordField: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val backButton: Button = findViewById(R.id.backButton)
        val registerTextView: TextView = findViewById(R.id.registerTextView)

        // Autofill email and password if registration is successfull
        val prefillEmail = intent.getStringExtra("email")
        val prefillPassword = intent.getStringExtra("password")
        if (!prefillEmail.isNullOrEmpty()) {
            emailField.setText(prefillEmail)
        }
        if (!prefillPassword.isNullOrEmpty()) {
            passwordField.setText(prefillPassword)
        }

        // Logic for the login button when it is clicked
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
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

            // Disable the login button to prevent multiple requests
            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Re-enable the login button after request completes
                    loginButton.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Home::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // login field when it is clicked
        registerTextView.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
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
