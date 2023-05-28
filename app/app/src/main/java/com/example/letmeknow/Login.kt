package com.example.letmeknow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {
    private var issuccess = false
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val firebaseAuth = FirebaseAuth.getInstance()

        val email1Layout: TextInputLayout = findViewById(R.id.loginemaillayout)
        val email1: TextInputEditText = findViewById(R.id.loginemail)
        val password1Layout: TextInputLayout = findViewById(R.id.loginpasslayout)
        val password1: TextInputEditText = findViewById(R.id.loginpass)
        val submitButton: Button = findViewById(R.id.loginsubmit)
        val forgotPassword: Button = findViewById(R.id.password_reset)

        emailListener(email1, email1Layout)
        passListener(password1, password1Layout)

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        val but: Button = findViewById(R.id.redirectHome)
        but.setOnClickListener {
            val intent = Intent(this, UserHome::class.java)
            startActivity(intent)
            finish()
        }
        val button: Button = findViewById(R.id.signupredirect)
        button.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        submitButton.setOnClickListener {
            val email = email1.text.toString()
            val password = password1.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                if(password.length>=8){
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            issuccess = true
                            but.performClick()
                        }else if(it.exception.toString()=="com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted."){
                            Toast.makeText(this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show()
                        }else if(it.exception.toString()=="com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password."){
                            Toast.makeText(this, "Wrong Password!", Toast.LENGTH_SHORT).show()
                        }else if(it.exception.toString()=="com.google.firebase.FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host) has occurred."){
                            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
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
        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            return "Invalid e-mail adress"
        }
        return null
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
        if(pass1Text.length<8){
            return "Minimum 8 character password"
        }
        return null
    }
}