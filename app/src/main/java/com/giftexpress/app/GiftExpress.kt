package com.giftexpress.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt dependency injection.
 * This initializes the Hilt dependency graph.
 */
@HiltAndroidApp
class GiftExpress : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any required libraries here
    }
}
