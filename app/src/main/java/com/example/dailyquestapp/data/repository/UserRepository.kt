package com.example.dailyquestapp.data.repository

import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.LoginRequest
import com.example.dailyquestapp.data.remote.dto.RegisterRequest
import com.example.dailyquestapp.data.remote.mapper.toProgressData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun registerUser(username: String, email: String, password: String): String {
        val response = apiService.registerUser(RegisterRequest(username, email, password))
        tokenManager.saveToken(response.token)
        return response.token
    }
    
    suspend fun loginUser(email: String, password: String): String {
        val response = apiService.loginUser(LoginRequest(email, password))
        tokenManager.saveToken(response.token)
        return response.token
    }
    
    suspend fun getCurrentUserProgress(): Flow<ProgressData> = flow {
        val response = apiService.getCurrentUser()
        emit(
            ProgressData(
                points = response.totalPoints,
                streak = response.currentStreak,
                lastDay = response.lastClaimedDay
            )
        )
    }
    
    fun isUserLoggedIn(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }
}
