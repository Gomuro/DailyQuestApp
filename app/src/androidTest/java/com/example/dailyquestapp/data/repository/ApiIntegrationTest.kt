package com.example.dailyquestapp.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dailyquestapp.data.remote.ApiService
import com.example.dailyquestapp.data.remote.dto.LoginRequest
import com.example.dailyquestapp.data.remote.dto.RegisterRequest
import com.example.dailyquestapp.di.NetworkModule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        tokenManager = TokenManager(context)
        
        val okHttpClient = NetworkModule.provideOkHttpClient(
            NetworkModule.provideLoggingInterceptor(),
            NetworkModule.provideAuthInterceptor(tokenManager)
        )
        
        val retrofit = NetworkModule.provideRetrofit(
            okHttpClient,
            mockWebServer.url("/api/").toString()
        )
        
        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRegisterUser() = runBlocking {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_CREATED)
            .setBody("""
                {
                    "_id": "test123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "totalPoints": 0,
                    "currentStreak": 0,
                    "token": "fake.jwt.token"
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = apiService.registerUser(
            RegisterRequest("testuser", "test@example.com", "password123")
        )

        // Assert
        val request = mockWebServer.takeRequest()
        assert(request.path == "/api/auth/register")
        assert(response.token == "fake.jwt.token")
    }

    @Test
    fun testLoginUser() = runBlocking {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                    "_id": "test123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "totalPoints": 100,
                    "currentStreak": 5,
                    "token": "fake.jwt.token"
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = apiService.loginUser(
            LoginRequest("test@example.com", "password123")
        )

        // Assert
        val request = mockWebServer.takeRequest()
        assert(request.path == "/api/auth/login")
        assert(response.token == "fake.jwt.token")
        assert(response.totalPoints == 100)
    }
} 