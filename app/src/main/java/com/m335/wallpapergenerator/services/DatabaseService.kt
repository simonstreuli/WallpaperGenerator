package com.m335.wallpapergenerator.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.json.JSONObject
import java.io.File

class DatabaseService : Service() {
    private val binder = LocalBinder()
    companion object {
        private const val IMAGES_JSON_FILE_NAME = "storage.json"
    }

    inner class LocalBinder : Binder() {
        fun getService(): DatabaseService = this@DatabaseService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun saveImage(imageUrl: String, description: String) {
        val images = getImages().toMutableMap()
        images[imageUrl] = description
        writeToJSONFile(images)
    }

    fun getImages(): Map<String, String> {
        return readFromJSONFile()
    }

    private fun writeToJSONFile(data: Map<String, String>) {
        try {
            val jsonObject = JSONObject(data)
            val file = File(filesDir, IMAGES_JSON_FILE_NAME)
            file.writeText(jsonObject.toString())
            Log.d("DatabaseService", "writeToJSONFile saved ${data.size} entries.")
        } catch (e: Exception) {
            Log.e("DatabaseService", "writeToJSONFile failed: ${e.message}")
        }
    }

    private fun readFromJSONFile(): Map<String, String> {
        val file = File(filesDir, IMAGES_JSON_FILE_NAME)
        if (!file.exists()) return emptyMap()

        val jsonString = file.readText()
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, String>()

        jsonObject.keys().forEach {
            map[it] = jsonObject.getString(it)
        }

        return map
    }
    fun deleteImage(imageUrl: String) {
        val images = getImages().toMutableMap()
        if (images.remove(imageUrl) != null) {
            writeToJSONFile(images)
            Log.d("DatabaseService", "Deleted $imageUrl, saved new file.")
        } else {
            Log.d("DatabaseService", "Image $imageUrl not found.")
        }
    }

}