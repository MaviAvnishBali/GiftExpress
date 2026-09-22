package com.giftexpress.app.di

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Play Core AppUpdateManager instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @Singleton
    fun provideAppUpdateManager(
        @ApplicationContext context: Context
    ): AppUpdateManager {
        return AppUpdateManagerFactory.create(context)
    }
}
