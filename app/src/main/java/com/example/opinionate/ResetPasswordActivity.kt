package com.example.letmeknow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val firebaseAuth = FirebaseAuth.getInstance()

        val emailLayout = findViewById<TextInputLayout>(R.id.reset_password_email_layout)
        val email = findViewById<TextInputEditText>(R.id.reset_password_email)
        val submitButton = findViewById<Button>(R.id.reset_password_submit)

        emailListener(email, emailLayout)

        submitButton.setOnClickListener {
            firebaseAuth.sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener {task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1000)
                    }else if(task.exception.toString()=="com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted."){
                        emailLayout.helperText = "Invalid User Email"
                    }else if(task.exception.toString()=="com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted."){
                        emailLayout.helperText = "User Doesn't Exist!"
                    }
                }
        }
    }

    private fun emailListener(email: TextInputEditText, emailLayout: TextInputLayout) {
        email.setOnFocusChangeListener { _, focused ->
            if(!focused){
                emailLayout.helperText = validEmail(email)
            }
        }
    }

    private fun validEmail(email: TextInputEditText): String? {
        val emailText = email.text.toString()
        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            return "Invalid e-mail address"
        }
        return null
    }
}