package com.giftexpress.app.data.api

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches Bearer token to every request.
 * Reads from TokenManager.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val path = chain.request().url.encodedPath
        // Auth endpoints must NOT carry a Bearer token
        val isAuthEndpoint = AUTH_ENDPOINTS.any { path.endsWith(it) }

        var token: String? = null
        if (!isAuthEndpoint) {
            token = runBlocking {
                tokenManager.getAccessToken()
            }
        }

        val request = if (!token.isNullOrBlank() && !isAuthEndpoint) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    companion object {
        private val AUTH_ENDPOINTS = listOf(
            "giftexpress/auth/login",
            "giftexpress/social-login",
            "giftexpress/auth/refresh"
        )
    }
}

