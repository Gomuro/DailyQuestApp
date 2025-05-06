package com.example.dailyquestapp.util

import kotlinx.coroutines.flow.Flow

/**
 * Interface for monitoring network connectivity
 */
interface ConnectivityObserver {
    
    fun observe(): Flow<Status>
    
    enum class Status {
        Available,
        Unavailable,
        Losing,
        Lost
    }
} 