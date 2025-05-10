package com.example.dailyquestapp.ai.processor

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.dailyquestapp.config.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class QuestResult(
    val quest: String,
    val points: Int,
    val goalRelevance: Int = 0,     // A score from 0-100 indicating how relevant the quest is to the user's goal
    val goalProgress: Int = 0,      // A score from 0-100 indicating how much progress this quest contributes to the goal
    val category: String = "",      // Category of the quest (matches goal categories)
    val difficultyLevel: String = "medium" // easy, medium, hard
)

data class GoalInfo(
    val title: String,
    val description: String = "",
    val category: String = "",
    val progress: Int = 0 // 0-100
)

interface AIProcessor {
    suspend fun generateQuest(prompt: String): Flow<QuestResult>
    
    suspend fun generateGoalBasedQuest(
        userPrompt: String,
        goalInfo: GoalInfo,
        difficulty: String = "medium"
    ): Flow<QuestResult>
}

class OpenAIProcessor(private val openAI: OpenAI) : AIProcessor {

    private val defaultModel = ModelId(AppConfig.DEFAULT_MODEL)

    override suspend fun generateQuest(prompt: String): Flow<QuestResult> = flow {
        val chatCompletionRequest = ChatCompletionRequest(
            model = defaultModel,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = getBaseSystemPrompt()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            maxTokens = AppConfig.MAX_TOKENS
        )
        
        val completion = openAI.chatCompletion(chatCompletionRequest)
        val response = completion.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Failed to generate quest: No response from OpenAI")
        
        emit(parseBasicQuestResponse(response))
    }
    
    override suspend fun generateGoalBasedQuest(
        userPrompt: String,
        goalInfo: GoalInfo,
        difficulty: String
    ): Flow<QuestResult> = flow {
        val chatCompletionRequest = ChatCompletionRequest(
            model = defaultModel, // Use advanced model for better goal-based generation
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = getEnhancedSystemPrompt()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = buildGoalBasedPrompt(userPrompt, goalInfo, difficulty)
                )
            ),
            maxTokens = AppConfig.MAX_TOKENS
        )
        
        val completion = openAI.chatCompletion(chatCompletionRequest)
        val response = completion.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Failed to generate quest: No response from OpenAI")
        
        emit(parseEnhancedQuestResponse(response))
    }
    
    private fun buildGoalBasedPrompt(userPrompt: String, goalInfo: GoalInfo, difficulty: String): String {
        return """
            I'm working toward this goal: "${goalInfo.title}"
            ${if (goalInfo.description.isNotBlank()) "Goal description: ${goalInfo.description}" else ""}
            ${if (goalInfo.category.isNotBlank()) "Goal category: ${goalInfo.category}" else ""}
            ${if (goalInfo.progress > 0) "Current progress toward goal: ${goalInfo.progress}%" else ""}
            
            I'd like a ${difficulty.lowercase()} difficulty task that will help me make progress toward this goal.
            
            IMPORTANT: Generate a task that is COMPLETELY DIFFERENT from any task I've done before.
            CRITICAL: The task MUST be completable within a single day (24 hours).
            ESSENTIAL: Focus on ACTIVE, measurable tasks that demonstrate real progress, NOT passive activities like journaling or visualization exercises.
            
            Additional context: $userPrompt
        """.trimIndent()
    }
    
    private fun getBaseSystemPrompt(): String {
        return AppConfig.AIPrompts.QUEST_BASE_PROMPT + """
            
            IMPORTANT INSTRUCTIONS FOR TASK VARIETY:
            - DO NOT generate tasks that are similar to previously completed tasks
            - Each new quest MUST be substantially different in nature, activity type, and domain
            - Analyze previous tasks to ensure your suggestion requires different skills or actions
            - If previous tasks involved physical activities, consider mental challenges
            - If previous tasks were creative, consider analytical tasks
            - Vary domains: health, education, creativity, social, professional
            - CRITICAL: ALL tasks MUST be completable within a single day (24 hours)
            - Do NOT suggest multi-day projects or tasks requiring extensive preparation
            
            IMPORTANT - FOCUS ON ACTIVE PROGRESSION:
            - Your role is to MONITOR progress and guide users toward ACTIVE improvement
            - DO NOT suggest passive tasks like "write in a notebook" or "visualize success"
            - Instead, suggest specific MEASURABLE actions that demonstrate real progress
            - Focus on behaviors that can be tracked, measured, and objectively completed
            - Prioritize skill-building activities with clear outcomes over reflective exercises
            
            EXAMPLE OF GOOD VARIETY:
            Previous task: "Go for a 20-minute walk outside"
            BAD follow-up: "Take a 15-minute jog around your neighborhood" (too similar)
            GOOD follow-up: "Complete 3 sets of 10 push-ups" (completely different activity)
            
            EXAMPLE OF GOOD TIMEFRAME:
            GOOD: "Practice a musical instrument for 30 minutes"
            BAD: "Read an entire book and write a report" (too time-consuming for one day)
            
            EXAMPLES OF ACTIVE VS PASSIVE TASKS:
            BAD (passive): "Write your thoughts in a journal about your career goals"
            GOOD (active): "Contact two companies in your target industry and request informational interviews"
            
            BAD (passive): "Visualize yourself successfully giving a presentation"
            GOOD (active): "Record yourself giving a 2-minute practice presentation and identify 3 areas to improve"
        """
    }
    
    private fun getEnhancedSystemPrompt(): String {
        return AppConfig.AIPrompts.GOAL_BASED_PROMPT + """
            
            IMPORTANT INSTRUCTIONS FOR TASK VARIETY:
            - DO NOT generate tasks that are similar to previously completed tasks
            - Each new quest MUST be substantially different in nature, activity type, and domain
            - Analyze previous tasks to ensure your suggestion requires different skills or actions
            - Maintain relevance to the stated goal, while ensuring variety in approach
            - If previous tasks were direct actions, consider planning or reflection tasks
            - If previous tasks were theoretical, suggest practical applications
            - CRITICAL: ALL tasks MUST be completable within a single day (24 hours)
            - Tasks should represent a meaningful step toward the goal but be realistic to complete in one day
            
            IMPORTANT - FOCUS ON ACTIVE PROGRESSION:
            - Your primary role is to MONITOR user progress and provide ACTIVE tasks that adjust to their needs
            - Analyze past performance to suggest the most impactful next actions
            - DO NOT suggest passive tasks like "journal about feelings" or "visualize success"
            - Instead, focus on concrete actions with clear completion criteria and measurable outcomes
            - Tasks should be specific, challenging, and directly contribute to skill development
            - Always prioritize active engagement over passive reflection

            POINTS SYSTEM:
            - Easy tasks: 50-200 points (15-30 minutes to complete)
            - Medium tasks: 201-500 points (30-60 minutes to complete)
            - Hard tasks: 501-1000 points (1-3 hours to complete, but still doable in a day)

            PARAMETERS EXPLANATION:
            - goalRelevance: A score from 0-100 indicating how relevant the quest is to the user's goal
            - goalProgress: A score from 0-100 indicating how much progress this quest contributes to the goal
            - category: A category for the quest that matches one of: PERSONAL, HEALTH, CAREER, EDUCATION, FINANCIAL, OTHER
            - difficulty: One of: easy, medium, hard

            EXAMPLES OF EFFECTIVE ACTIVE TASKS:
            For a goal related to learning programming:
            "Debug and fix three existing bugs in your current coding project | 300 | 90 | 10 | EDUCATION | medium"
            
            For a goal related to fitness:
            "Complete a HIIT workout with 10 exercises, 45 seconds each with 15 second breaks | 200 | 95 | 5 | HEALTH | easy"
            
            For a goal related to career advancement:
            "Update your resume with specific accomplishments from your current role and get feedback from a colleague | 400 | 85 | 15 | CAREER | medium"

            For a goal related to financial management:
            "Call three vendors to negotiate lower rates on monthly subscriptions or services | 350 | 90 | 20 | FINANCIAL | medium"

            RESPONSE FORMAT:
            Always respond with exactly one quest in the format: "quest description | points | goalRelevance | goalProgress | category | difficulty"
            Do not include any additional text or explanations.
        """.trimIndent()
    }
    
    private fun parseBasicQuestResponse(response: String): QuestResult {
        // Parse basic response (quest | points)
        val parts = response.split("|").map { it.trim() }
        if (parts.size < 2) {
            throw IllegalArgumentException("Invalid response format: $response")
        }
        
        val points = parts[1].toIntOrNull() 
            ?: throw NumberFormatException("Invalid points format: ${parts[1]}")
        
        return QuestResult(
            quest = parts[0],
            points = points
        )
    }
    
    private fun parseEnhancedQuestResponse(response: String): QuestResult {
        // Parse enhanced response (quest | points | goalRelevance | goalProgress | category | difficulty)
        val parts = response.split("|").map { it.trim() }
        if (parts.size < 2) {
            // Fallback to basic parsing if not enough parts
            return parseBasicQuestResponse(response)
        }
        
        val quest = parts[0]
        val points = parts.getOrNull(1)?.toIntOrNull() ?: 300
        val goalRelevance = parts.getOrNull(2)?.toIntOrNull() ?: 50
        val goalProgress = parts.getOrNull(3)?.toIntOrNull() ?: 10
        val category = parts.getOrNull(4) ?: ""
        val difficulty = parts.getOrNull(5)?.lowercase() ?: "medium"
        
        return QuestResult(
            quest = quest,
            points = points,
            goalRelevance = goalRelevance,
            goalProgress = goalProgress,
            category = category,
            difficultyLevel = difficulty
        )
    }
} 