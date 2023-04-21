package com.yapp.gallery.common.provider

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ConnectionProvider @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val transportTypes = arrayOf(
        NetworkCapabilities.TRANSPORT_WIFI,
        NetworkCapabilities.TRANSPORT_CELLULAR
    )

    private val connectionFlow = callbackFlow {
        val capabilities = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        val initialValue = (capabilities != null && transportTypes.any { capabilities.hasTransport(it) })
        send(initialValue)

        val callback = object : ConnectivityManager.NetworkCallback(){
            private var isAvailable = initialValue

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if(!isAvailable){
                    isAvailable = true
                    trySend(true)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Todo : onLost인 경우에 할지 말지 고민
//                if(isAvailable){
//                    isAvailable = false
//                    trySend(false)
//                }
            }
        }
        connectivityManager?.registerDefaultNetworkCallback(callback)

        awaitClose{
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }

    fun getConnectionFlow() : Flow<Boolean> = connectionFlow
}