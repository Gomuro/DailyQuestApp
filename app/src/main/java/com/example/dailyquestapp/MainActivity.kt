package com.example.dailyquestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.dailyquestapp.ui.theme.DailyQuestAppTheme
import java.util.Calendar
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.ui.draw.scale
import kotlin.random.Random
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontWeight
import com.example.dailyquestapp.components.AppMenu
import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.presentation.quest.QuestViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Close
import com.example.dailyquestapp.data.local.TaskStatus
import androidx.compose.material.icons.filled.Settings
import com.example.dailyquestapp.components.MenuItem
import androidx.compose.material.icons.outlined.History
import androidx.compose.foundation.shape.CircleShape
import com.example.dailyquestapp.ui.theme.LocalThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import com.example.dailyquestapp.navigation.MainNavigation
import com.example.dailyquestapp.presentation.profile.UserViewModel
import com.example.dailyquestapp.navigation.Screen

class MainActivity : ComponentActivity() {
    lateinit var questTextView: TextView
    lateinit var rewardButton: Button
    var rewards: MutableList<String> = mutableListOf(
        "100 points",
        "500 points",
        "1000 points",
        "2000 points",
        "5000 points"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dataStoreManager = DataStoreManager(applicationContext)
        
        setContent {
            CompositionLocalProvider(
                LocalThemeMode provides dataStoreManager.themePreferenceFlow
            ) {
                DailyQuestAppTheme {
                    val questViewModel: QuestViewModel by viewModel<QuestViewModel>()
                    val userViewModel: UserViewModel by viewModel<UserViewModel>()
                    
                    // Create a state to track authentication status
                    var authState by remember { mutableStateOf(AuthState.CHECKING) }
                    
                    // Initialize user auth state and determine what screen to show
                    LaunchedEffect(Unit) {
                        userViewModel.initialize(applicationContext) { isLoggedIn ->
                            authState = if (isLoggedIn) AuthState.AUTHENTICATED else AuthState.UNAUTHENTICATED
                        }
                    }
                    
                    // Based on auth state, show appropriate screen
                    when (authState) {
                        AuthState.CHECKING -> {
                            // Show splash screen / loading indicator
                            SplashScreen()
                        }
                        AuthState.AUTHENTICATED -> {
                            // User is authenticated, show main app content
                            MainNavigation(
                                questViewModel = questViewModel,
                                userViewModel = userViewModel,
                                onDailyQuestScreen = {
                                    DailyQuestScreen(questViewModel)
                                },
                                startScreen = Screen.HOME // Explicitly start at home screen
                            )
                        }
                        AuthState.UNAUTHENTICATED -> {
                            // User is not authenticated, show login/register flow
                            MainNavigation(
                                questViewModel = questViewModel,
                                userViewModel = userViewModel,
                                onDailyQuestScreen = {
                                    DailyQuestScreen(questViewModel)
                                },
                                startScreen = Screen.LOGIN // Explicitly start at login screen
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun SplashScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // You can add an app logo or animation here
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Daily Quest App", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    
    // Auth states to manage app startup flow
    enum class AuthState {
        CHECKING,
        AUTHENTICATED,
        UNAUTHENTICATED
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DailyQuestScreen(viewModel: QuestViewModel) {
        val context = LocalContext.current
        val view = LocalView.current
        val currentSeed by viewModel.currentSeed.collectAsState()
        val currentQuest by remember { derivedStateOf { viewModel.getCurrentQuest() } }
        var showReward by remember { mutableStateOf(false) }
        val timeToNextQuest = remember { derivedStateOf { calculateTimeToNextQuest() } }
        val progressState by viewModel.progress.collectAsState()
        var totalPoints by remember { mutableStateOf(progressState.points) }
        var currentStreak by remember { mutableStateOf(progressState.streak) }
        var showStreakAnimation by remember { mutableStateOf(false) }
        val isGeneratingQuest by viewModel.isGeneratingQuest.collectAsState()
        val streakScale by animateFloatAsState(
            targetValue = if (showStreakAnimation) 1.5f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        var lastClaimedDay by remember { mutableStateOf(progressState.lastDay) }
        var isQuestCompleted by remember { mutableStateOf(false) }
        var rewardedQuest by remember { mutableStateOf<Pair<String, Int>?>(null) }
        val animatedProgress = remember { Animatable(0f) }
        val (rejectCount, lastRejectDay) = viewModel.rejectInfo.collectAsStateWithLifecycle().value
        var wasRejected by remember { mutableStateOf(false) }

        AppMenu(
            menuContent = { closeMenu ->
                MenuItem(
                    text = "History",
                    icon = Icons.Outlined.History,
                    onClick = {
                        context.startActivity(Intent(context, HistoryActivity::class.java))
                        closeMenu()
                    }
                )
                MenuItem(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = { 
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                        closeMenu() 
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Progress Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Daily Quest", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(16.dp))
                    Text("💰 $totalPoints", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(16.dp))
                    
                    // Progress indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = { timeToNextQuest.value.progress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Streak display - moved outside the main header row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        AnimatedVisibility(
                            visible = currentStreak > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFFFFA500)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text("🔥", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$currentStreak Day${if (currentStreak > 1) "s" else ""}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                // Spacer before Quest Card
                Spacer(Modifier.height(32.dp)) // Adjusted height

                // Main Quest Card
                AnimatedVisibility(visible = !showReward) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Added elevation
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isGeneratingQuest) {
                            // Show loading indicator while generating quest
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Generating your quest...",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Show quest content
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎯Your task is:", style = MaterialTheme.typography.titleMedium)
                                
                                Spacer(Modifier.height(24.dp))
                                
                                Text(
                                    text = currentQuest.first.trim(),
                                    style = MaterialTheme.typography.displayMedium,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏆 Reward:", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${currentQuest.second} points",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Spacer after Quest Card
                Spacer(Modifier.height(32.dp)) // Adjusted height

                // Reward Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedVisibility(visible = showReward) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (wasRejected) 
                                    Icons.Filled.Close 
                                else 
                                    Icons.Default.CheckCircle,
                                contentDescription = if (wasRejected) "Rejected" else "Completed",
                                tint = if (wasRejected) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (wasRejected) 
                                    "Quest rejected" 
                                else 
                                    "+${currentQuest.second} points",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (wasRejected) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                val today = calendar.get(Calendar.DAY_OF_YEAR)
                                
                                if (lastRejectDay != today) {
                                    viewModel.updateRejectInfo(0, today)
                                }
                                
                                if (rejectCount < 5) {
                                    viewModel.updateRejectInfo(rejectCount + 1, today)
                                    wasRejected = true
                                    showReward = true
                                    Toast.makeText(
                                        context,
                                        "Rejects left: ${5 - (rejectCount + 1)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = rejectCount < 5 && !showReward,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.size(64.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = "Reject Quest (${5 - rejectCount} left)",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (!showReward && currentQuest != null) {
                                    val calendar = Calendar.getInstance()
                                    val today = calendar.get(Calendar.DAY_OF_YEAR)
                                    val isNewDay = lastClaimedDay != today
                                    val newStreak = if (isNewDay && lastClaimedDay == today - 1) currentStreak + 1 else 1

                                    // Use AI-generated points directly
                                    val points = currentQuest.second
                                    val newTotalPoints = progressState.points + points

                                    // Save progress to ViewModel (which will persist and sync)
                                    viewModel.saveProgress(
                                        points = newTotalPoints,
                                        streak = newStreak,
                                        lastDay = today,
                                        quest = currentQuest.first,
                                        questPoints = points,
                                        status = TaskStatus.COMPLETED
                                    )
                                    showReward = true
                                    wasRejected = false
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .scale(if (showReward) 0.9f else 1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showReward) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                                                else MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Claim Reward",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Spacer below buttons
                    Spacer(Modifier.height(24.dp)) // Adjusted height

                    Text(
                        text = when {
                            showReward -> "New quest loading..."
                            else -> "Next adventure in: ${timeToNextQuest.value.formattedTime}"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Auto-reset reward message
        LaunchedEffect(showReward) {
            if (showReward) {
                delay(2000)
                showReward = false
                viewModel.loadSeed(System.currentTimeMillis())
                viewModel.generateAIQuest() // Generate a new AI quest
                wasRejected = false
            }
        }

        // Updated streak animation with improved visual effects
        AnimatedVisibility(
            visible = showStreakAnimation,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val baseRadius = size.minDimension / 4
                
                // Draw background glow
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    radius = baseRadius * 1.2f * (0.9f + animatedProgress.value * 0.2f),
                    center = center
                )
                
                // Main particles - more organized circular distribution
                val particleCount = 12
                val angleStep = (2f * PI.toFloat() / particleCount)
                
                repeat(particleCount) { index ->
                    // Create smooth wave-like motion
                    val waveOffset = sin(animatedProgress.value * PI.toFloat() * 2f + index * 0.5f).toFloat() * 15f
                    val distanceFromCenter = baseRadius * 0.8f + waveOffset
                    
                    // Calculate position with smooth rotation
                    val angle = angleStep * index + animatedProgress.value * PI.toFloat() * 2f
                    val x = center.x + cos(angle) * distanceFromCenter
                    val y = center.y + sin(angle) * distanceFromCenter
                    
                    // Dynamic size based on position in animation cycle
                    val particleSize = 20.dp.toPx() * (0.7f + kotlin.math.abs(sin(animatedProgress.value * PI.toFloat() * 3f + index * 0.4f)).toFloat() * 0.5f)
                    
                    // Color based on position with smoother transitions
                    val hue = (index / particleCount.toFloat()) * 60f + animatedProgress.value * 30f
                    val particleColor = androidx.compose.ui.graphics.Color.hsv(
                        hue = hue,
                        saturation = 0.9f,
                        value = 1.0f
                    ).copy(alpha = 0.8f)
                    
                    // Draw each particle
                    drawCircle(
                        color = particleColor,
                        radius = particleSize,
                        center = Offset(x, y),
                        blendMode = BlendMode.Screen
                    )
                }
                
                // Draw center highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f * (1f - animatedProgress.value)),
                    radius = 40.dp.toPx() * (1f - animatedProgress.value * 0.7f),
                    center = center,
                    blendMode = BlendMode.Screen
                )
            }
        }

        // Update the animation spec for smoother motion
        LaunchedEffect(currentStreak) {
            if (currentStreak > 0) {
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        // Add daily reset logic
        LaunchedEffect(Unit) {
            while (true) {
                val calendar = Calendar.getInstance()
                val now = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val delayTime = calendar.timeInMillis - now
                
                delay(delayTime)
                isQuestCompleted = false
                viewModel.loadSeed(System.currentTimeMillis())
            }
        }

        // Add this LaunchedEffect to handle streak animation duration
        LaunchedEffect(showStreakAnimation) {
            if (showStreakAnimation) {
                delay(2000)
                showStreakAnimation = false
            }
        }

        // Update local states when ViewModel state changes
        LaunchedEffect(progressState) {
            totalPoints = progressState.points
            currentStreak = progressState.streak
            lastClaimedDay = progressState.lastDay
        }
    }

    fun getReward(index: Int): String {
        return rewards[index % rewards.size]
    }

    private fun calculateTimeToNextQuest(): QuestTimer {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val total = calendar.timeInMillis - now
        val hours = total / 3600000
        val minutes = (total % 3600000) / 60000
        val progress = 1 - (total.toFloat() / 86400000)
        
        return QuestTimer(
            formattedTime = String.format("%02dh %02dm", hours, minutes),
            progress = progress
        )
    }

    data class QuestTimer(val formattedTime: String, val progress: Float)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DailyQuestAppTheme {
        Greeting("Android")
    }
}