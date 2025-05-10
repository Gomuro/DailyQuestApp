package com.example.dailyquestapp.data.repository

import android.util.Log
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.UserGoalData
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.GoalDto
import com.example.dailyquestapp.data.remote.dto.GoalProgressRequest
import com.example.dailyquestapp.data.remote.dto.GoalRequest
import com.example.dailyquestapp.domain.model.GoalCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Repository for managing user goals with online/offline support
 */
class GoalRepository(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager,
    private val tokenManager: TokenManager
) {
    private val TAG = "GoalRepository"
    
    /**
     * Get the active goal, with server synchronization if possible
     */
    fun getActiveGoal(): Flow<UserGoalData?> {
        return dataStoreManager.userGoalFlow
            .map { localGoal ->
                // Return local goal immediately if it exists
                if (localGoal != null) return@map localGoal
                
                // Only attempt server fetch if we have a token
                if (tokenManager.getToken().isNotEmpty()) {
                    try {
                        val serverGoal = apiService.getActiveGoal()
                        updateLocalGoalFromServer(serverGoal)
                        serverGoal.toUserGoalData()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching active goal", e)
                        null
                    }
                } else {
                    null
                }
            }
            .distinctUntilChanged()
    }
    
    /**
     * Create a new goal, or update if it already exists
     */
    suspend fun saveGoal(goal: UserGoalData): Result<UserGoalData> {
        return try {
            // Update locally first
            dataStoreManager.saveUserGoal(
                title = goal.title,
                description = goal.description,
                category = goal.category,
                targetDate = goal.targetDate
            )
            
            // If we have a token, sync with server
            if (tokenManager.getToken().isNotEmpty()) {
                val goalRequest = GoalRequest(
                    title = goal.title,
                    description = goal.description,
                    category = goal.category,
                    difficulty = getDifficultyFromCategory(goal.category),
                    deadline = goal.targetDate?.let { formatDateForApi(it) }
                )
                
                val response = if (goal.remoteId != null) {
                    // Update existing goal
                    apiService.updateGoal(goal.remoteId, goalRequest)
                } else {
                    // Create new goal
                    apiService.createGoal(goalRequest)
                }
                
                // Update local storage with server response
                updateLocalGoalFromServer(response)
                Result.success(response.toUserGoalData())
            } else {
                // No token, return local goal
                Result.success(goal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving goal", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark a goal as completed
     */
    suspend fun completeGoal(goalId: String?): Result<UserGoalData> {
        return try {
            // Update locally first
            dataStoreManager.completeUserGoal()
            
            // If we have a token and remote ID, update on server
            if (tokenManager.getToken().isNotEmpty() && goalId != null) {
                val updateRequest = GoalRequest(
                    title = "", // These fields won't be used for status update
                    description = "",
                    category = "",
                    difficulty = 1,
                    deadline = null
                )
                
                val response = apiService.updateGoal(goalId, updateRequest.copy(
                    // Add the status field to the request
                    title = dataStoreManager.userGoalFlow.first()?.title ?: ""
                ))
                
                updateLocalGoalFromServer(response)
                Result.success(response.toUserGoalData())
            } else {
                // Return the local goal
                Result.success(dataStoreManager.userGoalFlow.first() ?: 
                    throw IllegalStateException("No goal found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error completing goal", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update goal progress
     */
    suspend fun updateGoalProgress(
        goalId: String, 
        progressIncrement: Int,
        questId: String? = null
    ): Result<UserGoalData> {
        if (tokenManager.getToken().isEmpty()) {
            return Result.failure(IllegalStateException("Not authenticated"))
        }
        
        return try {
            val request = GoalProgressRequest(
                progressIncrement = progressIncrement,
                questId = questId
            )
            
            val response = apiService.updateGoalProgress(goalId, request)
            updateLocalGoalFromServer(response)
            Result.success(response.toUserGoalData())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal progress", e)
            Result.failure(e)
        }
    }
    
    /**
     * Reset goal (delete current goal)
     */
    suspend fun resetGoal(goalId: String?): Result<Boolean> {
        return try {
            // Update locally first
            dataStoreManager.resetUserGoal()
            
            // If we have a token and remote ID, delete on server
            if (tokenManager.getToken().isNotEmpty() && goalId != null) {
                apiService.deleteGoal(goalId)
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting goal", e)
            Result.failure(e)
        }
    }
    
    // Helper methods
    
    private fun getDifficultyFromCategory(category: String): Int {
        return when (category) {
            GoalCategory.HEALTH.name -> 3
            GoalCategory.CAREER.name -> 4
            GoalCategory.EDUCATION.name -> 4
            GoalCategory.FINANCIAL.name -> 3
            else -> 2
        }
    }
    
    private fun formatDateForApi(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }
    
    private suspend fun updateLocalGoalFromServer(goalDto: GoalDto) {
        val localGoal = goalDto.toUserGoalData()
        dataStoreManager.saveUserGoal(
            title = localGoal.title,
            description = localGoal.description,
            category = localGoal.category,
            targetDate = localGoal.targetDate
        )
    }

    // Add this new function to handle manual refresh
    suspend fun refreshActiveGoal() {
        if (tokenManager.getToken().isEmpty()) return
        
        try {
            val serverGoal = apiService.getActiveGoal()
            updateLocalGoalFromServer(serverGoal)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing active goal", e)
        }
    }
} 