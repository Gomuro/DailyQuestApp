package com.example.dailyquestapp.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.UserGoalData
import com.example.dailyquestapp.domain.model.GoalCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class GoalViewModel : ViewModel(), KoinComponent {
    
    private val dataStoreManager: DataStoreManager by inject()
    
    private val _currentGoal = MutableStateFlow<UserGoalData?>(null)
    val currentGoal: StateFlow<UserGoalData?> = _currentGoal.asStateFlow()
    
    private val _hasSetInitialGoal = MutableStateFlow(false)
    val hasSetInitialGoal: StateFlow<Boolean> = _hasSetInitialGoal.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Load whether the user has set an initial goal
            dataStoreManager.hasSetInitialGoal.collect { hasSet ->
                _hasSetInitialGoal.value = hasSet
            }
        }
        
        viewModelScope.launch {
            // Load the current goal
            dataStoreManager.userGoalFlow.collect { goalData ->
                _currentGoal.value = goalData
            }
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
            try {
                // Parse target date if provided
                val targetDate = targetDateString?.let { parseDate(it) }
                
                // Save goal to DataStore
                dataStoreManager.saveUserGoal(
                    title = title,
                    description = description,
                    category = category.name,
                    targetDate = targetDate
                )
                
                _errorMessage.value = null
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
                dataStoreManager.completeUserGoal()
                _errorMessage.value = null
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
                dataStoreManager.resetUserGoal()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
} 