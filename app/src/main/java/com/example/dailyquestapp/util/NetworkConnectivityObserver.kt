package com.example.dailyquestapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Observer class that monitors network connectivity changes
 */
class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectivityObserver.Status.Available) }
                }
                
                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(ConnectivityObserver.Status.Losing) }
                }
                
                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.Lost) }
                }
                
                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.Unavailable) }
                }
            }
            
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, callback)
            
            // Set initial value
            val currentStatus = getCurrentConnectivityStatus()
            send(currentStatus)
            
            // Clean up the callback when the flow collection is stopped
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
    
    /**
     * Check the current connectivity status
     */
    fun getCurrentConnectivityStatus(): ConnectivityObserver.Status {
        val network = connectivityManager.activeNetwork ?: return ConnectivityObserver.Status.Unavailable
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return ConnectivityObserver.Status.Unavailable
        
        return when {
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> 
                ConnectivityObserver.Status.Available
            else -> ConnectivityObserver.Status.Unavailable
        }
    }
} 