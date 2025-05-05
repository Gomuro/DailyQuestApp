package com.example.dailyquestapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.local.TaskProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.flow

// @HiltViewModel
class HistoryViewModel constructor(
    private val progressRepository: ProgressRepository
) : ViewModel(), KoinComponent {
    
    private val _historyItems = MutableStateFlow<List<TaskProgress>>(emptyList())
    val historyItems: StateFlow<List<TaskProgress>> = _historyItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            // Get history items directly since we don't have a flow version
            val history = progressRepository.getTaskHistory()
            _historyItems.value = history
            _isLoading.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            progressRepository.clearTaskHistory()
            _historyItems.value = emptyList()
        }
    }
} 