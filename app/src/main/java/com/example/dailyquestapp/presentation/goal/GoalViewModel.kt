package com.example.dailyquestapp.presentation.goal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.UserGoalData
import com.example.dailyquestapp.data.repository.GoalRepository
import com.example.dailyquestapp.domain.model.GoalCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.State
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class GoalViewModel : ViewModel(), KoinComponent {
    
    private val dataStoreManager: DataStoreManager by inject()
    private val goalRepository: GoalRepository by inject()
    
    private val _currentGoal = MutableStateFlow<UserGoalData?>(null)
    val currentGoal: StateFlow<UserGoalData?> = _currentGoal.asStateFlow()
    
    private val _hasSetInitialGoal = MutableStateFlow(false)
    val hasSetInitialGoal: StateFlow<Boolean> = _hasSetInitialGoal.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // New success state for goal setting
    private val _goalSetSuccess = MutableStateFlow(false)
    val goalSetSuccess: StateFlow<Boolean> = _goalSetSuccess.asStateFlow()
    
    private var hasFetchedInitialGoal = false
    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing
    
    init {
        viewModelScope.launch {
            // Only check server once on initialization
            if (!hasFetchedInitialGoal) {
                _isRefreshing.value = true
                goalRepository.refreshActiveGoal()
                hasFetchedInitialGoal = true
                _isRefreshing.value = false
            }
        }
        
        viewModelScope.launch {
            // Load whether the user has set an initial goal
            dataStoreManager.hasSetInitialGoal.collect { hasSet ->
                _hasSetInitialGoal.value = hasSet
            }
        }
        
        viewModelScope.launch {
            // Load the current goal with server synchronization
            goalRepository.getActiveGoal().collect { goalData ->
                _currentGoal.value = goalData
                // If we have a goal, ensure hasSetInitialGoal is true
                if (goalData != null) {
                    ensureHasSetInitialGoal()
                }
            }
        }
    }
    
    /**
     * Ensures the hasSetInitialGoal flag is set to true if we have a goal
     */
    private suspend fun ensureHasSetInitialGoal() {
        if (!_hasSetInitialGoal.value) {
            dataStoreManager.setInitialGoalFlag(true)
        }
    }
    
    fun saveUserGoal(
        title: String,
        description: String,
        category: GoalCategory,
        targetDateString: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _goalSetSuccess.value = false
            
            try {
                // Parse target date if provided
                val targetDate = targetDateString?.let { parseDate(it) }
                
                // Create goal object
                val goalData = UserGoalData(
                    title = title,
                    description = description,
                    category = category.name,
                    targetDate = targetDate,
                    remoteId = _currentGoal.value?.remoteId
                )
                
                // Save goal to repository (which handles local and remote storage)
                goalRepository.saveGoal(goalData).fold(
                    onSuccess = { 
                        // Ensure initial goal flag is set
                        ensureHasSetInitialGoal()
                        _goalSetSuccess.value = true
                    },
                    onFailure = { e ->
                        _errorMessage.value = "Failed to save goal: ${e.message}"
                    }
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeGoal() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Complete goal in repository (handles local and remote)
                goalRepository.completeGoal(_currentGoal.value?.remoteId).fold(
                    onSuccess = { /* Success - already updated in flow */ },
                    onFailure = { e ->
                        _errorMessage.value = "Failed to complete goal: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetGoal() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Reset goal in repository (handles local and remote)
                goalRepository.resetGoal(_currentGoal.value?.remoteId).fold(
                    onSuccess = { /* Success - already updated in flow */ },
                    onFailure = { e ->
                        _errorMessage.value = "Failed to reset goal: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Resets the goal success state after navigation
     */
    fun resetGoalSetSuccessState() {
        _goalSetSuccess.value = false
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun parseDate(dateString: String): Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
    
    fun refreshGoals() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                goalRepository.refreshActiveGoal()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
} 