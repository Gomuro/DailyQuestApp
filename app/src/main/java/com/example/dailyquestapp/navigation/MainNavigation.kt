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
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val isUserLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val username by userViewModel.username.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    
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
                    selected = currentScreen == Screen.PROFILE || 
                              currentScreen == Screen.LOGIN || 
                              currentScreen == Screen.REGISTER,
                    onClick = { 
                        currentScreen = if (isUserLoggedIn) Screen.PROFILE else Screen.LOGIN 
                    }
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
                        onLoginClick = { currentScreen = Screen.LOGIN },
                        onLogoutClick = { 
                            userViewModel.logout(context) { success ->
                                if (success) {
                                    currentScreen = Screen.LOGIN
                                }
                            }
                        },
                        onRegisterClick = { currentScreen = Screen.REGISTER }
                    )
                }
                Screen.LOGIN -> {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            userViewModel.login(context, email, password) { success ->
                                if (success) {
                                    currentScreen = Screen.PROFILE
                                }
                            }
                        },
                        onCancelClick = { currentScreen = Screen.HOME },
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
                                    currentScreen = Screen.PROFILE
                                }
                            }
                        },
                        onCancelClick = { currentScreen = Screen.HOME },
                        onLoginClick = { currentScreen = Screen.LOGIN },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onErrorDismiss = { userViewModel.clearError() }
                    )
                }
            }
        }
    }
} 