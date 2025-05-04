package com.example.dailyquestapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.TaskProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    
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
            dataStoreManager.getTaskHistory().collect { historyItems ->
                _historyItems.value = historyItems
                _isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dataStoreManager.clearTaskHistory()
            // History will automatically update via the collect in loadHistory
        }
    }
} 