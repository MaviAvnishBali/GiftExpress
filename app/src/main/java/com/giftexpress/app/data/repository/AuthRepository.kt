package com.giftexpress.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.GoogleLoginRequest
import com.giftexpress.app.data.model.ChangePasswordRequest
import com.giftexpress.app.data.model.CreateCustomerRequest
import com.giftexpress.app.data.model.CustomerData
import com.giftexpress.app.data.model.CustomerTokenRequest
import com.giftexpress.app.data.model.ForgotPasswordRequest
import com.giftexpress.app.data.model.User
import com.giftexpress.app.utils.Constants
import com.giftexpress.app.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository handling authentication operations
 * Manages API calls and DataStore for session persistence
 */
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {

    // DataStore keys
    private val keyIsLoggedIn = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    private val keyUserName = stringPreferencesKey(Constants.KEY_USER_NAME)
    private val keyUserEmail = stringPreferencesKey(Constants.KEY_USER_EMAIL)
    private val keyUserId = stringPreferencesKey(Constants.KEY_USER_ID)
    private val keyAuthToken = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
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
            // Step 1: Generate Token
            val tokenResponse = apiService.generateCustomerToken(CustomerTokenRequest(email, password))
            
            if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                val token = tokenResponse.body()!!
                val bearerToken = "Bearer $token"
                
                // Step 2: Get Customer Details
                val detailsResponse = apiService.getCustomerDetails(bearerToken)
                
                if (detailsResponse.isSuccessful && detailsResponse.body() != null) {
                    val userData = detailsResponse.body()!!
                    
                    val user = User(
                        id = userData.id.toString(),
                        name = "${userData.firstName} ${userData.lastName}",
                        email = userData.email,
                        token = token // Store the raw token
                    )
                    
                    saveUserSession(user)
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error("Failed to fetch customer details: ${detailsResponse.message()}")
                }
            } else {
                NetworkResult.Error("Login failed: Invalid credentials or server error")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Login error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Login with Google
     */
    suspend fun googleLogin(idToken: String, email: String, firstName: String, lastName: String): NetworkResult<User> {
        return try {
            val response = apiService.googleLogin(GoogleLoginRequest(idToken, email, firstName, lastName))
            if (response.isSuccessful && response.body()?.data != null) {
                val userData = response.body()!!.data!!
                val user = User(
                    id = userData.userId,
                    name = "${userData.firstName} ${userData.lastName}",
                    email = userData.email,
                    token = userData.token
                )
                saveUserSession(user)
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error(response.body()?.message ?: "Google Login failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Google Login error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Signup new user
     */
    suspend fun signup(firstName: String, lastName: String, dob: String, email: String, password: String): NetworkResult<User> {
        return try {
            val request = CreateCustomerRequest(
                customer = CustomerData(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    dob = dob
                ),
                password = password
            )
            val response = apiService.createCustomer(request)
            
            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()!!
                
                // Note: Signup response doesn't return a token in this API structure.
                // Usually, you'd login immediately after signup to get a token.
                // For now, we'll return the user without a token or handle it as needed.
                val user = User(
                    id = userData.id.toString(),
                    name = "${userData.firstName} ${userData.lastName}",
                    email = userData.email,
                    token = "" // Token will be obtained via login
                )
                
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error("Signup failed: ${response.message()}")
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
            val user = getCurrentUser().first()
            if (user == null || user.token.isBlank()) {
                return NetworkResult.Error("User not logged in")
            }
            
            val response = apiService.changePassword(
                "Bearer ${user.token}",
                ChangePasswordRequest(currentPassword, newPassword)
            )
            
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: false)
            } else {
                NetworkResult.Error("Change password failed")
            }
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
            preferences[keyAuthToken] = user.token
        }
    }

    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[keyIsLoggedIn] ?: false
        }.first()
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
     * Logout user - clear DataStore
     */
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
