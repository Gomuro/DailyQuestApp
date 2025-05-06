package com.example.dailyquestapp.presentation.profile

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.local.dataStore
import com.example.dailyquestapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class UserViewModel : ViewModel(), KoinComponent {
    
    private val TAG = "UserViewModel"
    
    // Inject UserRepository
    private val userRepository: UserRepository by inject()
    
    // Login state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    // Username
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Companion object for constants
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USERNAME = stringPreferencesKey("username")
        val USER_ID = stringPreferencesKey("user_id")
    }
    
    // Initialize the user state from DataStore
    fun initialize(context: Context) {
        viewModelScope.launch {
            try {
                // Check if token exists in TokenManager (via Repository)
                // This is now a suspend function call, properly run in a coroutine
                val isTokenValid = userRepository.isUserLoggedIn()
                
                if (isTokenValid) {
                    // If we have a valid token, get user info from DataStore for quick UI display
                    val userDataFlow = context.dataStore.data.map { preferences ->
                        val username = preferences[USERNAME] ?: ""
                        username
                    }
                    
                    val storedUsername = userDataFlow.first()
                    _isLoggedIn.value = true
                    _username.value = storedUsername
                    
                    // In background, refresh user data from server
                    try {
                        userRepository.getCurrentUserProgress().collect { progressData ->
                            // If server returns valid data, we're good - user is authenticated
                            // Could update UI with server-side data here if needed
                        }
                    } catch (e: Exception) {
                        // If server auth fails, log out
                        Log.e(TAG, "Token validation failed: ${e.message}")
                        logout(context) { /* ignore result */ }
                    }
                } else {
                    _isLoggedIn.value = false
                    _username.value = ""
                }
                
                Log.d(TAG, "User initialized: logged in=${_isLoggedIn.value}, username=${_username.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user: ${e.message}")
                _isLoggedIn.value = false
                _username.value = ""
            }
        }
    }
    
    // Login function
    fun login(context: Context, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Call server login API
                val token = userRepository.loginUser(email, password)
                
                // If server login is successful, store basic user info locally for UI
                if (token.isNotEmpty()) {
                    // Extract username from email for display (optional)
                    val displayName = email.substringBefore("@")
                    
                    context.dataStore.edit { preferences ->
                        preferences[IS_LOGGED_IN] = true
                        preferences[USERNAME] = displayName
                    }
                    
                    // Update UI state
                    _isLoggedIn.value = true
                    _username.value = displayName
                    onResult(true)
                    
                    Log.d(TAG, "User logged in with email: $email")
                } else {
                    _errorMessage.value = "Login failed. Please check your credentials."
                    onResult(false)
                    Log.d(TAG, "Login failed: invalid credentials")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login error: ${e.message}"
                Log.e(TAG, "Error logging in: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Logout function
    fun logout(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear local user data
                context.dataStore.edit { preferences ->
                    preferences[IS_LOGGED_IN] = false
                    preferences[USERNAME] = ""
                }
                
                // Clear token - effectively logs out from server too
                // This is now a suspend function call, which is fine inside a coroutine
                userRepository.logoutUser()
                
                // Update state
                _isLoggedIn.value = false
                _username.value = ""
                onResult(true)
                
                Log.d(TAG, "User logged out")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging out: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Register function
    fun register(context: Context, username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Call server register API with proper email
                val token = userRepository.registerUser(username, email, password)
                
                if (token.isNotEmpty()) {
                    // Store basic user info locally for UI
                    context.dataStore.edit { preferences ->
                        preferences[IS_LOGGED_IN] = true
                        preferences[USERNAME] = username
                    }
                    
                    // Update state
                    _isLoggedIn.value = true
                    _username.value = username
                    onResult(true)
                    
                    Log.d(TAG, "User registered: $username with email: $email")
                } else {
                    _errorMessage.value = "Registration failed"
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Registration error: ${e.message}"
                Log.e(TAG, "Error registering: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
} 