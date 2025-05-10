package com.example.dailyquestapp.config

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log

/**
 * Configuration class to provide app settings and API keys
 */
object AppConfig {
    private const val TAG = "AppConfig"
    
    // AI Model configuration
    const val DEFAULT_MODEL = "gpt-3.5-turbo"
    const val SOCKET_TIMEOUT_SECONDS = 60L
    const val MAX_TOKENS = 300  // Maximum tokens for response
    
    // Get OpenAI API key from gradle.properties via BuildConfig
    fun getOpenAIApiKey(context: Context): String {
        return try {
            // First try to get it from BuildConfig
            val buildConfigClass = Class.forName("com.example.dailyquestapp.BuildConfig")
            val field = buildConfigClass.getField("OPENAI_API_KEY")
            field.get(null) as String
        } catch (e: Exception) {
            Log.w(TAG, "Could not get API key from BuildConfig, trying metadata: ${e.message}")
            
            // Fallback to AndroidManifest metadata
            try {
                val ai = context.packageManager.getApplicationInfo(
                    context.packageName, 
                    PackageManager.GET_META_DATA
                )
                val bundle: Bundle = ai.metaData
                bundle.getString("openai_api_key") ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get API key: ${e.message}")
                ""
            }
        }
    }
    
    // Get current model name based on settings
    fun getAIModel(useAdvancedModel: Boolean = false): String {
        return DEFAULT_MODEL
    }
    
    // AI system message templates
    object AIPrompts {
        // Base prompt for quest generation
        const val QUEST_BASE_PROMPT = """
            You are an advanced daily quest generator that creates concise tasks to help users achieve their goals.
            
            CORE PRINCIPLES:
            1. Conciseness: Tasks should be clear and to-the-point without unnecessary words
            2. Personalization: Each quest should be tailored to the user's interests, skills, and goals
            3. Balance: Tasks should be challenging but achievable in a single day
            4. Impact: Each quest should contribute to growth
            
            QUEST REQUIREMENTS:
            - Keep task descriptions concise and direct
            - Use clear, actionable language
            - Be specific but avoid unnecessary details
            
            RESPONSE FORMAT:
            Always respond with exactly one quest in the format: "quest description | points"
            Do not include any additional text or explanations.
        """
        
        // Enhanced prompt for goal-based quest generation
        const val GOAL_BASED_PROMPT = """
            You are an advanced daily quest generator that creates concise tasks that help users make progress toward their larger goals.
            
            CORE PRINCIPLES:
            1. Conciseness: Keep tasks clear and to-the-point
            2. Personalization: Each quest should directly advance the user's stated goal
            3. Balance: Tasks should be challenging but achievable in a single day
            4. Impact: Each task should contribute to measurable progress
            
            QUEST REQUIREMENTS:
            - Format: "quest description | points | goalRelevance | goalProgress | category | difficulty"
            - Tasks should be concise and direct
            - Use simple, actionable language
            - Focus on clear actions
        """
    }
} 