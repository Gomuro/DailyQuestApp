package com.example.dailyquestapp.data.remote

import com.example.dailyquestapp.data.remote.dto.*
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponse

    // Progress endpoints
    @POST("progress")
    suspend fun saveProgress(@Body request: ProgressRequest): ProgressResponse

    @POST("progress/seed")
    suspend fun saveSeed(@Body request: SeedRequest): SeedResponse

    @POST("progress/task-history")
    suspend fun saveTaskHistory(@Body request: TaskHistoryRequest): BaseResponse

    @GET("progress/task-history")
    suspend fun getTaskHistory(): List<TaskHistoryDto>

    @DELETE("progress/task-history")
    suspend fun clearTaskHistory(): BaseResponse

    @POST("progress/reject-info")
    suspend fun updateRejectInfo(@Body request: RejectInfoRequest): RejectInfoResponse

    @POST("progress/theme")
    suspend fun saveThemePreference(@Body request: ThemePreferenceRequest): ThemePreferenceResponse

    @GET("progress/theme")
    suspend fun getThemePreference(): ThemePreferenceResponse
    
    // Goal endpoints
    @GET("goals/active")
    suspend fun getActiveGoal(): GoalDto
    
    @GET("goals")
    suspend fun getAllGoals(@Query("status") status: String? = null): List<GoalDto>
    
    @GET("goals/{id}")
    suspend fun getGoalById(@Path("id") goalId: String): GoalDto
    
    @POST("goals")
    suspend fun createGoal(@Body request: GoalRequest): GoalDto
    
    @PUT("goals/{id}")
    suspend fun updateGoal(@Path("id") goalId: String, @Body request: GoalRequest): GoalDto
    
    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") goalId: String): BaseResponse
    
    @PATCH("goals/{id}/progress")
    suspend fun updateGoalProgress(@Path("id") goalId: String, @Body request: GoalProgressRequest): GoalDto
}
