package com.m335.wallpapergenerator.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ApiKeyStore {

    private val Context.dataStore by preferencesDataStore(name = "secure_prefs")
    private val API_KEY = stringPreferencesKey("API_KEY")

    suspend fun setApiKey(context: Context, apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    fun getApiKey(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[API_KEY] ?: ""
        }
    }

    fun hasApiKey(context: Context): Flow<Boolean> {
        return getApiKey(context).map { it.isNotEmpty() }
    }
}
