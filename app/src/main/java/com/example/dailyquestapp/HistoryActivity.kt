package com.example.dailyquestapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.presentation.history.HistoryViewModel
import com.example.dailyquestapp.ui.theme.DailyQuestAppTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.dailyquestapp.ui.theme.LocalThemeMode

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dataStoreManager = DataStoreManager(applicationContext)
        
        setContent {
            CompositionLocalProvider(
                LocalThemeMode provides dataStoreManager.themePreferenceFlow
            ) {
                DailyQuestAppTheme {
                    val viewModel: HistoryViewModel by viewModel<HistoryViewModel>()
                    HistoryScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackPressed: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val historyItems by viewModel.historyItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()

    // Effect to control status bar appearance
    DisposableEffect(darkTheme) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Enable edge-to-edge
        // Set status bar to transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT 
        // Set status bar icon contrast
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        
        onDispose {
            // Optionally reset changes on dispose if needed, though often not necessary
            // Depends on whether other screens need different behavior
        }
    }

    // Add confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All History?") },
            text = { Text("Are you sure you want to permanently delete all quest history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quest History") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    // Only show delete button if there are history items
                    if (historyItems.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Reset History",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (historyItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No quest history yet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Complete or reject a quest to see it here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyItems) { item ->
                        HistoryItemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(taskProgress: TaskProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = if (taskProgress.status == TaskStatus.COMPLETED) 
                    Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = taskProgress.status.name,
                tint = if (taskProgress.status == TaskStatus.COMPLETED) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Quest details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = taskProgress.quest,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${taskProgress.date} ${taskProgress.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Points
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (taskProgress.status == TaskStatus.COMPLETED)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (taskProgress.status == TaskStatus.COMPLETED) 
                        "+${taskProgress.points}" else "${taskProgress.points}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (taskProgress.status == TaskStatus.COMPLETED)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
} 