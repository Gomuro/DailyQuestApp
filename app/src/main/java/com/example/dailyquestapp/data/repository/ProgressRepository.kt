package com.example.dailyquestapp.data.repository

import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.*
import com.example.dailyquestapp.data.remote.mapper.toProgressData
import com.example.dailyquestapp.data.remote.mapper.toTaskProgressList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProgressRepository {
    // Progress data
    suspend fun saveProgress(points: Int, streak: Int, lastDay: Int): ProgressData
    fun getProgressFlow(): Flow<ProgressData>
    
    // Seed
    suspend fun saveSeed(seed: Long, day: Int): Pair<Long, Int>
    fun getSeedFlow(): Flow<Pair<Long, Int>>
    
    // Task history
    suspend fun saveTaskHistory(quest: String, points: Int, status: TaskStatus)
    
    // Enhanced task history with goal information
    suspend fun saveTaskHistory(
        quest: String, 
        points: Int, 
        status: TaskStatus, 
        goalId: String? = null,
        goalProgress: Int = 0
    )
    
    suspend fun getTaskHistory(): List<TaskProgress>
    suspend fun clearTaskHistory()
    
    // Reject info
    suspend fun saveRejectInfo(count: Int, day: Int): Pair<Int, Int>
    fun getRejectInfoFlow(): Flow<Pair<Int, Int>>
    
    // Theme preference
    suspend fun saveThemePreference(themeMode: Int): Int
    fun getThemePreferenceFlow(): Flow<Int>
}

class ProgressRepositoryImpl constructor(
    private val apiService: ApiService
) : ProgressRepository {
    // Progress data
    override suspend fun saveProgress(points: Int, streak: Int, lastDay: Int): ProgressData {
        val response = apiService.saveProgress(ProgressRequest(points, streak, lastDay))
        return response.toProgressData()
    }
    
    override fun getProgressFlow(): Flow<ProgressData> = flow {
        val response = apiService.getCurrentUser()
        emit(
            ProgressData(
                points = response.totalPoints,
                streak = response.currentStreak,
                lastDay = response.lastClaimedDay
            )
        )
    }
    
    // Seed
    override suspend fun saveSeed(seed: Long, day: Int): Pair<Long, Int> {
        val response = apiService.saveSeed(SeedRequest(seed, day))
        return Pair(response.currentSeed, response.seedDay)
    }
    
    override fun getSeedFlow(): Flow<Pair<Long, Int>> = flow {
        val user = apiService.getCurrentUser()
        // Get additional seed data if needed
        val seedInfo = apiService.saveSeed(SeedRequest(0, 0)) // Just to get current values
        emit(Pair(seedInfo.currentSeed, seedInfo.seedDay))
    }
    
    // Task history - basic version for backward compatibility
    override suspend fun saveTaskHistory(quest: String, points: Int, status: TaskStatus) {
        saveTaskHistory(quest, points, status, null, 0)
    }
    
    // Task history - enhanced version with goal support
    override suspend fun saveTaskHistory(
        quest: String, 
        points: Int, 
        status: TaskStatus, 
        goalId: String?,
        goalProgress: Int
    ) {
        // Create a TaskHistoryRequest with goal information
        val request = if (goalId != null && goalProgress > 0) {
            // Include goal information for completed tasks
            TaskHistoryRequest(
                quest = quest,
                points = points,
                status = status.name,
                goalId = goalId,
                goalProgress = goalProgress
            )
        } else {
            // Basic request without goal info
            TaskHistoryRequest(
                quest = quest,
                points = points,
                status = status.name
            )
        }
        
        apiService.saveTaskHistory(request)
    }
    
    override suspend fun getTaskHistory(): List<TaskProgress> {
        return apiService.getTaskHistory().toTaskProgressList()
    }
    
    override suspend fun clearTaskHistory() {
        apiService.clearTaskHistory()
    }
    
    // Reject info
    override suspend fun saveRejectInfo(count: Int, day: Int): Pair<Int, Int> {
        val response = apiService.updateRejectInfo(RejectInfoRequest(count, day))
        return Pair(response.rejectCount, response.lastRejectDay)
    }
    
    override fun getRejectInfoFlow(): Flow<Pair<Int, Int>> = flow {
        val response = apiService.updateRejectInfo(RejectInfoRequest(0, 0)) // Just to get current values
        emit(Pair(response.rejectCount, response.lastRejectDay))
    }
    
    // Theme preference
    override suspend fun saveThemePreference(themeMode: Int): Int {
        val response = apiService.saveThemePreference(ThemePreferenceRequest(themeMode))
        return response.themePreference
    }
    
    override fun getThemePreferenceFlow(): Flow<Int> = flow {
        val response = apiService.getThemePreference()
        emit(response.themePreference)
    }
}