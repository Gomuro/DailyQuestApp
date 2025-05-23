package com.example.dailyquestapp.di

import com.example.dailyquestapp.presentation.goal.GoalViewModel
import com.example.dailyquestapp.presentation.history.HistoryViewModel
import com.example.dailyquestapp.presentation.profile.UserViewModel
import com.example.dailyquestapp.presentation.quest.QuestViewModel
import com.example.dailyquestapp.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Register QuestViewModel with its dependencies
    // AIProcessor is injected directly in the ViewModel using by inject()
    viewModel { QuestViewModel(get(), get()) }
    
    // Register HistoryViewModel
    viewModel { HistoryViewModel(get()) }
    
    // Register SettingsViewModel
    viewModel { SettingsViewModel(get()) }
    
    // Register UserViewModel for authentication
    viewModel { UserViewModel() }
    
    // Register GoalViewModel
    viewModel { GoalViewModel() }
} 