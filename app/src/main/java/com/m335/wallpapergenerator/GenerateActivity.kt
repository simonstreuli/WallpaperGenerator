package com.m335.wallpapergenerator

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.m335.wallpapergenerator.data.ApiKeyStore
import com.m335.wallpapergenerator.data.ImageStorage
import com.m335.wallpapergenerator.services.AiService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class GenerateActivity : AppCompatActivity() {

    private lateinit var aiService: AiService
    private var isOpenAiServiceBound = false

    private lateinit var loadingTitle: TextView
    private lateinit var loadingIcon: ImageView
    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)

        loadingTitle = findViewById(R.id.generate_loading_title)
        loadingIcon = findViewById(R.id.loading_icon)

        // Start rotation animation (if needed manually)
        loadingIcon.setImageResource(R.drawable.loading_animation_icon)

        // Bind AI Service
        Intent(this, AiService::class.java).also { intent ->
            bindService(intent, aiServiceConnection, BIND_AUTO_CREATE)
        }

        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        val titles = listOf(
            getString(R.string.generate_title1),
            getString(R.string.generate_title2),
            getString(R.string.generate_title3),
            getString(R.string.generate_title4),
            getString(R.string.generate_title5),
            getString(R.string.generate_title6)
        )

        loadingJob = CoroutineScope(Dispatchers.Main).launch {
            var index = 0
            while (isActive) {
                loadingTitle.text = titles[index]
                index = (index + 1) % titles.size
                delay(1500)
            }
        }
    }

    private fun checkServiceAndStartGeneration() {
        val isWallpaper = intent.getBooleanExtra("isWallpaper", true)
        val description = intent.getStringExtra("description") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val apiKey = ApiKeyStore.getApiKey(applicationContext).first()
            val imageResponse = aiService.generateImage(apiKey, description, isWallpaper)

            withContext(Dispatchers.Main) {
                if (imageResponse == null) {
                    showToast("Verbindung fehlgeschlagen.")
                    finishWithCancel()
                } else if (imageResponse.successful) {
                    ImageStorage.saveImage(applicationContext, imageResponse.url, description)
                    showToast("Bild erfolgreich generiert!")
                    finishWithSuccess()
                } else {
                    showToast(imageResponse.url)
                    finishWithCancel()
                }
            }
        }
    }

    private val aiServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AiService.LocalBinder
            aiService = binder.getService()
            isOpenAiServiceBound = true
            checkServiceAndStartGeneration()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isOpenAiServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingJob?.cancel()
        if (isOpenAiServiceBound) {
            unbindService(aiServiceConnection)
            isOpenAiServiceBound = false
        }
    }

    private fun finishWithSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun finishWithCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
