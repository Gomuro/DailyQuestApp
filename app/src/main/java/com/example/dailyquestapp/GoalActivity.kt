package com.example.dailyquestapp

import android.content.Intent
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
        // This is now the single source of truth for edit mode
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
                        val currentGoal by goalViewModel.currentGoal.collectAsState()
                        
                        // Force one-time check when the screen is first displayed
                        val isNewGoal = currentGoal == null
                        
                        // Only show edit screen in exactly two cases:
                        // 1. We were explicitly told to edit (EDIT_MODE=true from intent)
                        // 2. There is no goal at all (first-time setup)
                        if (isEditMode || isNewGoal) {
                            GoalSetupScreen(
                                viewModel = goalViewModel,
                                onGoalSet = {
                                    // If we're in the activity, finish it to go back
                                    finish()
                                }
                            )
                        } else {
                            // In all other cases, show display screen
                            GoalDisplayScreen(
                                viewModel = goalViewModel,
                                onEditGoal = {
                                    // Start a completely new activity with edit mode
                                    // instead of changing state within this one
                                    val intent = Intent(this@GoalActivity, GoalActivity::class.java).apply {
                                        putExtra("EDIT_MODE", true)
                                    }
                                    this@GoalActivity.startActivity(intent)
                                    finish() // Close current instance to avoid stacking
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} 