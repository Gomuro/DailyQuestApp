package com.example.dailyquestapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * A utility class for diagnosing network connectivity issues
 * Use this to test connections to the server
 */
class NetworkDiagnosticTool {
    private val TAG = "NetworkDiagnosticTool"
    
    companion object {
        // Test if device has internet connectivity
        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        
        // Test if server is reachable via TCP socket
        suspend fun isServerReachable(host: String, port: Int, timeout: Int = 3000): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), timeout)
                        true
                    }
                } catch (e: IOException) {
                    Log.e("NetworkDiagnosticTool", "Server not reachable: ${e.message}")
                    false
                }
            }
        }
        
        // Test if the API endpoint is responding
        suspend fun testApiEndpoint(url: String): ApiTestResult {
            return withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                try {
                    val response = client.newCall(request).execute()
                    ApiTestResult(
                        isSuccess = response.isSuccessful,
                        statusCode = response.code,
                        message = response.message,
                        body = response.body?.string()
                    )
                } catch (e: IOException) {
                    ApiTestResult(
                        isSuccess = false,
                        statusCode = -1,
                        message = "Error: ${e.message}",
                        body = null
                    )
                }
            }
        }
        
        // Full diagnostics test
        suspend fun runFullDiagnostics(context: Context): DiagnosticResult {
            val hasInternet = isInternetAvailable(context)
            val isEmulatorLocalhost = isServerReachable("10.0.2.2", 5000)
            val isDirectLocalhost = isServerReachable("127.0.0.1", 5000)
            
            // Only test API if basic connectivity is available
            val apiResult = if (hasInternet && (isEmulatorLocalhost || isDirectLocalhost)) {
                testApiEndpoint("http://10.0.2.2:5000/")
            } else {
                null
            }
            
            return DiagnosticResult(
                hasInternetConnectivity = hasInternet,
                isEmulatorLocalhostReachable = isEmulatorLocalhost,
                isDirectLocalhostReachable = isDirectLocalhost,
                apiTestResult = apiResult
            )
        }
    }
    
    data class ApiTestResult(
        val isSuccess: Boolean,
        val statusCode: Int,
        val message: String,
        val body: String?
    )
    
    data class DiagnosticResult(
        val hasInternetConnectivity: Boolean,
        val isEmulatorLocalhostReachable: Boolean,
        val isDirectLocalhostReachable: Boolean,
        val apiTestResult: ApiTestResult?
    ) {
        fun isFullyConnected(): Boolean {
            return hasInternetConnectivity && 
                   (isEmulatorLocalhostReachable || isDirectLocalhostReachable) &&
                   (apiTestResult?.isSuccess == true)
        }
        
        fun getDetailedReport(): String {
            return """
                Network Diagnostic Report:
                ------------------------
                Internet Connectivity: ${if (hasInternetConnectivity) "✅" else "❌"}
                Emulator localhost (10.0.2.2:5000): ${if (isEmulatorLocalhostReachable) "✅" else "❌"}
                Direct localhost (127.0.0.1:5000): ${if (isDirectLocalhostReachable) "✅" else "❌"}
                
                API Test: ${if (apiTestResult?.isSuccess == true) "✅" else "❌"}
                ${apiTestResult?.let { 
                    "Status Code: ${it.statusCode}\n" +
                    "Message: ${it.message}\n" +
                    "Response: ${it.body?.take(200) ?: "N/A"}"
                } ?: "API not tested due to connection issues"}
                
                Recommendation:
                ${getRecommendation()}
            """.trimIndent()
        }
        
        private fun getRecommendation(): String {
            return when {
                !hasInternetConnectivity -> 
                    "Check device internet connection. Make sure Wi-Fi or mobile data is enabled."
                    
                !isEmulatorLocalhostReachable && !isDirectLocalhostReachable ->
                    "Server appears to be unreachable. Verify server is running on port 5000 and " +
                    "check if firewall is blocking connections."
                    
                apiTestResult?.isSuccess == false ->
                    "Server is reachable but API returned error ${apiTestResult.statusCode}. " +
                    "Check server logs for details."
                    
                isFullyConnected() ->
                    "All connections are working correctly! If you're still experiencing issues, " +
                    "check specific API endpoints or authentication."
                    
                else ->
                    "Unknown issue. Check logcat for detailed error messages."
            }
        }
    }
} 