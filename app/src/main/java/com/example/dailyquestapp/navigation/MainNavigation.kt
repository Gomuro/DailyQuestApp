package com.example.dailyquestapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.dailyquestapp.presentation.profile.LoginScreen
import com.example.dailyquestapp.presentation.profile.ProfileScreen
import com.example.dailyquestapp.presentation.profile.RegisterScreen
import com.example.dailyquestapp.presentation.profile.UserViewModel
import com.example.dailyquestapp.presentation.quest.QuestViewModel

enum class Screen {
    HOME,
    PROFILE,
    LOGIN,
    REGISTER
}

@Composable
fun MainNavigation(
    questViewModel: QuestViewModel,
    userViewModel: UserViewModel,
    onDailyQuestScreen: @Composable () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) } // Start with login screen
    val isUserLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val username by userViewModel.username.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    
    // If user is not logged in, show login screen
    if (!isUserLoggedIn) {
        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onLoginClick = { email, password, rememberMe ->
                        userViewModel.login(context, email, password, rememberMe) { success ->
                            if (success) {
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
                                currentScreen = Screen.HOME
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