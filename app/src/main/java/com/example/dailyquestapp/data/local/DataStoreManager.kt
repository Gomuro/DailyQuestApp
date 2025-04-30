package com.example.dailyquestapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_progress")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val TOTAL_POINTS = intPreferencesKey("total_points")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_CLAIMED_DAY = intPreferencesKey("last_claimed_day")
        val CURRENT_SEED = longPreferencesKey("current_seed")
        val SEED_DAY = intPreferencesKey("seed_day")
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
}

data class ProgressData(
    val points: Int = 0,
    val streak: Int = 0,
    val lastDay: Int = -1
)
