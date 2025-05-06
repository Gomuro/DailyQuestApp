package com.example.dailyquestapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.util.Patterns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (username: String, email: String, password: String) -> Unit,
    onCancelClick: () -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isUsernameError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isEmailFormatError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }
    var passwordsMatch by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Function to validate email format
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()
    }
    
    // Show error in snackbar if present
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            onErrorDismiss()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        isUsernameError = it.isEmpty()
                    },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username"
                        )
                    },
                    supportingText = {
                        if (isUsernameError) {
                            Text("Username cannot be empty")
                        }
                    },
                    isError = isUsernameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                )
                
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        isEmailError = it.isEmpty()
                        isEmailFormatError = !isValidEmail(it) && it.isNotEmpty()
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    supportingText = {
                        when {
                            isEmailError -> Text("Email cannot be empty")
                            isEmailFormatError -> Text("Please enter a valid email address")
                        }
                    },
                    isError = isEmailError || isEmailFormatError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                )
                
                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        isPasswordError = it.isEmpty()
                        passwordsMatch = it == confirmPassword
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = {
                        if (isPasswordError) {
                            Text("Password cannot be empty")
                        }
                    },
                    isError = isPasswordError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                )
                
                // Confirm Password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        isConfirmPasswordError = it.isEmpty()
                        passwordsMatch = password == it
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password"
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = {
                        if (isConfirmPasswordError) {
                            Text("Please confirm your password")
                        } else if (!passwordsMatch) {
                            Text("Passwords do not match")
                        }
                    },
                    isError = isConfirmPasswordError || !passwordsMatch,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    enabled = !isLoading
                )
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onCancelClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            isUsernameError = username.isEmpty()
                            isEmailError = email.isEmpty()
                            isEmailFormatError = !isValidEmail(email) && email.isNotEmpty()
                            isPasswordError = password.isEmpty()
                            isConfirmPasswordError = confirmPassword.isEmpty()
                            passwordsMatch = password == confirmPassword
                            
                            val validInputs = !isUsernameError && !isEmailError && !isEmailFormatError && 
                                              !isPasswordError && !isConfirmPasswordError && passwordsMatch
                            
                            if (validInputs) {
                                onRegisterClick(username, email, password)
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        enabled = !isLoading
                    ) {
                        Text("Register")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onLoginClick,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Already have an account? Login",
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Show loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
} 