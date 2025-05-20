package com.m335.wallpapergenerator.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ImageStorageService : Service() {

    private val binder = LocalBinder()

    companion object {
        private const val IMAGES_JSON_FILE_NAME = "storage.json"
        private const val TAG = "DatabaseService"
    }

    inner class LocalBinder : Binder() {
        fun getService(): ImageStorageService = this@ImageStorageService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Saves an image URL with its associated description.
     */
    fun saveImage(imageUrl: String, description: String) {
        val currentData = getImages().toMutableMap()
        currentData[imageUrl] = description
        writeToJsonFile(currentData)
    }

    /**
     * Loads all saved image entries from file.
     */
    fun getImages(): Map<String, String> {
        return readFromJsonFile()
    }

    /**
     * Writes a map of image data to a JSON file.
     */
    private fun writeToJsonFile(data: Map<String, String>) {
        val file = File(filesDir, IMAGES_JSON_FILE_NAME)
        try {
            val json = JSONObject(data)
            file.writeText(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to JSON file: ${e.message}", e)
        }
    }

    /**
     * Reads image data from the JSON file.
     */
    private fun readFromJsonFile(): Map<String, String> {
        val file = File(filesDir, IMAGES_JSON_FILE_NAME)
        if (!file.exists()) return emptyMap()

        return try {
            val content = file.readText()
            val jsonObject = JSONObject(content)
            val map = mutableMapOf<String, String>()

            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.optString(key, "")
            }

            map
        } catch (e: IOException) {
            Log.e(TAG, "File read error: ${e.message}", e)
            emptyMap()
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
            emptyMap()
        }
    }
}
