package com.giftexpress.app.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator that transparently recovers from an expired access token.
 * Uses TokenManager to coordinate refresh across concurrent requests.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Never try to refresh the refresh call itself.
        if (response.request.url.encodedPath.endsWith("giftexpress/auth/refresh")) return null

        // Give up after a couple of attempts to avoid an infinite 401 loop.
        if (responseCount(response) >= 2) return null

        val failedToken = response.request
            .header("Authorization")?.removePrefix("Bearer ")?.trim()

        val newToken = runBlocking {
            tokenManager.refreshAccessToken(failedToken)
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    /** How many times this request has already been retried via the authenticator. */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
