package com.giftexpress.app.utils

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
 * Utility class to observe network connectivity changes
 */
class NetworkObserver(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val observe: Flow<Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private fun checkStatus() {
                launch { send(if (isNetworkAvailable()) Status.Available else Status.Unavailable) }
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                checkStatus()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                // Don't preemptively show no internet just because a network is degrading
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // A network was lost, but another (e.g. cellular) might still be active
                checkStatus()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                checkStatus()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Initial check
        val initialStatus = if (isNetworkAvailable()) Status.Available else Status.Unavailable
        launch { send(initialStatus) }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}
