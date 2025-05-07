package com.example.dailyquestapp.presentation.quest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.local.ProgressData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf
import com.example.dailyquestapp.data.local.TaskStatus
import org.koin.core.component.KoinComponent

// Remove @HiltViewModel annotation for now since we're not using Hilt DI
// @HiltViewModel
class QuestViewModel constructor(
    private val dataStoreManager: DataStoreManager,
    private val progressRepository: ProgressRepository
) : ViewModel(), KoinComponent {

    private val _progress = MutableStateFlow(ProgressData())
    val progress: StateFlow<ProgressData> = _progress.asStateFlow()

    private val _currentSeed = MutableStateFlow(0L)
    val currentSeed: StateFlow<Long> = _currentSeed.asStateFlow()

    private val _rejectInfo = MutableStateFlow(Pair(0, -1))
    val rejectInfo: StateFlow<Pair<Int, Int>> = _rejectInfo.asStateFlow()

    private val quests = listOf(
        " to take a selfie with a fish",
        " to draw a picture with crayons",
        " to write a poem about a cat",
        " to eat a slice of pizza",
        " to play a tune on a harmonica",
        " to dance with a broom",
        " to play a game of chess",
        " to cook a scrambled egg",
        " to solve a Rubik's cube",
        " to recite a Shakespearean sonnet"
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
                _currentSeed.value = if (day == today) seed else {
                    val newSeed = System.currentTimeMillis()
                    dataStoreManager.saveSeed(newSeed, today)
                    newSeed
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.rejectInfoFlow.collect { info ->
                _rejectInfo.value = info
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

    fun getCurrentQuest() = getTodayQuest(_currentSeed.value)

    private fun getTodayQuest(seed: Long): Pair<String, Int> {
        val random = Random(seed)
        val index = random.nextInt(quests.size)
        return Pair(quests[index], index)
    }

    fun loadSeed(newSeed: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            dataStoreManager.saveSeed(newSeed, today)
            _currentSeed.value = newSeed
        }
    }

    fun updateRejectInfo(count: Int, day: Int) {
        viewModelScope.launch {
            dataStoreManager.saveRejectInfo(count, day)
            _rejectInfo.value = Pair(count, day)
        }
    }
}