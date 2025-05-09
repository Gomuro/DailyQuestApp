package com.example.dailyquestapp.presentation.quest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.ai.processor.AIProcessor
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

    private val _currentSeed = MutableStateFlow(0L)
    val currentSeed: StateFlow<Long> = _currentSeed.asStateFlow()

    private val _rejectInfo = MutableStateFlow(Pair(0, -1))
    val rejectInfo: StateFlow<Pair<Int, Int>> = _rejectInfo.asStateFlow()

    // State for AI-generated quest
    private val _currentQuest = MutableStateFlow<Pair<String, Int>?>(null)
    val currentQuest: StateFlow<Pair<String, Int>?> = _currentQuest.asStateFlow()

    // Loading state for AI quest generation
    private val _isGeneratingQuest = MutableStateFlow(false)
    val isGeneratingQuest: StateFlow<Boolean> = _isGeneratingQuest.asStateFlow()
    
    // Current user goal
    private val _userGoal = MutableStateFlow<UserGoalData?>(null)

    // Fallback quests in case AI generation fails
    private val fallbackQuests = listOf(
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
                    // New day, generate a new quest
                    val newSeed = System.currentTimeMillis()
                    dataStoreManager.saveSeed(newSeed, today)
                    _currentSeed.value = newSeed
                    generateAIQuest()
                } else {
                    _currentSeed.value = seed
                    // Try to load existing AI quest or generate a new one
                    if (_currentQuest.value == null) {
                        generateAIQuest()
                    }
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.rejectInfoFlow.collect { info ->
                _rejectInfo.value = info
            }
        }
        
        // Load user goal data
        viewModelScope.launch {
            dataStoreManager.userGoalFlow.collect { goalData ->
                _userGoal.value = goalData
            }
        }
    }

    fun saveProgress(points: Int, streak: Int, lastDay: Int, quest: String, questPoints: Int, status: TaskStatus) {
        viewModelScope.launch {
            dataStoreManager.saveProgress(points, streak, lastDay)
            dataStoreManager.saveTaskHistory(quest, questPoints, status)
            // Also upload to server
            try {
                progressRepository.saveTaskHistory(quest, questPoints, status)
            } catch (e: Exception) {
                Log.e("QuestViewModel", "Failed to upload quest history: ${e.message}")
            }
        }
    }

    fun getCurrentQuest(): Pair<String, Int> {
        // Return AI-generated quest if available, otherwise use fallback
        return _currentQuest.value ?: getFallbackQuest(_currentSeed.value)
    }

    private fun getFallbackQuest(seed: Long): Pair<String, Int> {
        val random = Random(seed)
        val index = random.nextInt(fallbackQuests.size)
        return Pair(fallbackQuests[index], 100 + index * 100) // Points between 100-1000
    }

    fun loadSeed(newSeed: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            dataStoreManager.saveSeed(newSeed, today)
            _currentSeed.value = newSeed
            // Generate a new AI quest when seed changes
            generateAIQuest()
        }
    }

    fun updateRejectInfo(count: Int, day: Int) {
        viewModelScope.launch {
            dataStoreManager.saveRejectInfo(count, day)
            _rejectInfo.value = Pair(count, day)
        }
    }

    /**
     * Generate a quest using AI
     */
    fun generateAIQuest() {
        viewModelScope.launch {
            _isGeneratingQuest.value = true
            try {
                // Generate personalized prompt based on user data
                val prompt = generatePersonalizedPrompt()
                
                // Use AIProcessor to generate quest
                aiProcessor.generateQuest(prompt).collect { result ->
                    _currentQuest.value = Pair(result.quest, result.points)
                    Log.d("QuestViewModel", "AI generated quest: ${result.quest} with ${result.points} points")
                }
            } catch (e: Exception) {
                Log.e("QuestViewModel", "Failed to generate AI quest: ${e.message}")
                // Use fallback quest if AI generation fails
                _currentQuest.value = getFallbackQuest(_currentSeed.value)
            } finally {
                _isGeneratingQuest.value = false
            }
        }
    }

    /**
     * Generate a personalized prompt for the AI based on user data
     */
    private fun generatePersonalizedPrompt(): String {
        val progressData = _progress.value
        val streak = progressData.streak
        val points = progressData.points
        val goal = _userGoal.value
        
        // Create a prompt based on user's progress and goal
        val basePrompt = when {
            streak > 10 -> "Generate a challenging daily quest for an advanced user with a ${streak}-day streak and ${points} points."
            streak > 5 -> "Generate a moderately difficult daily quest for an intermediate user with a ${streak}-day streak."
            else -> "Generate a simple and achievable daily quest for a beginner user."
        }
        
        // Add goal-specific information if available
        return if (goal != null) {
            val goalContext = "User's main goal is: \"${goal.title}\". " +
                    if (goal.description.isNotBlank()) "Goal description: \"${goal.description}\". " else "" +
                    "Goal category: ${goal.category}. " +
                    "Generate a quest that will help the user make progress toward this goal."
            
            "$basePrompt $goalContext"
        } else {
            basePrompt
        }
    }
}