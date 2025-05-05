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

// TaskHistoryDto to TaskProgress
fun TaskHistoryDto.toTaskProgress(): TaskProgress {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val date = dateFormat.parse(this.timestamp)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val simpleTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    return TaskProgress(
        quest = this.quest,
        points = this.points,
        status = TaskStatus.valueOf(this.status),
        date = simpleDateFormat.format(date ?: Date()),
        time = simpleTimeFormat.format(date ?: Date())
    )
}

// List<TaskHistoryDto> to List<TaskProgress>
fun List<TaskHistoryDto>.toTaskProgressList(): List<TaskProgress> {
    return this.map { it.toTaskProgress() }
}
