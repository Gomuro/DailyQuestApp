package com.example.dailyquestapp.presentation.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dailyquestapp.domain.model.GoalCategory
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    viewModel: GoalViewModel,
    onGoalSet: () -> Unit
) {
    val currentGoal by viewModel.currentGoal.collectAsState()
    val hasSetInitialGoal by viewModel.hasSetInitialGoal.collectAsStateWithLifecycle(initialValue = false)
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var goalTitle by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.PERSONAL) }
    var targetDate by remember { mutableStateOf("") }
    
    var titleError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    // Load existing goal data if available
    LaunchedEffect(currentGoal) {
        currentGoal?.let { goal ->
            goalTitle = goal.title
            goalDescription = goal.description
            selectedCategory = try {
                GoalCategory.valueOf(goal.category)
            } catch (e: Exception) {
                GoalCategory.PERSONAL
            }
            goal.targetDate?.let {
                targetDate = viewModel.formatDate(it)
            }
        }
    }
    
    // Show error in SnackBar if present
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage ?: "An error occurred",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome section for new users
                if (!hasSetInitialGoal) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Welcome",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Welcome to Daily Quest!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "To help us personalize your daily quests, please set a main goal you'd like to achieve. We'll tailor your daily challenges to help you move toward this goal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Header
                Text(
                    text = if (currentGoal != null) "Edit Your Goal" else "Set Your Main Goal",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Define what you want to achieve with Daily Quest",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Goal Title
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { 
                        goalTitle = it
                        titleError = it.isBlank()
                    },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g., Become more physically active") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Goal"
                        )
                    },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title cannot be empty")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Goal Description
                OutlinedTextField(
                    value = goalDescription,
                    onValueChange = { goalDescription = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Describe your goal in more detail") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description"
                        )
                    },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Category Selector
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "Category"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            GoalCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Target Date
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date (Optional)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Target Date"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
                
                // Save Button
                Button(
                    onClick = {
                        if (goalTitle.isBlank()) {
                            titleError = true
                            return@Button
                        }
                        
                        viewModel.saveUserGoal(
                            title = goalTitle,
                            description = goalDescription,
                            category = selectedCategory,
                            targetDateString = if (targetDate.isBlank()) null else targetDate
                        )
                        
                        onGoalSet()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    Text(if (currentGoal != null) "Update Goal" else "Set My Goal")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your goal will help us tailor daily quests that align with your objectives.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
    }
} 