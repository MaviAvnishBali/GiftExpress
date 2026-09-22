package com.giftexpress.app.data.api

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.giftexpress.app.BuildConfig
import com.giftexpress.app.data.model.RefreshTokenRequest
import com.giftexpress.app.data.model.TokenResponse
import com.giftexpress.app.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val keyAuthToken = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
    private val keyRefreshToken = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val keyTokenSavedTime = longPreferencesKey(Constants.KEY_TOKEN_SAVED_TIME)
    private val keyIsLoggedIn = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)

    private val mutex = Mutex()
    private val gson = Gson()
    
    // Dedicated OkHttpClient with timeouts and no interceptors to prevent deadlocks/infinite loops
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var accessToken: String? = null
    @Volatile
    private var refreshToken: String? = null
    @Volatile
    private var tokenSavedTime: Long = 0L
    
    private val TAG = "TokenManager"

    init {
        // Pre-populate tokens asynchronously as soon as TokenManager is created
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = dataStore.data.first()
                accessToken = prefs[keyAuthToken]
                refreshToken = prefs[keyRefreshToken]
                tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
            } catch (e: Exception) {
                Log.w(TAG, "Failed to preload tokens from DataStore", e)
            }
        }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    suspend fun getAccessToken(): String? {
        if (accessToken == null) {
            mutex.withLock {
                if (accessToken == null) {
                    val prefs = dataStore.data.first()
                    accessToken = prefs[keyAuthToken]
                    refreshToken = prefs[keyRefreshToken]
                    tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
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
                    tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
                }
            }
        }
        return refreshToken
    }

    suspend fun getTokenSavedTime(): Long {
        if (tokenSavedTime == 0L) {
            mutex.withLock {
                if (tokenSavedTime == 0L) {
                    val prefs = dataStore.data.first()
                    tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
                }
            }
        }
        return tokenSavedTime
    }

    /**
     * Synchronous access token retrieval with DataStore fallback.
     * Prevents process death from returning null and falsely marking user as logged out.
     */
    fun getAccessTokenSync(): String? {
        if (accessToken == null) {
            try {
                runBlocking {
                    val prefs = dataStore.data.first()
                    accessToken = prefs[keyAuthToken]
                    refreshToken = prefs[keyRefreshToken]
                    tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read access token synchronously", e)
            }
        }
        return accessToken
    }

    fun getTokenSavedTimeSync(): Long {
        if (tokenSavedTime == 0L) {
            try {
                runBlocking {
                    val prefs = dataStore.data.first()
                    tokenSavedTime = prefs[keyTokenSavedTime] ?: 0L
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read tokenSavedTime synchronously", e)
            }
        }
        return tokenSavedTime
    }

    /**
     * Checks whether the current session has expired (>= 30 minutes since saved/refreshed).
     * Matches iOS AppPreference.isSessionExpired():
     *   let thirtyMinutes: Int = 30 * 60 // 1800 seconds
     *   return elapsedTime >= thirtyMinutes
     */
    suspend fun isSessionExpired(): Boolean {
        val savedTime = getTokenSavedTime()
        if (savedTime <= 0L) return true
        val elapsedTime = System.currentTimeMillis() - savedTime
        val thirtyMinutes = 30 * 60 * 1000L // 30 minutes in milliseconds
        return elapsedTime >= thirtyMinutes
    }

    fun isSessionExpiredSync(): Boolean {
        val savedTime = getTokenSavedTimeSync()
        if (savedTime <= 0L) return true
        val elapsedTime = System.currentTimeMillis() - savedTime
        val thirtyMinutes = 30 * 60 * 1000L
        return elapsedTime >= thirtyMinutes
    }

    suspend fun saveTokens(newAccessToken: String, newRefreshToken: String?) {
        mutex.withLock {
            val now = System.currentTimeMillis()
            accessToken = newAccessToken
            if (newRefreshToken != null) {
                refreshToken = newRefreshToken
            }
            tokenSavedTime = now
            dataStore.edit { prefs ->
                prefs[keyAuthToken] = newAccessToken
                if (newRefreshToken != null) {
                    prefs[keyRefreshToken] = newRefreshToken
                }
                prefs[keyTokenSavedTime] = now
                prefs[keyIsLoggedIn] = true
            }
        }
    }

    suspend fun clearTokens() {
        mutex.withLock {
            clearTokensInternal()
        }
    }

    /**
     * Refreshes the token synchronously inside OkHttp's authenticator or via repository.
     * Takes the token that failed so we can check if another thread already refreshed it.
     * 
     * Uses Mutex to ensure only one refresh request goes out even if multiple 401s occur.
     * Retries up to 3 times matching iOS AppPreference.refreshToken(retryCount: 0).
     * Never clears tokens on network or 5xx server errors.
     */
    suspend fun refreshAccessToken(failedToken: String?): String? {
        val reqId = UUID.randomUUID().toString().substring(0, 6)
        log("AUTH -> REFRESH [$reqId] -> started. Failed token hash: ${failedToken?.hashCode()}")
        
        mutex.withLock {
            // Check if another thread already refreshed while waiting for lock
            val currentToken = accessToken
            if (currentToken != null && failedToken != null && currentToken != failedToken) {
                log("AUTH -> REFRESH [$reqId] -> already refreshed by another thread. Current token hash: ${currentToken.hashCode()}")
                return currentToken
            }

            // If this was a proactive refresh (failedToken == null), but token was refreshed < 30 seconds ago
            if (failedToken == null && currentToken != null && (System.currentTimeMillis() - tokenSavedTime < 30_000L)) {
                log("AUTH -> REFRESH [$reqId] -> token was recently refreshed. Skipping.")
                return currentToken
            }

            // Need to refresh - retrieve refresh token
            val currentRefreshToken = refreshToken ?: run {
                val prefs = dataStore.data.first()
                prefs[keyRefreshToken]
            }
            
            if (currentRefreshToken.isNullOrBlank()) {
                log("AUTH -> REFRESH [$reqId] -> failed. No refresh token available.")
                clearTokensInternal()
                return null
            }

            val refreshUrl = BuildConfig.BASE_URL + "giftexpress/auth/refresh"
            val body = gson.toJson(RefreshTokenRequest(currentRefreshToken))
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(refreshUrl)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .build()

            var attempts = 0
            val maxAttempts = 3
            var successToken: String? = null

            while (attempts < maxAttempts && successToken == null) {
                attempts++
                try {
                    refreshClient.newCall(request).execute().use { resp ->
                        val payload = resp.body?.string()

                        // Explicit 401/403 means the refresh token itself is truly expired/invalid
                        if (resp.code == 401 || resp.code == 403) {
                            log("AUTH -> REFRESH [$reqId] -> refresh token rejected (${resp.code}). Invalidating session.")
                            clearTokensInternal()
                            return null
                        }

                        if (resp.isSuccessful && !payload.isNullOrBlank()) {
                            val tokenResponse = gson.fromJson(payload, TokenResponse::class.java)
                            val newAccess = tokenResponse.accessToken
                            if (newAccess.isNotBlank()) {
                                val now = System.currentTimeMillis()
                                accessToken = newAccess
                                if (!tokenResponse.refreshToken.isNullOrBlank()) {
                                    refreshToken = tokenResponse.refreshToken
                                }
                                tokenSavedTime = now

                                dataStore.edit { prefs ->
                                    prefs[keyAuthToken] = newAccess
                                    if (!tokenResponse.refreshToken.isNullOrBlank()) {
                                        prefs[keyRefreshToken] = tokenResponse.refreshToken
                                    }
                                    prefs[keyTokenSavedTime] = now
                                    prefs[keyIsLoggedIn] = true
                                }
                                
                                log("AUTH -> REFRESH [$reqId] -> success on attempt $attempts. New token hash: ${newAccess.hashCode()}")
                                successToken = newAccess
                            } else {
                                log("AUTH -> REFRESH [$reqId] -> blank access token received in payload. Attempt $attempts/$maxAttempts.")
                            }
                        } else {
                            log("AUTH -> REFRESH [$reqId] -> server responded with code ${resp.code}. Attempt $attempts/$maxAttempts.")
                            if (attempts < maxAttempts) {
                                kotlinx.coroutines.delay(1500L * attempts)
                            }
                        }
                    }
                } catch (e: Exception) {
                    log("AUTH -> REFRESH [$reqId] -> network exception: ${e.message}. Attempt $attempts/$maxAttempts.")
                    if (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(1500L * attempts)
                    }
                }
            }

            if (successToken == null) {
                log("AUTH -> REFRESH [$reqId] -> failed after $maxAttempts attempts. Preserving existing tokens.")
            }
            return successToken
        }
    }
    
    // Non-locking version for internal use inside mutex.withLock
    private suspend fun clearTokensInternal() {
        accessToken = null
        refreshToken = null
        tokenSavedTime = 0L
        dataStore.edit { prefs ->
            prefs.remove(keyAuthToken)
            prefs.remove(keyRefreshToken)
            prefs.remove(keyTokenSavedTime)
            prefs[keyIsLoggedIn] = false
        }
    }
}
