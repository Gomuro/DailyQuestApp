package com.example.dailyquestapp.di

import android.content.Context
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.repository.OfflineFirstProgressRepository
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.repository.ProgressRepositoryImpl
import com.example.dailyquestapp.data.repository.TokenManager
import com.example.dailyquestapp.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    // Provide application coroutine scope
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    
    // Provide TokenManager
    single { TokenManager(androidContext()) }
    
    // Provide UserRepository
    single { UserRepository(get<ApiService>(), get<TokenManager>()) }
    
    // Provide DataStoreManager
    single { DataStoreManager(androidContext()) }
    
    // Provide ProgressRepository - use the offline-first implementation
    single<ProgressRepository> { 
        OfflineFirstProgressRepository(
            context = androidContext(),
            dataStoreManager = get(),
            apiService = get(),
            externalScope = get()
        ) 
    }
    
    // Also provide the remote-only implementation if needed
    single { ProgressRepositoryImpl(get<ApiService>()) }
}
