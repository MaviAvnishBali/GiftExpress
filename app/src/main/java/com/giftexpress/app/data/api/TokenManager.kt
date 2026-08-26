package com.giftexpress.app.data.api

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.giftexpress.app.data.model.RefreshTokenRequest
import com.giftexpress.app.data.model.TokenResponse
import com.giftexpress.app.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val keyAuthToken = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
    private val keyRefreshToken = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val keyTokenSavedTime = longPreferencesKey(Constants.KEY_TOKEN_SAVED_TIME)

    private val mutex = Mutex()
    private val gson = Gson()
    
    // Dedicated OkHttpClient with no interceptors to prevent deadlocks/infinite loops
    private val refreshClient = OkHttpClient.Builder().build()

    @Volatile
    private var accessToken: String? = null
    @Volatile
    private var refreshToken: String? = null
    
    private val TAG = "TokenManager"

    private fun log(message: String) {
        // Safe logging
        Log.d(TAG, message)
    }

    suspend fun getAccessToken(): String? {
        if (accessToken == null) {
            mutex.withLock {
                if (accessToken == null) {
                    val prefs = dataStore.data.first()
                    accessToken = prefs[keyAuthToken]
                    refreshToken = prefs[keyRefreshToken]
                }
            }
        }
        return accessToken
    }

    suspend fun getRefreshToken(): String? {
        if (refreshToken == null) {
            mutex.withLock {
                if (refreshToken == null) {
                    val prefs = dataStore.data.first()
                    accessToken = prefs[keyAuthToken]
                    refreshToken = prefs[keyRefreshToken]
                }
            }
        }
        return refreshToken
    }
    
    fun getAccessTokenSync(): String? = accessToken

    suspend fun saveTokens(newAccessToken: String, newRefreshToken: String?) {
        mutex.withLock {
            accessToken = newAccessToken
            if (newRefreshToken != null) {
                refreshToken = newRefreshToken
            }
            dataStore.edit { prefs ->
                prefs[keyAuthToken] = newAccessToken
                if (newRefreshToken != null) {
                    prefs[keyRefreshToken] = newRefreshToken
                }
                prefs[keyTokenSavedTime] = System.currentTimeMillis()
            }
        }
    }

    suspend fun clearTokens() {
        mutex.withLock {
            clearTokensInternal()
        }
    }

    /**
     * Refreshes the token synchronously inside OkHttp's authenticator.
     * Takes the token that failed so we can check if another thread already refreshed it.
     * 
     * Uses Mutex to ensure only one refresh request goes out even if multiple 401s occur.
     */
    suspend fun refreshAccessToken(failedToken: String?): String? {
        val reqId = UUID.randomUUID().toString().substring(0, 6)
        log("AUTH -> REFRESH [$reqId] -> started. Failed token hash: ${failedToken?.hashCode()}")
        
        mutex.withLock {
            // Check if another thread already refreshed
            val currentToken = accessToken
            if (currentToken != null && currentToken != failedToken) {
                log("AUTH -> REFRESH [$reqId] -> already refreshed by another thread. Current token hash: ${currentToken.hashCode()}")
                return currentToken
            }

            // Need to refresh
            val currentRefreshToken = refreshToken ?: run {
                val prefs = dataStore.data.first()
                prefs[keyRefreshToken]
            }
            
            if (currentRefreshToken.isNullOrBlank()) {
                log("AUTH -> REFRESH [$reqId] -> failed. No refresh token available.")
                clearTokensInternal()
                return null
            }

            return try {
                val body = gson.toJson(RefreshTokenRequest(currentRefreshToken))
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(Constants.BASE_URL + "giftexpress/auth/refresh")
                    .post(body)
                    .header("Accept", "application/json")
                    .build()

                // Execute synchronously on the dedicated refreshClient
                refreshClient.newCall(request).execute().use { resp ->
                    val payload = resp.body?.string()
                    if (!resp.isSuccessful || payload.isNullOrBlank()) {
                        log("AUTH -> REFRESH [$reqId] -> failed with code ${resp.code}. Clearing tokens.")
                        clearTokensInternal()
                        return null
                    }

                    val tokenResponse = gson.fromJson(payload, TokenResponse::class.java)
                    val newAccess = tokenResponse.accessToken
                    if (newAccess.isBlank()) {
                        log("AUTH -> REFRESH [$reqId] -> failed. Blank access token. Clearing tokens.")
                        clearTokensInternal()
                        return null
                    }

                    // Update memory & DataStore Atomically
                    accessToken = newAccess
                    if (tokenResponse.refreshToken != null) {
                        refreshToken = tokenResponse.refreshToken
                    }
                    dataStore.edit { prefs ->
                        prefs[keyAuthToken] = newAccess
                        if (tokenResponse.refreshToken != null) {
                            prefs[keyRefreshToken] = tokenResponse.refreshToken
                        }
                        prefs[keyTokenSavedTime] = System.currentTimeMillis()
                    }
                    
                    log("AUTH -> REFRESH [$reqId] -> success. New token hash: ${newAccess.hashCode()}")
                    newAccess
                }
            } catch (e: Exception) {
                log("AUTH -> REFRESH [$reqId] -> exception: ${e.message}")
                clearTokensInternal()
                null
            }
        }
    }
    
    // Non-locking version for internal use inside mutex.withLock
    private suspend fun clearTokensInternal() {
        accessToken = null
        refreshToken = null
        dataStore.edit { prefs ->
            prefs.remove(keyAuthToken)
            prefs.remove(keyRefreshToken)
            prefs.remove(keyTokenSavedTime)
        }
    }
}
