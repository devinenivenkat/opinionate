package com.example.letmeknow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firebaseAuth = FirebaseAuth.getInstance()

        val button: Button = findViewById(R.id.loginredirect)
        button.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val email1Layout: TextInputLayout = findViewById(R.id.signupemaillayout)
        val email1: TextInputEditText = findViewById(R.id.signupemail)
        val password1Layout: TextInputLayout = findViewById(R.id.signuppasslayout)
        val password1: TextInputEditText = findViewById(R.id.signuppass)
        val submitButton: Button = findViewById(R.id.signupsubmit)

        emailListener(email1, email1Layout)
        passListener(password1, password1Layout)

        submitButton.setOnClickListener {
            val email = email1.text.toString()
            val password = password1.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if(password.length>=8){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if(it.isSuccessful){
                            val inter = Intent(this, Login::class.java)
                            startActivity(inter)
                            finish()
                        }else if(it.exception.toString() == "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account."){
                            Toast.makeText(this, "Account already exists!", Toast.LENGTH_SHORT).show()
                        }else if(it.exception.toString()=="com.google.firebase.FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host) has occurred."){
                            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Minimum 8 character password", Toast.LENGTH_SHORT).show()
                }

            }else {

                Toast.makeText(this, "enter full details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun emailListener(email1: TextInputEditText, email1Layout: TextInputLayout) {
        email1.setOnFocusChangeListener { _, focused ->
            if(!focused){
                email1Layout.helperText = validEmail(email1)
            }
        }
    }

    private fun validEmail(email1: TextInputEditText): String? {
        val emailText = email1.text.toString()
        return if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            "Invalid e-mail adress"
        }else{
            null
        }
    }

    private fun passListener(pass1: TextInputEditText, pass1Layout: TextInputLayout) {
        pass1.setOnFocusChangeListener { _, focused ->
            if(!focused){
                pass1Layout.helperText = validPass(pass1)
            }
        }
    }

    private fun validPass(pass1: TextInputEditText): String? {
        val pass1Text = pass1.text.toString()
        return if(pass1Text.length<8){
            "Minimum 8 characters"
        } else{
            null
        }
    }
}