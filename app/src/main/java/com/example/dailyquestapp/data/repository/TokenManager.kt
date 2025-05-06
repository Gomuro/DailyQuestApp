package com.example.dailyquestapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

private val Context.tokenDataStore by preferencesDataStore(name = "auth_token")

class TokenManager constructor(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }
    
    fun getTokenFlow(): Flow<String> {
        return context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_KEY] ?: ""
        }
    }
    
    suspend fun saveToken(token: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    suspend fun clearToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
    
    // Synchronous version for immediate access (use carefully)
    fun getToken(): String {
        // This is still a blocking call but much safer than before
        // as it only gets the first emission rather than collecting indefinitely
        var token = ""
        runBlocking {
            token = getTokenFlow().first()
        }
        return token
    }
}
