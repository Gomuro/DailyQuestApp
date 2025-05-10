package com.example.dailyquestapp.data.remote.mapper

import com.example.dailyquestapp.data.local.TaskProgress
import com.example.dailyquestapp.data.local.TaskProgressGoalInfo
import com.example.dailyquestapp.data.local.TaskStatus
import com.example.dailyquestapp.data.remote.dto.TaskHistoryDto
import com.example.dailyquestapp.data.remote.dto.GoalInfoDto
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Convert a list of TaskHistoryDto objects to TaskProgress objects
 */
fun List<TaskHistoryDto>.toTaskProgressList(): List<TaskProgress> {
    return this.map { it.toTaskProgress() }
}

/**
 * Convert a single TaskHistoryDto to TaskProgress
 */
fun TaskHistoryDto.toTaskProgress(): TaskProgress {
    // Format the timestamp to date and time strings
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val parseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    
    val parsedDate = try {
        parseDateFormat.parse(timestamp)
    } catch (e: Exception) {
        null
    }
    
    val date = if (parsedDate != null) dateFormat.format(parsedDate) else "Unknown"
    val time = if (parsedDate != null) timeFormat.format(parsedDate) else "Unknown"
    
    return TaskProgress(
        quest = quest,
        points = points,
        status = try {
            TaskStatus.valueOf(status.uppercase(Locale.ROOT))
        } catch (e: IllegalArgumentException) {
            TaskStatus.REJECTED
        },
        date = date,
        time = time,
        goalInfo = goalInfo?.let {
            TaskProgressGoalInfo(
                id = it.goalId,
                title = it.title,
                category = it.category
            )
        }
    )
} 