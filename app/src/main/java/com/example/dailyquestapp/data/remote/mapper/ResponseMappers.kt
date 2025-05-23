package com.example.dailyquestapp.data.remote.mapper

import com.example.dailyquestapp.data.local.ProgressData
import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.data.remote.dto.*
import java.text.SimpleDateFormat
import java.util.*

// AuthResponse to local data
fun AuthResponse.toProgressData(): ProgressData {
    return ProgressData(
        points = this.totalPoints,
        streak = this.currentStreak,
        lastDay = -1 // Will be updated with correct value when user completes a quest
    )
}

// ProgressResponse to ProgressData
fun ProgressResponse.toProgressData(): ProgressData {
    return ProgressData(
        points = this.totalPoints,
        streak = this.currentStreak,
        lastDay = this.lastClaimedDay
    )
}

// Note: Renamed from toTaskProgress to avoid conflicts with TaskHistoryMapper.kt
// Use this version only for simple task progress without goal information
fun TaskHistoryDto.toBasicTaskProgress(): TaskProgress {
    val parseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val parsedDate = parseDateFormat.parse(this.timestamp)
    return TaskProgress(
        quest = this.quest,
        points = this.points,
        status = TaskStatus.valueOf(this.status),
        date = if (parsedDate != null) dateFormat.format(parsedDate) else "",
        time = if (parsedDate != null) timeFormat.format(parsedDate) else ""
    )
}

// Note: toTaskProgressList was removed to avoid conflicts with the implementation in TaskHistoryMapper.kt
