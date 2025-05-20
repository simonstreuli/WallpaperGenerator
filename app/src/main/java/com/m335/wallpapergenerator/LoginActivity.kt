package com.m335.wallpapergenerator

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.m335.wallpapergenerator.data.ApiKeyStore
import com.m335.wallpapergenerator.services.AiService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var aiService: AiService
    private var isAiServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Intent(this, AiService::class.java).also { intent ->
            this.bindService(intent, openAiConnection, BIND_AUTO_CREATE)
        }
    }

    private val openAiConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AiService.LocalBinder
            aiService = binder.getService()
            isAiServiceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isAiServiceBound = false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onButtonContinue(view: View) {
        val editField = findViewById<EditText>(R.id.input_text_apikey)
        val apiKey = editField.text.toString().trim()

        if (apiKey.isEmpty()) {
            showToast("Bitte gib einen API Key ein.")
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val isValidKey = aiService.verifyApiKey(apiKey)
            launch(Dispatchers.Main) {
                if (isValidKey) {
                    // Speichere API Key mit DataStore
                    launch(Dispatchers.IO) {
                        ApiKeyStore.setApiKey(this@LoginActivity, apiKey)
                    }
                    setResult(RESULT_OK)
                    showToast("Erfolgreich verbunden!")
                    finish()
                } else {
                    showToast(aiService.getErrorString())
                }
            }
        }
    }

    fun onButtonSkip(view: View) {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isAiServiceBound) {
            this.unbindService(openAiConnection)
            isAiServiceBound = false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}