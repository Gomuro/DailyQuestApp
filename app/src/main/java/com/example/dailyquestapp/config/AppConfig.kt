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
} 