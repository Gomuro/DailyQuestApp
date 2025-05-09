package com.example.dailyquestapp.domain.model

import java.util.*

data class UserGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetDate: Date? = null,
    val createdDate: Date = Date(),
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val category: GoalCategory = GoalCategory.PERSONAL
)

enum class GoalCategory {
    PERSONAL, HEALTH, CAREER, EDUCATION, FINANCIAL, OTHER
} 