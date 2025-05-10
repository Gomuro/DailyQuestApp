package com.example.dailyquestapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskHistoryDto(
    @SerializedName("quest")
    val quest: String,
    
    @SerializedName("points")
    val points: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("goalInfo")
    val goalInfo: GoalInfoDto? = null
)

data class GoalInfoDto(
    @SerializedName("goalId")
    val goalId: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("category")
    val category: String
) 