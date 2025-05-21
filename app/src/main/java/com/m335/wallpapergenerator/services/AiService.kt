package com.m335.wallpapergenerator.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.m335.wallpapergenerator.services.models.ImageResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiService : Service() {
    private val binder = LocalBinder()
    private var errorString = ""

    inner class LocalBinder : Binder() {
        fun getService(): AiService = this@AiService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    suspend fun verifyApiKey(apiKey: String): Boolean {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/models")
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        println("DEBUG: Starting API Key verification...")
        println("DEBUG: Using API Key: ${apiKey.take(5)}...") // nur ersten 5 Zeichen zur Sicherheit
        Log.d("verifyApiKey", "Sending request to OpenAI...")

        return try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            Log.d("verifyApiKey", "HTTP response code: ${response.code}")
            Log.d("verifyApiKey", "Response message: ${response.message}")
            println("DEBUG: Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Log.d("verifyApiKey", "API key is valid.")
                true
            } else {
                errorString = "Invalid API Key"
                Log.e("verifyApiKey", "API Key invalid. Body: ${response.body?.string()}")
                false
            }
        } catch (e: IOException) {
            errorString = "Error verifying Key"
            Log.e("verifyApiKey", "Exception during request: ${e.message}", e)
            false
        }
    }


    suspend fun generateImage(apiKey: String, description: String, isWallpaper: Boolean): ImageResponseModel? {
        val client = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
        val payload = """
        {
            "model": "dall-e-3",
            "prompt": "$description",
            "n": 1,
            "size": "${ if(isWallpaper) "1024x1792" else "1792x1024"}"
        }
        """
        println("Message payload: $payload")
        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody())
            .build()

        return try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string() ?: return ImageResponseModel("Error: Empty response", description, false)
            val jsonObject = JSONObject(responseBody)
            val dataArray = jsonObject.getJSONArray("data")

            if (dataArray.length() > 0) {
                val firstItem = dataArray.getJSONObject(0)
                val url = firstItem.getString("url")
                ImageResponseModel(url, description, true)
            } else {
                ImageResponseModel("Error: No data in response", description, false)
            }
        } catch (e: IOException) {
            ImageResponseModel("Something went wrong: ${e.message}", description, false)
        }
    }

    fun getErrorString(): String {
        return errorString
    }
}