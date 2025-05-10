package com.example.dailyquestapp.presentation.quest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.ai.processor.AIProcessor
import com.example.dailyquestapp.ai.processor.GoalInfo
import com.example.dailyquestapp.ai.processor.QuestResult
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.local.UserGoalData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf
import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Remove @HiltViewModel annotation for now since we're not using Hilt DI
// @HiltViewModel
class QuestViewModel constructor(
    private val dataStoreManager: DataStoreManager,
    private val progressRepository: ProgressRepository
) : ViewModel(), KoinComponent {

    // Inject AIProcessor
    private val aiProcessor: AIProcessor by inject()

    private val _progress = MutableStateFlow(ProgressData())
    val progress: StateFlow<ProgressData> = _progress.asStateFlow()

    private val _currentSeed = MutableStateFlow(System.currentTimeMillis()) // Initialize with current time
    val currentSeed: StateFlow<Long> = _currentSeed.asStateFlow()

    private val _rejectInfo = MutableStateFlow(Pair(0, -1))
    val rejectInfo: StateFlow<Pair<Int, Int>> = _rejectInfo.asStateFlow()

    // State for AI-generated quest
    private val _currentQuest = MutableStateFlow<Pair<String, Int>?>(null)
    val currentQuestInternal: StateFlow<Pair<String, Int>?> = _currentQuest.asStateFlow() // Renamed for clarity
    
    // Enhanced quest result with goal information
    private val _currentQuestDetails = MutableStateFlow<QuestResult?>(null)
    val currentQuestDetails: StateFlow<QuestResult?> = _currentQuestDetails.asStateFlow()

    // Loading state for AI quest generation
    private val _isGeneratingQuest = MutableStateFlow(false)
    val isGeneratingQuest: StateFlow<Boolean> = _isGeneratingQuest.asStateFlow()
    
    // Current user goal
    private val _userGoal = MutableStateFlow<UserGoalData?>(null)
    val userGoal: StateFlow<UserGoalData?> = _userGoal.asStateFlow()
    
    // New state to track if we should show the "set goal first" message
    private val _needsGoalMessage = MutableStateFlow(false)
    val needsGoalMessage: StateFlow<Boolean> = _needsGoalMessage.asStateFlow()

    // Fallback quests in case AI generation fails
    internal val fallbackQuests = listOf( // Made internal for access in combine
        "to take a selfie with a fish",
        "to draw a picture with crayons",
        "to write a poem about a cat",
        "to eat a slice of pizza",
        "to play a tune on a harmonica",
        "to dance with a broom",
        "to play a game of chess",
        "to cook a scrambled egg",
        "to solve a Rubik's cube",
        "to recite a Shakespearean sonnet"
    )
    
    // Combined flow for the UI to display the current quest string and points
    val displayedQuestFlow: StateFlow<Pair<String, Int>> =
        combine(
            _currentQuest, // Use the internal _currentQuest
            _needsGoalMessage,
            _userGoal,
            _currentSeed
        ) { questNullable, needsGoal, goalData, seedValue ->
            if (needsGoal) {
                Pair("Set a goal first to get personalized quests!", 0)
            } else {
                questNullable ?: run { // AI quest is null, use fallback
                    if (goalData == null) { // Should ideally not happen if needsGoal is false
                        Pair("Set a goal to unlock quests!", 0)
                    } else {
                        val random = Random(seedValue)
                        val index = random.nextInt(fallbackQuests.size)
                        Pair(fallbackQuests[index], 100 + index * 100)
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Pair("Loading your first quest...", 0) // Initial sensible value
        )

    init {
        viewModelScope.launch {
            progressRepository.getProgressFlow().collect { progressData ->
                _progress.value = progressData
            }
        }

        viewModelScope.launch {
            dataStoreManager.seedFlow.collect { (seed, day) ->
                val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                if (day != today) {
                    val newSeed = System.currentTimeMillis()
                    dataStoreManager.saveSeed(newSeed, today)
                    _currentSeed.value = newSeed
                    checkGoalAndGenerateQuest()
                } else {
                    _currentSeed.value = seed
                    if (displayedQuestFlow.value.first == "Loading your first quest..." || _currentQuest.value == null) { // Check against displayedQuestFlow or _currentQuest
                        checkGoalAndGenerateQuest()
                    }
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.rejectInfoFlow.collect { info ->
                _rejectInfo.value = info
            }
        }
        
        viewModelScope.launch {
            dataStoreManager.userGoalFlow.collect { goalData ->
                val hadGoalBefore = _userGoal.value != null
                _userGoal.value = goalData
                
                if (!hadGoalBefore && goalData != null) {
                    if (_currentQuest.value == null) {
                        generateAIQuest() // This will set _currentQuest
                    }
                    _needsGoalMessage.value = false
                } else if (goalData == null) {
                    // If goal is removed or becomes null, we need to show the message
                    _needsGoalMessage.value = true
                    _currentQuest.value = null // Clear any existing quest
                    _currentQuestDetails.value = null
                }
            }
        }
    }
    
    private fun checkGoalAndGenerateQuest() {
        viewModelScope.launch {
            if (_userGoal.value != null) {
                generateAIQuest()
                _needsGoalMessage.value = false
            } else {
                _needsGoalMessage.value = true
                _currentQuest.value = null
                _currentQuestDetails.value = null
            }
        }
    }

    fun saveProgress(points: Int, streak: Int, lastDay: Int, quest: String, questPoints: Int, status: TaskStatus) {
        viewModelScope.launch {
            dataStoreManager.saveProgress(points, streak, lastDay)
            dataStoreManager.saveTaskHistory(quest, questPoints, status)
            
            if (status == TaskStatus.COMPLETED) {
                try {
                    val goal = _userGoal.value
                    val questDetails = _currentQuestDetails.value
                    val goalId = goal?.remoteId
                    val goalProgress = questDetails?.goalProgress ?: 0
                    
                    progressRepository.saveTaskHistory(
                        quest = quest, 
                        points = questPoints, 
                        status = status,
                        goalId = goalId,
                        goalProgress = goalProgress
                    )
                    Log.d("QuestViewModel", "Task completed: $quest, goal: ${goal?.title}, progress: $goalProgress")
                } catch (e: Exception) {
                    Log.e("QuestViewModel", "Failed to upload quest history: ${e.message}")
                }
            } else {
                try {
                    progressRepository.saveTaskHistory(quest, questPoints, status)
                } catch (e: Exception) {
                    Log.e("QuestViewModel", "Failed to upload quest history: ${e.message}")
                }
            }
        }
    }

    @Deprecated("Use displayedQuestFlow instead for UI", ReplaceWith("displayedQuestFlow.value"))
    fun getCurrentQuest(): Pair<String, Int> {
        return displayedQuestFlow.value
    }

    @Deprecated("Fallback logic is now part of displayedQuestFlow")
    private fun getFallbackQuest(seed: Long): Pair<String, Int> {
        // This logic is now incorporated into displayedQuestFlow
        if (_userGoal.value == null) {
            return Pair("Set a goal first to get personalized quests", 0)
        }
        val random = Random(seed)
        val index = random.nextInt(fallbackQuests.size)
        return Pair(fallbackQuests[index], 100 + index * 100)
    }

    fun loadSeed(newSeed: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            dataStoreManager.saveSeed(newSeed, today)
            _currentSeed.value = newSeed
            checkGoalAndGenerateQuest()
        }
    }

    fun updateRejectInfo(count: Int, day: Int) {
        viewModelScope.launch {
            dataStoreManager.saveRejectInfo(count, day)
            _rejectInfo.value = Pair(count, day)
        }
    }

    fun generateAIQuest() {
        viewModelScope.launch {
            if (_userGoal.value == null) {
                _needsGoalMessage.value = true
                _currentQuest.value = null // Ensure no quest is shown
                _currentQuestDetails.value = null
                return@launch
            }
            
            _isGeneratingQuest.value = true
            _needsGoalMessage.value = false // We have a goal, so no need for the message
            try {
                val goal = _userGoal.value!! // Safe due to the check above
                
                val recentTasks = dataStoreManager.getTaskHistory()
                    .first()
                    .filter { it.status == TaskStatus.COMPLETED }
                    .take(5)
                
                Log.d("QuestViewModel", "Generating goal-based quest for goal: ${goal.title}")
                val goalInfo = GoalInfo(
                    title = goal.title,
                    description = goal.description,
                    category = goal.category
                )
                
                val difficulty = when {
                    _progress.value.streak > 10 -> "hard"
                    _progress.value.streak > 5 -> "medium"
                    else -> "easy"
                }
                
                val userContext = generatePersonalizedPrompt(recentTasks)
                
                aiProcessor.generateGoalBasedQuest(
                    userPrompt = userContext,
                    goalInfo = goalInfo,
                    difficulty = difficulty
                ).collect { questResult ->
                    _currentQuest.value = Pair(questResult.quest, questResult.points)
                    _currentQuestDetails.value = questResult
                    Log.d("QuestViewModel", "Generated goal-based quest: ${questResult.quest} " +
                            "with ${questResult.points} points, relevance: ${questResult.goalRelevance}%, " +
                            "progress: ${questResult.goalProgress}%, category: ${questResult.category}")
                }
            } catch (e: Exception) {
                Log.e("QuestViewModel", "Failed to generate AI quest: ${e.message}")
                // When AI fails, _currentQuest remains null, displayedQuestFlow will use fallback
                _currentQuest.value = null 
                _currentQuestDetails.value = null // Clear details too
            } finally {
                _isGeneratingQuest.value = false
            }
        }
    }

    private fun generatePersonalizedPrompt(recentTasks: List<TaskProgress> = emptyList()): String {
        val progressData = _progress.value
        val streak = progressData.streak
        val points = progressData.points
        
        val basePrompt = when {
            streak > 10 -> "Generate a challenging daily quest for an advanced user with a ${streak}-day streak and ${points} points."
            streak > 5 -> "Generate a moderately difficult daily quest for an intermediate user with a ${streak}-day streak."
            else -> "Generate a simple and achievable daily quest for a beginner user."
        }
        
        if (recentTasks.isNotEmpty()) {
            val historyPrompt = recentTasks.joinToString("\n") { 
                "- ${it.date}: ${it.quest}" 
            }
            
            return """
                $basePrompt
                
                PREVIOUS TASKS (DO NOT create anything similar to these):
                $historyPrompt
                
                IMPORTANT: Generate a task that is COMPLETELY DIFFERENT from all previous tasks.
                It should involve different skills, different actions, and a different domain.
                If previous tasks were mostly physical, generate a mental or creative challenge.
                If previous tasks were mostly mental, generate a physical or social challenge.
                
                The new task should feel fresh and novel to the user.
            """
        }
        
        return basePrompt
    }
    
    fun getGoalRelevanceText(): String {
        val questDetails = _currentQuestDetails.value ?: return ""
        
        return when (questDetails.goalRelevance) {
            in 0..25 -> "Slightly related to your goal"
            in 26..50 -> "Somewhat related to your goal"
            in 51..75 -> "Strongly related to your goal"
            else -> "Directly advances your goal"
        }
    }
    
    fun getGoalProgressText(): String {
        val questDetails = _currentQuestDetails.value ?: return ""
        
        return when (questDetails.goalProgress) {
            in 0..10 -> "Small step forward"
            in 11..30 -> "Good progress"
            in 31..60 -> "Significant progress" 
            else -> "Major advancement"
        }
    }
}