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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.LinearProgressIndicator
import com.example.dailyquestapp.ui.theme.LocalThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import com.example.dailyquestapp.navigation.MainNavigation
import com.example.dailyquestapp.presentation.profile.UserViewModel
import com.example.dailyquestapp.navigation.Screen
import com.example.dailyquestapp.presentation.goal.GoalViewModel
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Search

// Static object to hold app-wide session flags
object AppSessionFlags {
    var hasCheckedGoalThisSession = false
}

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
                    val goalViewModel: GoalViewModel by viewModel<GoalViewModel>()
                    
                    var authState by remember { mutableStateOf(AuthState.CHECKING) }
                    val hasSetGoal by goalViewModel.hasSetInitialGoal.collectAsStateWithLifecycle(initialValue = false)
                    
                    LaunchedEffect(Unit) {
                        userViewModel.initialize(applicationContext) { isLoggedIn ->
                            authState = if (isLoggedIn) AuthState.AUTHENTICATED else AuthState.UNAUTHENTICATED
                        }
                    }
                    
                    when (authState) {
                        AuthState.CHECKING -> SplashScreen()
                        AuthState.AUTHENTICATED -> {
                            // Set a static flag to track this app session
                            if (!AppSessionFlags.hasCheckedGoalThisSession) {
                                AppSessionFlags.hasCheckedGoalThisSession = true
                                
                                // Only show goal setup on first launch when there's no goal
                                val startScreen = if (!hasSetGoal) Screen.GOAL_SETUP else Screen.HOME
                                
                                MainNavigation(
                                    questViewModel = questViewModel,
                                    userViewModel = userViewModel,
                                    onDailyQuestScreen = { DailyQuestScreen(questViewModel) },
                                    startScreen = startScreen
                                )
                            } else {
                                // On subsequent navigation, always go to HOME
                                MainNavigation(
                                    questViewModel = questViewModel,
                                    userViewModel = userViewModel,
                                    onDailyQuestScreen = { DailyQuestScreen(questViewModel) },
                                    startScreen = Screen.HOME
                                )
                            }
                        }
                        AuthState.UNAUTHENTICATED -> {
                            MainNavigation(
                                questViewModel = questViewModel,
                                userViewModel = userViewModel,
                                onDailyQuestScreen = { DailyQuestScreen(questViewModel) },
                                startScreen = Screen.LOGIN
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
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Daily Quest App", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    
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
        val currentQuest by viewModel.displayedQuestFlow.collectAsState()
        val currentQuestDetails by viewModel.currentQuestDetails.collectAsState()
        val userGoal by viewModel.userGoal.collectAsState()
        val needsGoalMessage by viewModel.needsGoalMessage.collectAsState()
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Daily Quest", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(12.dp))
                    Text("💰 $totalPoints", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(12.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = { timeToNextQuest.value.progress },
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("🔥", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "$currentStreak Day${if (currentStreak > 1) "s" else ""}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = !showReward,
                    enter = fadeIn(animationSpec = tween(600)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .heightIn(min = 280.dp)
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isGeneratingQuest && currentQuest.first.startsWith("Loading")) {
                        Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "loadingPulse")
                                val scale by infiniteTransition.animateValue(
                                    initialValue = 0.8f,
                                    targetValue = 1.2f,
                                    typeConverter = Float.VectorConverter,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulseAnimation"
                                )
                                
                                val alpha by infiniteTransition.animateValue(
                                    initialValue = 0.6f,
                                    targetValue = 1.0f,
                                    typeConverter = Float.VectorConverter,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alphaAnimation"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .scale(scale)
                                        .alpha(alpha),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 1.5.dp
                                    )
                                }
                                
                                Spacer(Modifier.height(12.dp))
                                
                                val loadingText = "Crafting your personalized quest..."
                                
                                var displayedTextLength by remember { mutableStateOf(0) }
                                
                                LaunchedEffect(Unit) {
                                    while (true) {
                                        if (displayedTextLength >= loadingText.length + 5) {
                                            displayedTextLength = 0
                                            delay(500)
                                        }
                                        
                                        if (displayedTextLength <= loadingText.length) {
                                            displayedTextLength++
                                        } else {
                                            displayedTextLength++
                                        }
                                        
                                        delay(100)
                                    }
                                }
                                
                                Text(
                                    text = loadingText.take(
                                        minOf(displayedTextLength, loadingText.length)
                                    ) + if (displayedTextLength % 2 == 0) "_" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (needsGoalMessage || currentQuest.first.contains("Set a goal")) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Set Goal First",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            
                            Text(
                                    text = "Start Your Journey!",
                                    style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                ) {
                                    val messageText = currentQuest.first
                                    
                                    Text(
                                        text = messageText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            
                            Spacer(Modifier.height(16.dp))
                            
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(context, GoalActivity::class.java))
                                    },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .defaultMinSize(minWidth = 140.dp, minHeight = 48.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Set Goal"
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Set My Goal",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        } else {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                val isCompactHeight = maxHeight < 600.dp
                                
                                val baseStyle = when {
                                    isCompactHeight -> MaterialTheme.typography.bodyMedium
                                    maxWidth < 360.dp -> MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.9
                                    )
                                    maxWidth > 600.dp -> MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.1
                                    )
                                    else -> MaterialTheme.typography.bodyLarge
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🎯Your task is:", style = baseStyle)
                                    
                                    AnimatedContent(
                                        targetState = currentQuest.first,
                                        transitionSpec = {
                                            (slideInVertically { height -> height } + fadeIn(animationSpec = tween(600)))
                                                .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(600)))
                                        },
                                        label = "QuestTransition"
                                    ) { questText ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                        ) {
                                            val isLongText = questText.length > 150
                                            var showFullText by remember { mutableStateOf(false) }
                                            
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = questText.trim(),
                                                        style = baseStyle,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = if (isLongText) 3 else 4,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier
                                                            .padding(horizontal = 4.dp)
                                                            .fillMaxWidth()
                                                    )
                                                    
                                                    // Remove the magnifying glass icon
                                                }
                                                
                                                // Show a clear "View Full Details" button at the bottom for long text
                                                if (isLongText) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Button(
                                                        onClick = { showFullText = true },
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.8f)
                                                            .height(32.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    ) {
                                                        Text(
                                                            "View Full Details",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            FullTextDialog(
                                                text = questText.trim(),
                                                title = "Your Task",
                                                isVisible = showFullText,
                                                onDismiss = { showFullText = false }
                                            )
                                        }
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("🏆 Reward:", style = baseStyle)
                                        Spacer(Modifier.width(4.dp))
                                        
                                        AnimatedContent(
                                            targetState = currentQuest.second,
                                            transitionSpec = {
                                                fadeIn(animationSpec = tween(500))
                                                    .togetherWith(fadeOut(animationSpec = tween(500)))
                                            },
                                            label = "PointsTransition"
                                        ) { points ->
                                            Text(
                                                text = "$points points",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = baseStyle
                                            )
                                        }
                                    }
                                    
                                    if (userGoal != null && currentQuestDetails != null && currentQuest.second > 0) {
                                        Spacer(Modifier.height(8.dp))
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.Start
                                            ) {
                                                val goalTitle = userGoal?.title ?: ""
                                                
                                                Text(
                                                    text = "Goal: $goalTitle",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Relevance:",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        
                                                        AnimatedContent(
                                                            targetState = viewModel.getGoalRelevanceText(),
                                                            transitionSpec = {
                                                                fadeIn(animationSpec = tween(500))
                                                                    .togetherWith(fadeOut(animationSpec = tween(500)))
                                                            },
                                                            label = "RelevanceTransition"
                                                        ) { relevanceText ->
                                                            Text(
                                                                text = relevanceText,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                    }
                                                    Spacer(Modifier.height(1.dp))
                                                    
                                                    val animatedRelevance = animateFloatAsState(
                                                        targetValue = currentQuestDetails?.goalRelevance?.toFloat()?.div(100f) ?: 0.5f,
                                                        animationSpec = tween(1000),
                                                        label = "RelevanceAnimation"
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { animatedRelevance.value },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Progress:",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        
                                                        AnimatedContent(
                                                            targetState = viewModel.getGoalProgressText(),
                                                            transitionSpec = {
                                                                fadeIn(animationSpec = tween(500))
                                                                    .togetherWith(fadeOut(animationSpec = tween(500)))
                                                            },
                                                            label = "ProgressTextTransition"
                                                        ) { progressText ->
                                                            Text(
                                                                text = progressText,
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                    }
                                                    Spacer(Modifier.height(1.dp))
                                                    
                                                    val animatedProgress = animateFloatAsState(
                                                        targetValue = currentQuestDetails?.goalProgress?.toFloat()?.div(100f) ?: 0.1f,
                                                        animationSpec = tween(1000),
                                                        label = "ProgressAnimation"
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { animatedProgress.value },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = showReward,
                    enter = fadeIn(animationSpec = tween(600)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(70.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Preparing your next quest...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isNarrow = maxWidth < 320.dp
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = showReward) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = if (wasRejected) Icons.Filled.Close else Icons.Default.CheckCircle,
                                contentDescription = if (wasRejected) "Rejected" else "Completed",
                                    tint = if (wasRejected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                    text = if (wasRejected) "Quest rejected" else "+${currentQuest.second} points",
                                style = MaterialTheme.typography.headlineSmall,
                                    color = if (wasRejected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                            horizontalArrangement = if (isNarrow) Arrangement.Center else Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
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
                                enabled = !needsGoalMessage && rejectCount < 5 && !showReward && currentQuest.second > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier
                                    .size(40.dp)
                                    .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close, 
                                    contentDescription = "Reject Quest (${5 - rejectCount} left)",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            if (isNarrow) Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                    if (!showReward && currentQuest.second > 0) {
                                    val calendar = Calendar.getInstance()
                                    val today = calendar.get(Calendar.DAY_OF_YEAR)
                                        val isNewDay = lastClaimedDay != today
                                        val newStreak = if (isNewDay && lastClaimedDay == today - 1) currentStreak + 1 else 1

                                        val points = currentQuest.second
                                        val newTotalPoints = progressState.points + points

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
                                    .size(40.dp)
                                    .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
                                .scale(if (showReward) 0.9f else 1f),
                                shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showReward) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                                                else MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(0.dp),
                                enabled = !needsGoalMessage && !showReward && currentQuest.second > 0
                        ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Claim Reward",
                                    modifier = Modifier.size(20.dp)
                                )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = when {
                            needsGoalMessage -> currentQuest.first
                            showReward -> "New quest loading..."
                            currentQuest.first.startsWith("Loading") -> "Generating new quest..."
                            else -> "Next adventure in: ${timeToNextQuest.value.formattedTime}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    }
                }
            }
        }

        LaunchedEffect(showReward) {
            if (showReward) {
                delay(2000)
                showReward = false
                if (!needsGoalMessage) {
                    viewModel.generateAIQuest()
                }
                wasRejected = false
            }
        }

        AnimatedVisibility(
            visible = showStreakAnimation,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val baseRadius = size.minDimension / 4
                
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    radius = baseRadius * 1.2f * (0.9f + animatedProgress.value * 0.2f),
                    center = center
                )
                
                val particleCount = 12
                val angleStep = (2f * PI.toFloat() / particleCount)
                
                repeat(particleCount) { index ->
                    val waveOffset = sin(animatedProgress.value * PI.toFloat() * 2f + index * 0.5f).toFloat() * 15f
                    val distanceFromCenter = baseRadius * 0.8f + waveOffset
                    val angle = angleStep * index + animatedProgress.value * PI.toFloat() * 2f
                    val x = center.x + cos(angle) * distanceFromCenter
                    val y = center.y + sin(angle) * distanceFromCenter
                    val particleSize = 20.dp.toPx() * (0.7f + kotlin.math.abs(sin(animatedProgress.value * PI.toFloat() * 3f + index * 0.4f)).toFloat() * 0.5f)
                    val hue = (index / particleCount.toFloat()) * 60f + animatedProgress.value * 30f
                    val particleColor = androidx.compose.ui.graphics.Color.hsv(
                        hue = hue,
                        saturation = 0.9f,
                        value = 1.0f
                    ).copy(alpha = 0.8f)
                    
                    drawCircle(
                        color = particleColor,
                        radius = particleSize,
                        center = Offset(x, y),
                        blendMode = BlendMode.Screen
                    )
                }
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f * (1f - animatedProgress.value)),
                    radius = 40.dp.toPx() * (1f - animatedProgress.value * 0.7f),
                    center = center,
                    blendMode = BlendMode.Screen
                )
            }
        }

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
                viewModel.loadSeed(System.currentTimeMillis())
            }
        }

        LaunchedEffect(showStreakAnimation) {
            if (showStreakAnimation) {
                delay(2000)
                showStreakAnimation = false
            }
        }

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

// Add a custom composable function for auto-sizing text
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = 24.sp,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Color.Unspecified,
    scrollable: Boolean = false,
    allowScaling: Boolean = true
) {
    // Remember a scroll state only if scrollable is true
    val scrollState = if (scrollable) rememberScrollState() else null
    
    // Apply scrollable modifier conditionally
    val contentModifier = if (scrollable) {
        modifier.verticalScroll(scrollState!!)
    } else {
        modifier
    }
    
    // Use fixed text size without scaling - remove adaptive behavior
    Text(
        text = text,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        color = color,
        modifier = contentModifier
    )
}

// Add a FullTextDialog for showing text in fullscreen
@Composable
fun FullTextDialog(
    text: String,
    title: String = "Task Details",
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}