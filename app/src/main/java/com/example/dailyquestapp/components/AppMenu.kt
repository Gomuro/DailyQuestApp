package com.example.dailyquestapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween

@Composable
fun AppMenu(
    menuContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {
    var isMenuVisible by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Clickable overlay
        if (isMenuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isMenuVisible = false }
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

        // Menu system
        Column(modifier = Modifier.fillMaxSize()) {
            // Menu button
            IconButton(
                onClick = { isMenuVisible = !isMenuVisible },
                modifier = Modifier.padding(top = 40.dp, start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Main content
            content()
        }

        // Menu drawer
        AnimatedVisibility(
            visible = isMenuVisible,
            enter = slideInHorizontally(animationSpec = tween(300)) { -it },
            exit = slideOutHorizontally(animationSpec = tween(300)) { -it },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .padding(top = 60.dp, start = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Menu", style = MaterialTheme.typography.titleMedium)
                        IconButton(
                            onClick = { isMenuVisible = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    menuContent()
                }
            }
        }
    }
}
