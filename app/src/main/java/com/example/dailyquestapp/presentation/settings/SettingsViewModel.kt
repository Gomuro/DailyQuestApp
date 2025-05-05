package com.example.dailyquestapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.local.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

// @HiltViewModel
class SettingsViewModel constructor(
    private val progressRepository: ProgressRepository
) : ViewModel(), KoinComponent {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM.value)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()
    
    init {
        loadThemePreference()
    }
    
    private fun loadThemePreference() {
        viewModelScope.launch {
            progressRepository.getThemePreferenceFlow().collect { themePreference ->
                _themeMode.value = themePreference
            }
        }
    }
    
    fun setThemeMode(themeMode: Int) {
        viewModelScope.launch {
            progressRepository.saveThemePreference(themeMode)
            _themeMode.value = themeMode
        }
    }
} 