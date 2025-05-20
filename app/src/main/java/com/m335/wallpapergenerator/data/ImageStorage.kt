package com.m335.wallpapergenerator.data

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

object ImageStorage {

    private const val FILE_NAME = "storage.json"
    private const val TAG = "ImageStorage"

    fun saveImage(context: Context, imageUrl: String, description: String) {
        val currentData = getImages(context).toMutableMap()
        currentData[imageUrl] = description
        writeToJsonFile(context, currentData)
    }

    fun getImages(context: Context): Map<String, String> {
        return readFromJsonFile(context)
    }

    private fun writeToJsonFile(context: Context, data: Map<String, String>) {
        val file = File(context.filesDir, FILE_NAME)
        try {
            val json = JSONObject(data)
            file.writeText(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to JSON file: ${e.message}", e)
        }
    }

    private fun readFromJsonFile(context: Context): Map<String, String> {
        val file = File(context.filesDir, FILE_NAME)
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
