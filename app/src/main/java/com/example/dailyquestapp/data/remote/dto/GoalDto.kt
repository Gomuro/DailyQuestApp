package com.example.dailyquestapp.data.remote.dto

import com.example.dailyquestapp.data.local.UserGoalData
import com.google.gson.annotations.SerializedName
import java.util.*

data class GoalDto(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("difficulty")
    val difficulty: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("progress")
    val progress: Int,
    
    @SerializedName("deadline")
    val deadline: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("completedAt")
    val completedAt: String? = null
) {
    // Convert from DTO to domain model
    fun toUserGoalData(): UserGoalData {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        return UserGoalData(
            title = title,
            description = description,
            category = category,
            targetDate = deadline?.let { try { dateFormat.parse(it) } catch (e: Exception) { null } },
            createdDate = try { dateFormat.parse(createdAt) ?: Date() } catch (e: Exception) { Date() },
            isCompleted = status == "COMPLETED",
            isActive = status == "ACTIVE",
            remoteId = id,
            progress = progress
        )
    }
}

// Request body for creating/updating a goal
data class GoalRequest(
    val title: String,
    val description: String,
    val category: String,
    val difficulty: Int,
    val deadline: String? = null
)

// Request body for updating goal progress
data class GoalProgressRequest(
    val progressIncrement: Int,
    val questId: String? = null
) 