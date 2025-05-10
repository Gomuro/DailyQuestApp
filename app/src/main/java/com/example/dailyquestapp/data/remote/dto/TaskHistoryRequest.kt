package com.example.dailyquestapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskHistoryRequest(
    @SerializedName("quest")
    val quest: String,
    
    @SerializedName("points")
    val points: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("goalId")
    val goalId: String? = null,
    
    @SerializedName("goalProgress")
    val goalProgress: Int = 0
) 