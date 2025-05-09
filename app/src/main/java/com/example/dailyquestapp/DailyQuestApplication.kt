package com.example.dailyquestapp

import android.app.Application
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
import org.koin.core.context.startKoin
import org.koin.dsl.module

class DailyQuestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@DailyQuestApplication)
            modules(appModule, repositoryModule, viewModelModule, aiModule)
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
