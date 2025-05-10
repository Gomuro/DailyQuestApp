package com.example.dailyquestapp.data.remote.dto

data class ProgressRequest(
    val points: Int,
    val streak: Int,
    val lastDay: Int
)

data class ProgressResponse(
    val totalPoints: Int,
    val currentStreak: Int,
    val lastClaimedDay: Int
)

data class SeedRequest(
    val seed: Long,
    val day: Int
)

data class SeedResponse(
    val currentSeed: Long,
    val seedDay: Int
)

data class RejectInfoRequest(
    val count: Int,
    val day: Int
)

data class RejectInfoResponse(
    val rejectCount: Int,
    val lastRejectDay: Int
)

data class ThemePreferenceRequest(
    val themeMode: Int
)

data class ThemePreferenceResponse(
    val themePreference: Int
)

data class BaseResponse(
    val message: String
)
