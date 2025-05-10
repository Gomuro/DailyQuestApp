package com.example.dailyquestapp.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.example.dailyquestapp.ai.processor.AIProcessor
import com.example.dailyquestapp.ai.processor.OpenAIProcessor
import com.example.dailyquestapp.config.AppConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

/**
 * Dependency injection module for AI-related components
 */
val aiModule = module {
    // Provide OpenAI client with API key
    single { 
        OpenAI(
            token = AppConfig.getOpenAIApiKey(androidContext()),
            timeout = Timeout(socket = AppConfig.SOCKET_TIMEOUT_SECONDS.seconds)
        )
    }
    
    // Provide AIProcessor implementation
    single<AIProcessor> { OpenAIProcessor(get()) }
} 