package com.example.dailyquestapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.ThemeMode
import com.example.dailyquestapp.presentation.settings.SettingsViewModel
import com.example.dailyquestapp.ui.theme.DailyQuestAppTheme
import com.example.dailyquestapp.ui.theme.LocalThemeMode

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dataStoreManager = DataStoreManager(applicationContext)
        
        setContent {
            CompositionLocalProvider(
                LocalThemeMode provides dataStoreManager.themePreferenceFlow
            ) {
                DailyQuestAppTheme {
                    val viewModel: SettingsViewModel by viewModel<SettingsViewModel>()
                    SettingsScreen(
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit
) {
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Effect to control status bar appearance (same as HistoryActivity)
    DisposableEffect(darkTheme) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        onDispose { /* Optional cleanup */ }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            // Theme Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Display Theme",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    // Theme Options
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // System Default Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == ThemeMode.SYSTEM.value,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM.value) }
                            )
                            Text(
                                text = "System Default",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // Light Theme Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == ThemeMode.LIGHT.value,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT.value) }
                            )
                            Text(
                                text = "Light",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // Dark Theme Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == ThemeMode.DARK.value,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK.value) }
                            )
                            Text(
                                text = "Dark",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Note: You can add more settings sections here in the future
        }
    }
} 