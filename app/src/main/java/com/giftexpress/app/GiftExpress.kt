package com.giftexpress.app

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt dependency injection.
 * This initializes the Hilt dependency graph.
 */
@HiltAndroidApp
class GiftExpress : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Stripe with flavor-specific key:
        // dev  → pk_test_51Q2aL7... (matches dev server's sk_test_ secret key)
        // prod → pk_live_duGJaQa7... (matches production server's sk_live_ secret key)
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
    }
}
