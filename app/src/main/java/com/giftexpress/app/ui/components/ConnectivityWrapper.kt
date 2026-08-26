package com.giftexpress.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.giftexpress.app.utils.NetworkObserver

@Composable
fun ConnectivityWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val networkObserver = remember { NetworkObserver(context) }
    val networkStatus by networkObserver.observe.collectAsState(initial = NetworkObserver.Status.Available)

    if (networkStatus == NetworkObserver.Status.Unavailable || networkStatus == NetworkObserver.Status.Lost) {
        NoInternetScreen(onRetry = {
            // The observer will automatically update when connection is restored
        })
    } else {
        content()
    }
}
