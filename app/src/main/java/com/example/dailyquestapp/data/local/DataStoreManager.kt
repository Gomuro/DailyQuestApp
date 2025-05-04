package com.example.dailyquestapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_progress")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val TOTAL_POINTS = intPreferencesKey("total_points")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_CLAIMED_DAY = intPreferencesKey("last_claimed_day")
        val CURRENT_SEED = longPreferencesKey("current_seed")
        val SEED_DAY = intPreferencesKey("seed_day")
        val TASK_HISTORY = stringPreferencesKey("task_history")
        val REJECT_COUNT = intPreferencesKey("reject_count")
        val LAST_REJECT_DAY = intPreferencesKey("last_reject_day")
    }

    suspend fun saveProgress(
        points: Int,
        streak: Int,
        lastDay: Int
    ) {
        dataStore.edit { preferences ->
            preferences[TOTAL_POINTS] = points
            preferences[CURRENT_STREAK] = streak
            preferences[LAST_CLAIMED_DAY] = lastDay
        }
    }

    val progressFlow: Flow<ProgressData> = dataStore.data
        .map { preferences ->
            ProgressData(
                points = preferences[TOTAL_POINTS] ?: 0,
                streak = preferences[CURRENT_STREAK] ?: 0,
                lastDay = preferences[LAST_CLAIMED_DAY] ?: -1
            )
        }

    suspend fun saveSeed(seed: Long, day: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENT_SEED] = seed
            preferences[SEED_DAY] = day
        }
    }

    val seedFlow: Flow<Pair<Long, Int>> = dataStore.data
        .map { preferences ->
            Pair(
                preferences[CURRENT_SEED] ?: 0L,
                preferences[SEED_DAY] ?: -1
            )
        }

    suspend fun saveTaskHistory(quest: String, points: Int, status: TaskStatus) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        val formattedTime = timeFormat.format(calendar.time)
        
        val pointsPrefix = if (status == TaskStatus.COMPLETED) "+" else ""
        val taskEntry = "$formattedDate|$formattedTime|${quest.trim()}|${pointsPrefix}${points}pts|$status"
        
        dataStore.edit { preferences ->
            val currentHistory = preferences[TASK_HISTORY] ?: ""
            val newHistory = if (currentHistory.isEmpty()) {
                taskEntry
            } else {
                "$taskEntry\n$currentHistory"
            }
            preferences[TASK_HISTORY] = newHistory
        }
    }

    fun getTaskHistory(): Flow<List<TaskProgress>> {
        return dataStore.data.map { preferences ->
            val historyString = preferences[TASK_HISTORY] ?: ""
            if (historyString.isEmpty()) {
                emptyList()
            } else {
                historyString.split("\n").mapNotNull { line ->
                    try {
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size == 5) {
                            val date = parts[0]
                            val time = parts[1]
                            val quest = parts[2]
                            val pointsText = parts[3].replace("pts", "").replace("+", "").trim()
                            val points = pointsText.toIntOrNull() ?: 0
                            val status = try {
                                TaskStatus.valueOf(parts[4].uppercase(Locale.ROOT))
                            } catch (e: IllegalArgumentException) {
                                TaskStatus.REJECTED
                            }
                            
                            TaskProgress(
                                quest = quest,
                                points = points,
                                status = status,
                                date = date,
                                time = time
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    suspend fun clearTaskHistory() {
        dataStore.edit { preferences ->
            preferences.remove(TASK_HISTORY)
        }
    }

    suspend fun saveRejectInfo(count: Int, day: Int) {
        dataStore.edit { preferences ->
            preferences[REJECT_COUNT] = count
            preferences[LAST_REJECT_DAY] = day
        }
    }

    val rejectInfoFlow: Flow<Pair<Int, Int>> = dataStore.data
        .map { preferences ->
            Pair(
                preferences[REJECT_COUNT] ?: 0,
                preferences[LAST_REJECT_DAY] ?: -1
            )
        }
}

data class ProgressData(
    val points: Int = 0,
    val streak: Int = 0,
    val lastDay: Int = -1
)

data class TaskProgress(
    val quest: String,
    val points: Int,
    val status: TaskStatus,
    val date: String,
    val time: String
)

enum class TaskStatus { COMPLETED, REJECTED }
