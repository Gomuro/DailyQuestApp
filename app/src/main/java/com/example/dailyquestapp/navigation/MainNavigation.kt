package com.example.dailyquestapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import com.example.dailyquestapp.presentation.goal.GoalDisplayScreen
import com.example.dailyquestapp.presentation.goal.GoalSetupScreen
import com.example.dailyquestapp.presentation.goal.GoalViewModel
import com.example.dailyquestapp.presentation.profile.LoginScreen
import com.example.dailyquestapp.presentation.profile.ProfileScreen
import com.example.dailyquestapp.presentation.profile.RegisterScreen
import com.example.dailyquestapp.presentation.profile.UserViewModel
import com.example.dailyquestapp.presentation.quest.QuestViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class Screen {
    HOME,
    PROFILE,
    LOGIN,
    REGISTER,
    GOAL_SETUP,
    GOAL_DISPLAY
}

@Composable
fun MainNavigation(
    questViewModel: QuestViewModel,
    userViewModel: UserViewModel,
    onDailyQuestScreen: @Composable () -> Unit,
    startScreen: Screen = Screen.LOGIN // Default to LOGIN for backward compatibility
) {
    val context = LocalContext.current
    val isUserLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val username by userViewModel.username.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    
    // Get GoalViewModel
    val goalViewModel: GoalViewModel = koinViewModel()
    val hasSetInitialGoal by goalViewModel.hasSetInitialGoal.collectAsStateWithLifecycle(initialValue = false)
    
    // Track if initial authentication check is complete
    var isInitializing by remember { mutableStateOf(true) }
    
    // Effect to track when authentication state is determined
    LaunchedEffect(isUserLoggedIn, isLoading) {
        if (!isLoading) {
            isInitializing = false
        }
    }
    
    // Show loading indicator during initial authentication check
    if (isInitializing && isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }
    
    // Use exactly what was provided as the startScreen parameter
    // This now respects what MainActivity decided
    var currentScreen by remember { mutableStateOf(startScreen) }
    
    // If user is not logged in, show login screen
    if (!isUserLoggedIn) {
        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onLoginClick = { email, password, rememberMe ->
                        userViewModel.login(context, email, password, rememberMe) { success ->
                            if (success) {
                                // Always go to home screen after login, the home screen can 
                                // redirect to goal setup if needed
                                currentScreen = Screen.HOME
                            }
                        }
                    },
                    onCancelClick = { /* No cancel action needed */ },
                    onRegisterClick = { currentScreen = Screen.REGISTER },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onErrorDismiss = { userViewModel.clearError() }
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    onRegisterClick = { username, email, password ->
                        userViewModel.register(context, username, email, password) { success ->
                            if (success) {
                                // New users always go to goal setup
                                currentScreen = Screen.GOAL_SETUP
                            }
                        }
                    },
                    onCancelClick = { currentScreen = Screen.LOGIN },
                    onLoginClick = { currentScreen = Screen.LOGIN },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onErrorDismiss = { userViewModel.clearError() }
                )
            }
            else -> {
                // If somehow we're on another screen but not logged in, go to login
                currentScreen = Screen.LOGIN
            }
        }
        return
    }
    
    // Handle goal setup screen outside of main navigation
    if (currentScreen == Screen.GOAL_SETUP) {
        GoalSetupScreen(
            viewModel = goalViewModel,
            onGoalSet = {
                currentScreen = Screen.HOME
            }
        )
        return
    }
    
    // User is logged in, show main app content
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == Screen.HOME,
                    onClick = { currentScreen = Screen.HOME }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Flag, contentDescription = "My Goal") },
                    label = { Text("My Goal") },
                    selected = currentScreen == Screen.GOAL_DISPLAY,
                    onClick = { currentScreen = Screen.GOAL_DISPLAY }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentScreen == Screen.PROFILE,
                    onClick = { currentScreen = Screen.PROFILE }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.HOME -> {
                    onDailyQuestScreen()
                }
                Screen.GOAL_DISPLAY -> {
                    GoalDisplayScreen(
                        viewModel = goalViewModel,
                        onEditGoal = { currentScreen = Screen.GOAL_SETUP }
                    )
                }
                Screen.PROFILE -> {
                    ProfileScreen(
                        isUserLoggedIn = isUserLoggedIn,
                        username = username,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onErrorDismiss = { userViewModel.clearError() },
                        onLoginClick = { /* Not needed, already logged in */ },
                        onLogoutClick = { 
                            userViewModel.logout(context) { success ->
                                if (success) {
                                    currentScreen = Screen.LOGIN
                                }
                            }
                        },
                        onRegisterClick = { /* Not needed, already logged in */ }
                    )
                }
                else -> {
                    // Should not happen when logged in, but just in case
                    currentScreen = Screen.HOME
                }
            }
        }
    }
} 