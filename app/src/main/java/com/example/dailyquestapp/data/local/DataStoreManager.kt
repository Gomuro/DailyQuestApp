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
        val THEME_PREFERENCE = intPreferencesKey("theme_preference")
        
        // Goal related preferences
        val USER_GOAL_TITLE = stringPreferencesKey("user_goal_title")
        val USER_GOAL_DESCRIPTION = stringPreferencesKey("user_goal_description")
        val USER_GOAL_CATEGORY = stringPreferencesKey("user_goal_category")
        val USER_GOAL_TARGET_DATE = longPreferencesKey("user_goal_target_date")
        val USER_GOAL_CREATED_DATE = longPreferencesKey("user_goal_created_date")
        val USER_GOAL_COMPLETED = booleanPreferencesKey("user_goal_completed")
        val USER_GOAL_ACTIVE = booleanPreferencesKey("user_goal_active")
        val HAS_SET_INITIAL_GOAL = booleanPreferencesKey("has_set_initial_goal")
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

    suspend fun saveThemePreference(themeMode: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = themeMode
        }
    }

    val themePreferenceFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[THEME_PREFERENCE] ?: ThemeMode.SYSTEM.value
        }

    /**
     * Update local task history with remote data
     * This method will replace the existing task history with the provided list
     */
    suspend fun updateTaskHistoryCache(tasks: List<TaskProgress>) {
        if (tasks.isEmpty()) return
        
        val historyEntries = tasks.joinToString("\n") { task ->
            val pointsPrefix = if (task.status == TaskStatus.COMPLETED) "+" else ""
            "${task.date}|${task.time}|${task.quest.trim()}|${pointsPrefix}${task.points}pts|${task.status}"
        }
        
        dataStore.edit { preferences ->
            preferences[TASK_HISTORY] = historyEntries
        }
    }

    /**
     * Save the user's goal information
     */
    suspend fun saveUserGoal(
        title: String,
        description: String,
        category: String,
        targetDate: Date? = null
    ) {
        dataStore.edit { preferences ->
            preferences[USER_GOAL_TITLE] = title
            preferences[USER_GOAL_DESCRIPTION] = description
            preferences[USER_GOAL_CATEGORY] = category
            preferences[USER_GOAL_CREATED_DATE] = Date().time
            targetDate?.let { preferences[USER_GOAL_TARGET_DATE] = it.time }
            preferences[USER_GOAL_COMPLETED] = false
            preferences[USER_GOAL_ACTIVE] = true
            preferences[HAS_SET_INITIAL_GOAL] = true
        }
    }

    /**
     * Check if the user has set their initial goal
     */
    val hasSetInitialGoal: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[HAS_SET_INITIAL_GOAL] ?: false
        }

    /**
     * Set the initial goal flag directly
     */
    suspend fun setInitialGoalFlag(hasSet: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SET_INITIAL_GOAL] = hasSet
        }
    }

    /**
     * Get the user's current goal
     */
    val userGoalFlow: Flow<UserGoalData?> = dataStore.data
        .map { preferences ->
            val title = preferences[USER_GOAL_TITLE] ?: return@map null
            val description = preferences[USER_GOAL_DESCRIPTION] ?: ""
            
            UserGoalData(
                title = title,
                description = description,
                category = preferences[USER_GOAL_CATEGORY] ?: "PERSONAL",
                targetDate = preferences[USER_GOAL_TARGET_DATE]?.let { Date(it) },
                createdDate = Date(preferences[USER_GOAL_CREATED_DATE] ?: Date().time),
                isCompleted = preferences[USER_GOAL_COMPLETED] ?: false,
                isActive = preferences[USER_GOAL_ACTIVE] ?: true,
                remoteId = null,
                progress = 0
            )
        }

    /**
     * Mark the current goal as completed
     */
    suspend fun completeUserGoal() {
        dataStore.edit { preferences ->
            preferences[USER_GOAL_COMPLETED] = true
            preferences[USER_GOAL_ACTIVE] = false
        }
    }

    /**
     * Reset the user's goal (for creating a new one)
     */
    suspend fun resetUserGoal() {
        dataStore.edit { preferences ->
            preferences.remove(USER_GOAL_TITLE)
            preferences.remove(USER_GOAL_DESCRIPTION)
            preferences.remove(USER_GOAL_CATEGORY)
            preferences.remove(USER_GOAL_TARGET_DATE)
            preferences.remove(USER_GOAL_COMPLETED)
            preferences.remove(USER_GOAL_ACTIVE)
            // Don't reset the HAS_SET_INITIAL_GOAL flag
        }
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
    val time: String,
    val goalInfo: TaskProgressGoalInfo? = null
)

data class TaskProgressGoalInfo(
    val id: String,
    val title: String,
    val category: String
)

enum class TaskStatus { COMPLETED, REJECTED }

enum class ThemeMode(val value: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2)
}

data class UserGoalData(
    val title: String,
    val description: String,
    val category: String,
    val targetDate: Date? = null,
    val createdDate: Date = Date(),
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val remoteId: String? = null,
    val progress: Int = 0
)
