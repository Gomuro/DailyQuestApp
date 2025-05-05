package com.example.dailyquestapp.di

import android.content.Context
import com.example.dailyquestapp.data.local.DataStoreManager
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.repository.ProgressRepository
import com.example.dailyquestapp.data.repository.TokenManager
import com.example.dailyquestapp.data.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    // Provide TokenManager
    single { TokenManager(androidContext()) }
    
    // Provide UserRepository
    single { UserRepository(get<ApiService>(), get<TokenManager>()) }
    
    // Provide ProgressRepository
    single { ProgressRepository(get<ApiService>()) }
    
    // Provide DataStoreManager
    single { DataStoreManager(androidContext()) }
}
