package com.example.dailyquestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.presentation.goal.GoalDisplayScreen
import com.example.dailyquestapp.presentation.goal.GoalSetupScreen
import com.example.dailyquestapp.presentation.goal.GoalViewModel
import com.example.dailyquestapp.ui.theme.DailyQuestAppTheme
import com.example.dailyquestapp.ui.theme.LocalThemeMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class GoalActivity : ComponentActivity() {
    
    private val goalViewModel: GoalViewModel by viewModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dataStoreManager = DataStoreManager(applicationContext)
        
        // Check if we are editing an existing goal or creating a new one
        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        
        setContent {
            CompositionLocalProvider(
                LocalThemeMode provides dataStoreManager.themePreferenceFlow
            ) {
                DailyQuestAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var isEditing by remember { mutableStateOf(isEditMode) }
                        val currentGoal by goalViewModel.currentGoal.collectAsState()
                        
                        if (isEditing || currentGoal == null) {
                            // Show setup screen for new goal or when editing
                            GoalSetupScreen(
                                viewModel = goalViewModel,
                                onGoalSet = {
                                    // If we're in the activity, finish it to go back
                                    finish()
                                }
                            )
                        } else {
                            // Show display screen when viewing an existing goal
                            GoalDisplayScreen(
                                viewModel = goalViewModel,
                                onEditGoal = {
                                    // Switch to edit mode
                                    isEditing = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} 