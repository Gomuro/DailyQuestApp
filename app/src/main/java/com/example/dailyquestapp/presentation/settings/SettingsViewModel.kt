package com.example.dailyquestapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM.value)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()
    
    init {
        loadThemePreference()
    }
    
    private fun loadThemePreference() {
        viewModelScope.launch {
            dataStoreManager.themePreferenceFlow.collect { themePreference ->
                _themeMode.value = themePreference
            }
        }
    }
    
    fun setThemeMode(themeMode: Int) {
        viewModelScope.launch {
            dataStoreManager.saveThemePreference(themeMode)
            _themeMode.value = themeMode
        }
    }
} 