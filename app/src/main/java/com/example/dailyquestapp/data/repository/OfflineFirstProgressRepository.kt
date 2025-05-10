package com.example.dailyquestapp.data.repository

import android.content.Context
import android.util.Log
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.*
import com.example.dailyquestapp.util.ConnectivityObserver
import com.example.dailyquestapp.util.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.FlowPreview

/**
 * Implementation of ProgressRepository that follows an offline-first approach:
 * 1. All data is saved locally first
 * 2. Then attempts to sync with the server
 * 3. Falls back to local data when server is unavailable
 * 4. Queues changes for sync when connectivity is restored
 */
@OptIn(FlowPreview::class)
class OfflineFirstProgressRepository(
    private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val apiService: ApiService,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ProgressRepository {

    private val TAG = "OfflineFirstProgressRepo"
    
    // Network connectivity observer
    private val connectivityObserver = NetworkConnectivityObserver(context)
    
    // Tracks if we're online or not based on network state
    private val _isOnline = MutableStateFlow(
        connectivityObserver.getCurrentConnectivityStatus() == ConnectivityObserver.Status.Available
    )
    
    private val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    // Queue of operations to sync when back online
    private val pendingOperations = mutableListOf<suspend () -> Unit>()

    // Track ongoing theme operations to prevent duplicate calls
    private val themeMutex = Mutex()
    private var pendingThemeOperation = false

    init {
        // Observe network connectivity changes
        externalScope.launch {
            connectivityObserver.observe().collect { status ->
                val online = status == ConnectivityObserver.Status.Available
                
                if (online && !_isOnline.value) {
                    // We just went back online
                    Log.d(TAG, "Network connection restored. Processing pending operations.")
                    _isOnline.value = true
                    processPendingOperations()
                } else if (!online && _isOnline.value) {
                    // We just went offline
                    Log.d(TAG, "Network connection lost.")
                    _isOnline.value = false
                }
            }
        }
    }

    // Progress data
    override suspend fun saveProgress(points: Int, streak: Int, lastDay: Int): ProgressData {
        // Always save locally first
        dataStoreManager.saveProgress(points, streak, lastDay)
        
        // Try to sync with server
        return try {
            val response = apiService.saveProgress(ProgressRequest(points, streak, lastDay))
            ProgressData(
                points = response.totalPoints,
                streak = response.currentStreak,
                lastDay = response.lastClaimedDay
            )
        } catch (e: Exception) {
            handleSyncError(e)
            // Queue for later sync
            queueOperation { saveProgress(points, streak, lastDay) }
            // Return local data
            ProgressData(points, streak, lastDay)
        }
    }
    
    override fun getProgressFlow(): Flow<ProgressData> {
        // Combine local data with remote attempts, but with debouncing and optimization
        return dataStoreManager.progressFlow
            .distinctUntilChanged() // Only emit when values actually change
            .onEach { localData ->
                // Try to sync with server in the background if we're online
                if (isOnline.value) {
                    externalScope.launch {
                        try {
                            val response = apiService.getCurrentUser()
                            val remoteData = ProgressData(
                                points = response.totalPoints,
                                streak = response.currentStreak,
                                lastDay = response.lastClaimedDay
                            )
                            
                            // Only update local if remote data is different and newer
                            if (remoteData != localData && 
                                (remoteData.points > localData.points || 
                                 remoteData.streak > localData.streak)) {
                                Log.d(TAG, "Updating local data with remote data: $remoteData")
                                dataStoreManager.saveProgress(
                                    remoteData.points,
                                    remoteData.streak,
                                    remoteData.lastDay
                                )
                            }
                        } catch (e: Exception) {
                            handleSyncError(e)
                        }
                    }
                }
            }
            .debounce(500) // Add debouncing to prevent rapid updates
    }
    
    // Seed
    override suspend fun saveSeed(seed: Long, day: Int): Pair<Long, Int> {
        // Save locally first
        dataStoreManager.saveSeed(seed, day)
        
        // Try to sync with server
        return try {
            val response = apiService.saveSeed(SeedRequest(seed, day))
            Pair(response.currentSeed, response.seedDay)
        } catch (e: Exception) {
            handleSyncError(e)
            queueOperation { saveSeed(seed, day) }
            Pair(seed, day)
        }
    }
    
    override fun getSeedFlow(): Flow<Pair<Long, Int>> {
        return dataStoreManager.seedFlow.onEach { localData ->
            if (isOnline.value) {
                externalScope.launch {
                    try {
                        val seedInfo = apiService.saveSeed(SeedRequest(0, 0))
                        val remoteData = Pair(seedInfo.currentSeed, seedInfo.seedDay)
                        
                        if (remoteData != localData) {
                            dataStoreManager.saveSeed(remoteData.first, remoteData.second)
                        }
                    } catch (e: Exception) {
                        handleSyncError(e)
                    }
                }
            }
        }
    }
    
    // Task history
    override suspend fun saveTaskHistory(quest: String, points: Int, status: TaskStatus) {
        // Call the enhanced version with null goalId and 0 goalProgress
        saveTaskHistory(quest, points, status, null, 0)
    }
    
    // Enhanced task history with goal information
    override suspend fun saveTaskHistory(
        quest: String, 
        points: Int, 
        status: TaskStatus, 
        goalId: String?,
        goalProgress: Int
    ) {
        // Save locally first
        dataStoreManager.saveTaskHistory(quest, points, status)
        Log.d(TAG, "[saveTaskHistory] Saving to server: quest=$quest, points=$points, status=$status, goalId=$goalId, goalProgress=$goalProgress")
        
        // Try to sync with server
        try {
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
            
            val response = apiService.saveTaskHistory(request)
            Log.d(TAG, "[saveTaskHistory] Server response: ${response.message}")
        } catch (e: Exception) {
            Log.e(TAG, "[saveTaskHistory] Error: ${e.message}")
            handleSyncError(e)
            queueOperation { saveTaskHistory(quest, points, status, goalId, goalProgress) }
        }
    }
    
    override suspend fun getTaskHistory(): List<TaskProgress> {
        return try {
            // Try to get from server first if online
            if (isOnline.value) {
                try {
                    Log.d(TAG, "[getTaskHistory] Fetching from server...")
                    val remoteHistory = apiService.getTaskHistory().map { dto ->
                        Log.d(TAG, "[getTaskHistory] Received DTO: ${'$'}dto")
                        // Format the timestamp to date and time strings
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        val parseDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                        val parsedDate = try { parseDateFormat.parse(dto.timestamp) } catch (e: Exception) { null }
                        val date = if (parsedDate != null) dateFormat.format(parsedDate) else "Unknown"
                        val time = if (parsedDate != null) timeFormat.format(parsedDate) else "Unknown"
                        TaskProgress(
                            quest = dto.quest,
                            points = dto.points,
                            status = TaskStatus.valueOf(dto.status),
                            date = date,
                            time = time
                        )
                    }
                    Log.d(TAG, "[getTaskHistory] Parsed remote history: ${'$'}remoteHistory")
                    // Update local cache with remote data
                    dataStoreManager.updateTaskHistoryCache(remoteHistory)
                    remoteHistory
                } catch (e: Exception) {
                    Log.e(TAG, "[getTaskHistory] Error fetching remote history: ${'$'}{e.message}")
                    handleSyncError(e)
                    // Fall back to local data
                    return dataStoreManager.getTaskHistory().first()
                }
            } else {
                // Use local data if offline
                Log.d(TAG, "[getTaskHistory] Offline, using local data")
                return dataStoreManager.getTaskHistory().first()
            }
        } catch (e: Exception) {
            Log.e(TAG, "[getTaskHistory] Error in getTaskHistory: ${'$'}{e.message}")
            handleSyncError(e)
            // If all else fails, return empty list to avoid indefinite loading
            return emptyList()
        }
    }
    
    override suspend fun clearTaskHistory() {
        // Clear locally first
        dataStoreManager.clearTaskHistory()
        
        // Try to sync with server
        try {
            apiService.clearTaskHistory()
        } catch (e: Exception) {
            handleSyncError(e)
            queueOperation { clearTaskHistory() }
        }
    }
    
    // Reject info
    override suspend fun saveRejectInfo(count: Int, day: Int): Pair<Int, Int> {
        // Save locally first
        dataStoreManager.saveRejectInfo(count, day)
        
        // Try to sync with server
        return try {
            val response = apiService.updateRejectInfo(RejectInfoRequest(count, day))
            Pair(response.rejectCount, response.lastRejectDay)
        } catch (e: Exception) {
            handleSyncError(e)
            queueOperation { saveRejectInfo(count, day) }
            Pair(count, day)
        }
    }
    
    override fun getRejectInfoFlow(): Flow<Pair<Int, Int>> {
        return dataStoreManager.rejectInfoFlow.onEach { localData ->
            if (isOnline.value) {
                externalScope.launch {
                    try {
                        val response = apiService.updateRejectInfo(RejectInfoRequest(0, 0))
                        val remoteData = Pair(response.rejectCount, response.lastRejectDay)
                        
                        if (remoteData != localData) {
                            dataStoreManager.saveRejectInfo(remoteData.first, remoteData.second)
                        }
                    } catch (e: Exception) {
                        handleSyncError(e)
                    }
                }
            }
        }
    }
    
    // Theme preference
    override suspend fun saveThemePreference(themeMode: Int): Int {
        // Save locally first
        dataStoreManager.saveThemePreference(themeMode)
        
        // Use mutex to ensure only one theme operation happens at a time
        return themeMutex.withLock {
            try {
                Log.d(TAG, "Saving theme preference to server: $themeMode")
                pendingThemeOperation = true
                val response = apiService.saveThemePreference(ThemePreferenceRequest(themeMode))
                Log.d(TAG, "Theme preference saved to server: ${response.themePreference}")
                response.themePreference
            } catch (e: Exception) {
                handleSyncError(e)
                queueOperation { saveThemePreference(themeMode) }
                themeMode
            } finally {
                pendingThemeOperation = false
            }
        }
    }
    
    override fun getThemePreferenceFlow(): Flow<Int> {
        return dataStoreManager.themePreferenceFlow
            .distinctUntilChanged()
            .onEach { localTheme ->
                // Only fetch from server if we're online and no theme operation is ongoing
                if (isOnline.value && !pendingThemeOperation) {
                    // Use mutex to ensure only one theme operation happens at a time
                    externalScope.launch {
                        themeMutex.withLock {
                            if (!pendingThemeOperation) { // Check again inside lock
                                try {
                                    Log.d(TAG, "Fetching theme preference from server")
                                    val response = apiService.getThemePreference()
                                    if (response.themePreference != localTheme) {
                                        Log.d(TAG, "Updating local theme with remote value: ${response.themePreference}")
                                        dataStoreManager.saveThemePreference(response.themePreference)
                                    }
                                } catch (e: Exception) {
                                    handleSyncError(e)
                                }
                            }
                        }
                    }
                }
            }
            .debounce(500)
    }
    
    // Helper methods
    private fun handleSyncError(e: Exception) {
        when (e) {
            is IOException -> {
                // Network error
                Log.w(TAG, "Network error: ${e.message}")
            }
            is HttpException -> {
                // HTTP error
                Log.w(TAG, "HTTP error: ${e.code()}")
                if (e.code() == 401 || e.code() == 403) {
                    // Auth error - needs special handling
                    Log.e(TAG, "Authentication error")
                }
            }
            else -> {
                // Other error
                Log.e(TAG, "Unknown error: ${e.message}")
            }
        }
    }
    
    private fun queueOperation(operation: suspend () -> Unit) {
        synchronized(pendingOperations) {
            pendingOperations.add(operation)
            Log.d(TAG, "Operation queued. Total pending: ${pendingOperations.size}")
        }
    }
    
    private suspend fun processPendingOperations() {
        // Get a copy of operations while holding the lock, then release it
        val operationsToProcess = synchronized(pendingOperations) {
            if (pendingOperations.isEmpty()) {
                return
            }
            
            Log.d(TAG, "Processing ${pendingOperations.size} pending operations")
            
            val operations = pendingOperations.toList()
            pendingOperations.clear()
            operations
        }
        
        // Process operations outside the synchronized block
        for (operation in operationsToProcess) {
            try {
                operation() // Suspension point now outside synchronized block
                Log.d(TAG, "Pending operation executed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute pending operation: ${e.message}")
                handleSyncError(e)
                queueOperation(operation) // Safe to call, as it has its own synchronization
            }
        }
    }
} 