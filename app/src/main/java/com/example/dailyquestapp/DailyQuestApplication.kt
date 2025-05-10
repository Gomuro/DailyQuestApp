package com.example.dailyquestapp

import android.app.Application
import android.util.Log
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.repository.TokenManager
import com.example.dailyquestapp.di.NetworkModule
import com.example.dailyquestapp.di.aiModule
import com.example.dailyquestapp.di.repositoryModule
import com.example.dailyquestapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

/**
 * Application class for DailyQuestApp that initializes Koin dependency injection
 */
class DailyQuestApplication : Application() {
    private val TAG = "DailyQuestApplication"
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Initializing application...")
        
        // Initialize Koin DI framework if not already started
        if (GlobalContext.getOrNull() == null) {
            Log.d(TAG, "Starting Koin...")
            startKoin {
                // Use AndroidLogger with INFO level for better debugging
                androidLogger(Level.INFO)
                // Provide Android context
                androidContext(this@DailyQuestApplication)
                // Load all modules
                modules(appModule, repositoryModule, viewModelModule, aiModule)
            }
            Log.d(TAG, "Koin initialization completed")
        } else {
            Log.d(TAG, "Koin already initialized")
        }
    }
    
    // Define base Koin module for network-related DI
    private val appModule = module {
        // Network related instances
        single { NetworkModule.provideLoggingInterceptor() }
        single { NetworkModule.provideAuthInterceptor(get()) }
        single { NetworkModule.provideOkHttpClient(get(), get()) }
        single { NetworkModule.provideBaseUrl() }
        single { NetworkModule.provideRetrofit(get(), get()) }
        single { NetworkModule.provideApiService(get()) }
    }
}
