package com.example.dailyquestapp.data.remote.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val _id: String,
    val username: String,
    val email: String,
    val totalPoints: Int,
    val currentStreak: Int,
    val token: String
)

data class UserResponse(
    val _id: String,
    val username: String,
    val email: String,
    val totalPoints: Int,
    val currentStreak: Int,
    val lastClaimedDay: Int
)
