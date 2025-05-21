package com.m335.wallpapergenerator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.m335.wallpapergenerator.services.DatabaseService
import com.m335.wallpapergenerator.services.SettingsService
import com.m335.wallpapergenerator.services.AiService

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        val settingsServiceIntent = Intent(this, SettingsService::class.java)
        startService(settingsServiceIntent)

        val databaseServiceIntent = Intent(this, DatabaseService::class.java)
        startService(databaseServiceIntent)

        val aiServiceIntent = Intent(this, AiService::class.java)
        startService(aiServiceIntent)
    }
}