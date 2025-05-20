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

    /**
     * Speichert ein Bild mit Beschreibung.
     */
    fun saveImage(context: Context, imageUrl: String, description: String) {
        val current = getImages(context).toMutableMap()
        current[imageUrl] = description
        writeImages(context, current)
    }

    /**
     * Löscht ein Bild anhand der URL.
     */
    fun deleteImage(context: Context, imageUrl: String) {
        val current = getImages(context).toMutableMap()
        if (current.remove(imageUrl) != null) {
            writeImages(context, current)
            Log.i(TAG, "Bild gelöscht: $imageUrl")
        } else {
            Log.w(TAG, "Bild nicht gefunden: $imageUrl")
        }
    }

    /**
     * Gibt alle gespeicherten Bilder zurück.
     */
    fun getImages(context: Context): Map<String, String> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()

        return try {
            val jsonObject = JSONObject(file.readText())
            buildMap {
                jsonObject.keys().forEach { key ->
                    put(key, jsonObject.optString(key, ""))
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Datei konnte nicht gelesen werden: ${e.message}", e)
            emptyMap()
        } catch (e: JSONException) {
            Log.e(TAG, "Fehler beim Parsen von JSON: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Schreibt die Bild-Daten in die Datei.
     */
    private fun writeImages(context: Context, images: Map<String, String>) {
        val file = File(context.filesDir, FILE_NAME)
        try {
            val json = JSONObject(images)
            file.writeText(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Schreiben in JSON-Datei: ${e.message}", e)
        }
    }
}
