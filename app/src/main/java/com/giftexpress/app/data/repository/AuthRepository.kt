package com.giftexpress.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.api.TokenManager
import com.giftexpress.app.data.model.CustomerDetailsResponse
import com.giftexpress.app.data.model.ExtensionAttributes
import com.giftexpress.app.data.model.GoogleLoginRequest
import com.giftexpress.app.data.model.ChangePasswordRequest
import com.giftexpress.app.data.model.CreateCustomerRequest
import com.giftexpress.app.data.model.CustomerData
import com.giftexpress.app.data.model.CustomerTokenRequest
import com.giftexpress.app.data.model.ForgotPasswordRequest
import com.giftexpress.app.data.model.RefreshTokenRequest
import com.giftexpress.app.data.model.TokenResponse
import com.giftexpress.app.data.model.UpdateCustomerBody
import com.giftexpress.app.data.model.UpdateCustomerRequest
import com.giftexpress.app.data.model.User
import com.giftexpress.app.utils.Constants
import com.giftexpress.app.utils.NetworkResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repository handling authentication operations
 * Manages API calls and DataStore for session persistence
 */
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>,
    private val tokenManager: TokenManager,
    private val cartCountManager: CartCountManager
) {
    // Google Sign-In client for logout
    var googleSignInClient: GoogleSignInClient? = null
    var pendingCartSku: String? = null
    var pendingWishlistSku: String? = null

    // DataStore keys
    private val keyIsLoggedIn = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    private val keyUserName = stringPreferencesKey(Constants.KEY_USER_NAME)
    private val keyUserEmail = stringPreferencesKey(Constants.KEY_USER_EMAIL)
    private val keyUserId = stringPreferencesKey(Constants.KEY_USER_ID)
    private val keyAuthToken = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
    private val keyRefreshToken = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val keyTokenSavedTime = longPreferencesKey(Constants.KEY_TOKEN_SAVED_TIME)
    private val keyRememberMe = booleanPreferencesKey(Constants.KEY_REMEMBER_ME)
    private val keySavedEmail = stringPreferencesKey(Constants.KEY_SAVED_EMAIL)
    private val keySavedPassword = stringPreferencesKey(Constants.KEY_SAVED_PASSWORD)

    /**
     * Save "Remember Me" credentials
     */
    suspend fun saveRememberMeCredentials(email: String, password: String, enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[keyRememberMe] = enabled
            if (enabled) {
                preferences[keySavedEmail] = email
                preferences[keySavedPassword] = password
            } else {
                preferences[keySavedEmail] = ""
                preferences[keySavedPassword] = ""
            }
        }
    }

    /**
     * Get "Remember Me" status
     */
    fun isRememberMeEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[keyRememberMe] ?: false
        }
    }

    /**
     * Get saved credentials
     */
    fun getSavedCredentials(): Flow<Pair<String, String>> {
        return dataStore.data.map { preferences ->
            val email = preferences[keySavedEmail] ?: ""
            val password = preferences[keySavedPassword] ?: ""
            Pair(email, password)
        }
    }

    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): NetworkResult<User> {
        return try {
            val tokenResponse = apiService.generateCustomerToken(CustomerTokenRequest(email, password))

            if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                val token = tokenResponse.body()!!.accessToken
                val refreshToken = tokenResponse.body()!!.refreshToken
                // Sync to TokenManager so interceptor sends it for getCustomerDetails
                tokenManager.saveTokens(token, refreshToken)

                val detailsResponse = apiService.getCustomerDetails()
                if (detailsResponse.isSuccessful && detailsResponse.body() != null) {
                    val userData = detailsResponse.body()!!
                    val user = User(
                        id = userData.id.toString(),
                        name = "${userData.firstName} ${userData.lastName}",
                        email = userData.email,
                        token = token
                    )
                    saveUserSession(user)  // also saves to DataStore
                    NetworkResult.Success(user)
                } else {
                    tokenManager.clearTokens()
                    NetworkResult.Error("Failed to fetch customer details: ${detailsResponse.message()}")
                }
            } else {
                val errorMsg = tokenResponse.errorBody()?.string()
                    ?.let { parseErrorMessage(it) }
                    ?: "Invalid email or password"
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Login failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Login with Google
     * Step 1: Get token from social-login endpoint
     * Step 2: Use token to fetch customer details from "me" endpoint
     */
    suspend fun googleLogin(
        email: String,
        firstName: String,
        lastName: String,
        socialId: String,
        type: String = "google"
    ): NetworkResult<User> {
        return try {
            val tokenResponse = apiService.googleLogin(
                GoogleLoginRequest(email, firstName, lastName, socialId, type)
            )

            if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                val token = tokenResponse.body()!!.accessToken
                val refreshToken = tokenResponse.body()!!.refreshToken
                tokenManager.saveTokens(token, refreshToken)

                val detailsResponse = apiService.getCustomerDetails()
                if (detailsResponse.isSuccessful && detailsResponse.body() != null) {
                    val userData = detailsResponse.body()!!
                    val user = User(
                        id = userData.id.toString(),
                        name = "${userData.firstName} ${userData.lastName}",
                        email = userData.email,
                        token = token
                    )
                    saveUserSession(user)
                    NetworkResult.Success(user)
                } else {
                    tokenManager.clearTokens()
                    NetworkResult.Error("Failed to fetch customer details: ${detailsResponse.message()}")
                }
            } else {
                val errorMsg = tokenResponse.errorBody()?.string()
                    ?.let { parseErrorMessage(it) }
                    ?: "Google login failed"
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Google login failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Signup new user — matches iOS: createAccount then immediately callLoginAPI.
     * Returns the logged-in User with token after successful registration.
     */
    suspend fun signup(
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        password: String,
        isSubscribed: Boolean = false
    ): NetworkResult<User> {
        return try {
            val request = CreateCustomerRequest(
                customer = CustomerData(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    dob = dob,
                    extensionAttributes = ExtensionAttributes(isSubscribed = isSubscribed)
                ),
                password = password
            )
            val response = apiService.createCustomer(request)

            if (response.isSuccessful && response.body() != null) {
                // Account created — auto-login immediately (matches iOS callLoginAPI)
                login(email, password)
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?.let { parseErrorMessage(it) }
                    ?: "Signup failed: ${response.message()}"
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Signup error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Forgot Password
     */
    suspend fun forgotPassword(email: String): NetworkResult<Boolean> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: false)
            } else {
                NetworkResult.Error("Forgot password request failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Change Password
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResult<Boolean> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: false)
            else NetworkResult.Error("Change password failed")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Save user session to DataStore
     */
    private suspend fun saveUserSession(user: User) {
        dataStore.edit { preferences ->
            preferences[keyIsLoggedIn] = true
            preferences[keyUserName] = user.name
            preferences[keyUserEmail] = user.email
            preferences[keyUserId] = user.id
        }
    }

    /**
     * Check if user is logged in — also restores token into TokenProvider on app restart
     */
    suspend fun isLoggedIn(): Boolean {
        val prefs = dataStore.data.first()
        val loggedIn = prefs[keyIsLoggedIn] ?: false
        if (loggedIn) {
            // Restore tokens into memory so the interceptor + authenticator work after app restart
            tokenManager.getAccessToken()
        }
        return loggedIn
    }

    fun isLoggedInSync(): Boolean {
        return tokenManager.getAccessTokenSync() != null
    }

    /**
     * Get current user from DataStore
     */
    fun getCurrentUser(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val isLoggedIn = preferences[keyIsLoggedIn] ?: false
            if (isLoggedIn) {
                User(
                    id = preferences[keyUserId] ?: "",
                    name = preferences[keyUserName] ?: "",
                    email = preferences[keyUserEmail] ?: "",
                    token = preferences[keyAuthToken] ?: ""
                )
            } else {
                null
            }
        }
    }

    /**
     * Update user profile (PUT customers/me)
     */
    suspend fun updateProfile(
        id: Int,
        email: String,
        firstName: String,
        lastName: String,
        dob: String
    ): NetworkResult<CustomerDetailsResponse> {
        return try {
            val response = apiService.updateCustomer(
                UpdateCustomerRequest(UpdateCustomerBody(id = id, email = email, firstname = firstName, lastname = lastName, dob = dob))
            )
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Update failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Get full customer details from API
     */
    suspend fun getCustomerId(): Int? {
        return dataStore.data.map { it[keyUserId] }.first()?.toIntOrNull()
    }

    suspend fun getCustomerDetails(): NetworkResult<CustomerDetailsResponse> {
        return try {
            val response = apiService.getCustomerDetails()
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Refresh auth token
     */
    suspend fun refreshToken(): NetworkResult<String> {
        return try {
            val newToken = tokenManager.refreshAccessToken(null)
            if (newToken != null) {
                NetworkResult.Success(newToken)
            } else {
                NetworkResult.Error("Refresh failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Extract a human-readable message from a JSON error body.
     * Handles {"message":"..."} and {"error":"..."} formats.
     */
    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val element = com.google.gson.Gson().fromJson(errorBody, com.google.gson.JsonObject::class.java)
            element?.get("message")?.asString
                ?: element?.get("error")?.asString
                ?: element?.get("error_description")?.asString
                ?: "Login failed"
        } catch (e: Exception) {
            "Login failed"
        }
    }

    /**
     * Logout user - clear DataStore and sign out from Google
     */
    suspend fun logout() {
        tokenManager.clearTokens()
        try {
            googleSignInClient?.signOut()?.await()
        } catch (e: Exception) {
            // Continue with logout even if Google sign-out fails
        }
        dataStore.edit { preferences -> preferences.clear() }
        cartCountManager.reset()  // clear the header cart badge for the next session
    }
}
