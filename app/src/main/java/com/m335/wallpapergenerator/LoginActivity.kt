package com.m335.wallpapergenerator

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.m335.wallpapergenerator.services.SettingsService
import com.m335.wallpapergenerator.services.AiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var settingsService: SettingsService
    private lateinit var aiService: AiService
    private var isDatabaseServiceBound = false
    private var isOpenAiServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.login_button_continue).setOnClickListener {
            onButtonContinue()
        }
        findViewById<TextView>(R.id.login_button_skip).setOnClickListener {
            onButtonSkip()
        }

        // Services binden
        Intent(this, SettingsService::class.java).also { intent ->
            bindService(intent, databaseConnection, BIND_AUTO_CREATE)
        }

        Intent(this, AiService::class.java).also { intent ->
            bindService(intent, openAiConnection, BIND_AUTO_CREATE)
        }
    }

    private val databaseConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SettingsService.LocalBinder
            settingsService = binder.getService()
            isDatabaseServiceBound = true
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
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isOpenAiServiceBound = false
        }
    }

    private fun onButtonContinue() {
        val editField = findViewById<EditText>(R.id.input_text_apikey)
        val apiKey = editField.text.toString().trim()

        if (apiKey.isEmpty()) {
            showToast("Please enter an API Key before continuing.")
            return
        }

        if (!isOpenAiServiceBound) {
            showToast("OpenAI Service not ready yet. Please try again.")
            return
        }

        showToast("Trying API Key...")

        GlobalScope.launch(Dispatchers.IO) {
            val isValidKey = aiService.verifyApiKey(apiKey)
            launch(Dispatchers.Main) {
                if (isValidKey) {
                    settingsService.setApiKey(apiKey)
                    setResult(RESULT_OK)
                    showToast("Success!")
                    finish()
                } else {
                    showToast(aiService.getErrorString())
                }
            }
        }
    }


    private fun onButtonSkip() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isDatabaseServiceBound) {
            unbindService(databaseConnection)
            isDatabaseServiceBound = false
        }
        if (isOpenAiServiceBound) {
            unbindService(openAiConnection)
            isOpenAiServiceBound = false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
