package com.example.dailyquestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.dailyquestapp.ui.theme.DailyQuestAppTheme
import com.example.dailyquestapp.util.NetworkDiagnosticTool
import kotlinx.coroutines.launch

class DiagnosticActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyQuestAppTheme {
                DiagnosticScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DiagnosticScreen(onBackClick: () -> Unit) {
        var diagnosticResult by remember { mutableStateOf<NetworkDiagnosticTool.DiagnosticResult?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Network Diagnostics") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                runDiagnostics { result ->
                                    diagnosticResult = result
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Run Diagnostics"
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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Server Connectivity Test",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "Running diagnostics...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (diagnosticResult == null) {
                    Button(
                        onClick = {
                            runDiagnostics { result ->
                                diagnosticResult = result
                            }
                        },
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("Start Diagnostics")
                    }
                    
                    Text(
                        text = "This will test the connection to your server and API endpoints.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    // Display results
                    val result = diagnosticResult!!
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.isFullyConnected())
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if (result.isFullyConnected())
                                    "✅ All Systems Operational"
                                else
                                    "❌ Connection Issues Detected",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (result.isFullyConnected())
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Detailed results
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = result.getDetailedReport(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            runDiagnostics { newResult ->
                                diagnosticResult = newResult
                            }
                        }
                    ) {
                        Text("Run Again")
                    }
                }
            }
        }
    }
    
    private fun runDiagnostics(onComplete: (NetworkDiagnosticTool.DiagnosticResult) -> Unit) {
        lifecycleScope.launch {
            try {
                val result = NetworkDiagnosticTool.runFullDiagnostics(this@DiagnosticActivity)
                onComplete(result)
            } catch (e: Exception) {
                // Handle any uncaught exceptions
                val errorResult = NetworkDiagnosticTool.DiagnosticResult(
                    hasInternetConnectivity = false,
                    isEmulatorLocalhostReachable = false,
                    isDirectLocalhostReachable = false,
                    apiTestResult = NetworkDiagnosticTool.ApiTestResult(
                        isSuccess = false,
                        statusCode = -1,
                        message = "Error: ${e.message}",
                        body = null
                    )
                )
                onComplete(errorResult)
            }
        }
    }
} 