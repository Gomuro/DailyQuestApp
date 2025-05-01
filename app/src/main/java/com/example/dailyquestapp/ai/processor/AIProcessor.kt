package com.example.dailyquestapp.ai.processor

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class QuestResult(
    val quest: String,
    val points: Int
)

interface AIProcessor {
    suspend fun generateQuest(prompt: String): Flow<QuestResult>
}

class OpenAIProcessor(private val openAI: OpenAI) : AIProcessor {
    override suspend fun generateQuest(prompt: String): Flow<QuestResult> = flow {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = """
                    You are an advanced daily quest generator that creates personalized, meaningful tasks to help users achieve their goals.

                    CORE PRINCIPLES:
                    1. Personalization: Each quest should be tailored to the user's interests, skills, and goals
                    2. Progression: Quests should build upon previous achievements
                    3. Balance: Tasks should be challenging but achievable
                    4. Impact: Each quest should contribute to long-term growth

                    QUEST REQUIREMENTS:
                    - Format: "quest description | points"
                    - Length: 1-2 sentences maximum
                    - Tone: Motivational and encouraging
                    - Specificity: Clear, actionable steps
                    - Points: Based on difficulty (50-1000 points)

                    POINTS SYSTEM:
                    - Easy tasks: 50-200 points
                    - Medium tasks: 201-500 points
                    - Hard tasks: 501-1000 points

                    EXAMPLES:
                    For a programmer:
                    "Implement a basic REST API endpoint | 300"
                    "Complete a code review for a colleague | 200"
                    "Learn and apply a new design pattern | 500"

                    For a designer:
                    "Create a wireframe for a mobile app | 400"
                    "Research and document color psychology principles | 250"
                    "Design a responsive layout for a landing page | 600"

                    For a fitness enthusiast:
                    "Complete a 30-minute HIIT workout | 300"
                    "Track and analyze your nutrition for a day | 200"
                    "Learn and practice a new strength pose | 150"

                    RESPONSE FORMAT:
                    Always respond with exactly one quest in the format: "quest description | points"
                    Do not include any additional text or explanations.
                    """.trimIndent()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )
        
        val completion = openAI.chatCompletion(chatCompletionRequest)
        val response = completion.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Failed to generate quest: No response from OpenAI")
        
        // Parse response
        val parts = response.split("|").map { it.trim() }
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid response format: $response")
        }
        
        val points = parts[1].toIntOrNull() 
            ?: throw NumberFormatException("Invalid points format: ${parts[1]}")
        
        emit(QuestResult(
            quest = parts[0],
            points = points
        ))
    }
} 