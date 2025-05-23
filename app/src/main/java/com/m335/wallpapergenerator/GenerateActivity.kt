package com.m335.wallpapergenerator

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.m335.wallpapergenerator.services.DatabaseService
import com.m335.wallpapergenerator.services.SettingsService
import com.m335.wallpapergenerator.services.AiService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GenerateActivity : AppCompatActivity() {
    private lateinit var settingsService: SettingsService
    private lateinit var aiService: AiService
    private lateinit var databaseService: DatabaseService
    private var isPreferenceServiceBound = false
    private var isOpenAiServiceBound = false
    private var isDatabaseServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)

        Intent(this, SettingsService::class.java).also { intent ->
            this.bindService(intent, preferenceConnection, BIND_AUTO_CREATE)
        }

        Intent(this, AiService::class.java).also { intent ->
            this.bindService(intent, openAiConnection, BIND_AUTO_CREATE)
        }

        Intent(this, DatabaseService::class.java).also { intent ->
            this.bindService(intent, databaseConnection, BIND_AUTO_CREATE)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkServicesConnected() {
        if (!isPreferenceServiceBound || !isOpenAiServiceBound || !isDatabaseServiceBound) return

        val isWallpaper = intent.getBooleanExtra("isWallpaper", true)
        val description = intent.getStringExtra("description") as String

        println("Wallpaper: $isWallpaper")
        println("Description: $description")

        GlobalScope.launch(Dispatchers.IO) {
            val imageResponse =
                aiService.generateImage(settingsService.getApiKey(), description, isWallpaper)
            launch(Dispatchers.Main) {
                if (imageResponse == null) {
                    Toast.makeText(this@GenerateActivity, "Check your connection.", Toast.LENGTH_LONG)
                        .show()
                    setResult(RESULT_CANCELED)
                    finish()
                } else if (imageResponse.successful) {
                    println("GENERATED IMAGE RESULT: ${imageResponse.url}")
                    databaseService.saveImage(imageResponse.url, description)
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@GenerateActivity, imageResponse.url, Toast.LENGTH_LONG)
                        .show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val title = getString(R.string.generate_title);

        findViewById<TextView>(R.id.generate_loading_title).text = title

        val spinner = findViewById<ImageView>(R.id.generate_loading_icon)
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        spinner.startAnimation(animation)
    }


    private val preferenceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SettingsService.LocalBinder
            settingsService = binder.getService()
            isPreferenceServiceBound = true
            checkServicesConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isPreferenceServiceBound = false
        }
    }

    private val databaseConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DatabaseService.LocalBinder
            databaseService = binder.getService()
            isDatabaseServiceBound = true
            checkServicesConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isDatabaseServiceBound = false
        }
    }

    private val openAiConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AiService.LocalBinder
            aiService = binder.getService()
            isOpenAiServiceBound = true
            checkServicesConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isOpenAiServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPreferenceServiceBound) {
            this.unbindService(preferenceConnection)
            isPreferenceServiceBound = false
        }
        if (isOpenAiServiceBound) {
            this.unbindService(openAiConnection)
            isOpenAiServiceBound = false
        }
        if (isDatabaseServiceBound) {
            this.unbindService(databaseConnection)
            isDatabaseServiceBound = false
        }
    }
}