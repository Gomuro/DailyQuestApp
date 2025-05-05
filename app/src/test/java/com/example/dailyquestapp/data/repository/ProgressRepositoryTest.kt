package com.example.dailyquestapp.data.repository

import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class ProgressRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var progressRepository: ProgressRepository

    @Before
    fun setup() {
        apiService = Mockito.mock(ApiService::class.java)
        progressRepository = ProgressRepository(apiService)
    }

    @Test
    fun saveProgress_callsApiAndReturnsData() = runBlocking {
        // Arrange
        val response = ProgressResponse(100, 5, 123)
        `when`(apiService.saveProgress(ProgressRequest(100, 5, 123))).thenReturn(response)

        // Act
        val result = progressRepository.saveProgress(100, 5, 123)

        // Assert
        assertEquals(100, result.points)
        assertEquals(5, result.streak)
        assertEquals(123, result.lastDay)
    }

    @Test
    fun saveSeed_callsApiAndReturnsData() = runBlocking {
        // Arrange
        val response = SeedResponse(123456L, 10)
        `when`(apiService.saveSeed(SeedRequest(123456L, 10))).thenReturn(response)

        // Act
        val result = progressRepository.saveSeed(123456L, 10)

        // Assert
        assertEquals(123456L, result.first)
        assertEquals(10, result.second)
    }

    @Test
    fun saveTaskHistory_callsApi() = runBlocking {
        // Arrange
        val response = BaseResponse("Task history saved successfully")
        `when`(apiService.saveTaskHistory(TaskHistoryRequest("Test quest", 100, "COMPLETED")))
            .thenReturn(response)

        // Act - should not throw
        progressRepository.saveTaskHistory("Test quest", 100, TaskStatus.COMPLETED)
    }
} 