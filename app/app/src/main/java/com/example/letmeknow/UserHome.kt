package com.example.letmeknow

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.letmeknow.databinding.ActivityUserHomeBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class UserHome : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityUserHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseAuth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.appBarUserHome2.toolbar)
        val toolbar: Toolbar = binding.appBarUserHome2.toolbar
        toolbar.setOnMenuItemClickListener{
                item -> when(item.itemId){
            R.id.logoutButton -> {
                firebaseAuth.signOut()
                val inter = Intent(this, WelcomeScreen::class.java)
                startActivity(inter)
                finish()
            }

        }
            false
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_user_home2)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.CreateNewPoll, R.id.MyPolls, R.id.UserHome, R.id.AnsweredPolls
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.user_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_user_home2)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}