package com.example.linkkoy3.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val USER_KEY = stringPreferencesKey("user")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
