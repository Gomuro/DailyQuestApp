package com.example.dailyquestapp.data.repository

import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.LoginRequest
import com.example.dailyquestapp.data.remote.dto.RegisterRequest
import com.example.dailyquestapp.data.remote.mapper.toProgressData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import android.util.Log
import java.io.IOException

class UserRepository constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val TAG = "UserRepository"
    
    suspend fun registerUser(username: String, email: String, password: String): String {
        try {
            val response = apiService.registerUser(RegisterRequest(username, email, password))
            tokenManager.saveToken(response.token)
            return response.token
        } catch (e: Exception) {
            when (e) {
                is IOException -> Log.e(TAG, "Network error during registration: ${e.message}")
                is CancellationException -> throw e  // Let coroutine cancellation pass through
                else -> Log.e(TAG, "Error during registration: ${e.message}")
            }
            throw e
        }
    }
    
    suspend fun loginUser(email: String, password: String): String {
        try {
            val response = apiService.loginUser(LoginRequest(email, password))
            tokenManager.saveToken(response.token)
            return response.token
        } catch (e: Exception) {
            when (e) {
                is IOException -> Log.e(TAG, "Network error during login: ${e.message}")
                is CancellationException -> throw e  // Let coroutine cancellation pass through
                else -> Log.e(TAG, "Error during login: ${e.message}")
            }
            throw e
        }
    }
    
    suspend fun logoutUser() {
        tokenManager.clearToken()
        // No need to call API endpoint as we're using token-based authentication
        // Simply removing the token on the client side is enough to "log out"
    }
    
    suspend fun getCurrentUserProgress(): Flow<ProgressData> = flow {
        try {
            val response = apiService.getCurrentUser()
            emit(
                ProgressData(
                    points = response.totalPoints,
                    streak = response.currentStreak,
                    lastDay = response.lastClaimedDay
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IOException -> Log.e(TAG, "Network error getting user progress: ${e.message}")
                is CancellationException -> throw e  // Let coroutine cancellation pass through
                else -> Log.e(TAG, "Error getting user progress: ${e.message}")
            }
            throw e
        }
    }
    
    suspend fun isUserLoggedIn(): Boolean {
        return tokenManager.getTokenFlow().first().isNotEmpty()
    }
}
