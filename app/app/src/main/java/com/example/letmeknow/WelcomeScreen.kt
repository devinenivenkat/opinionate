package com.example.letmeknow

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class WelcomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        // Animations
        val topAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.top_animation_welcome_screen)
        val bottomAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation_welcome_screen)

        // Hooks
        val logo: ImageView = findViewById(R.id.logo)
        val appName: TextView = findViewById(R.id.appName)
        val appSlogan: TextView = findViewById(R.id.appSlogan)

        // Setting the animation
        logo.animation = topAnim
        appName.animation = bottomAnim
        appSlogan.animation = bottomAnim

        // Starting a new activity after a delay
        val user = FirebaseAuth.getInstance().currentUser
        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null){
                val intent = Intent(this, UserHome::class.java)
                startActivity(intent)
            } else{
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}