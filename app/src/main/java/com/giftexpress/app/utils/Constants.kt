package com.giftexpress.app.utils

/**
 * Application-wide constants
 */
object Constants {
    // API
    const val BASE_URL = "https://e03a53d91a.nxcli.io/rest/V1/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // DataStore Keys
    const val DATASTORE_NAME = "modern_app_preferences"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_ID = "user_id"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_SAVED_EMAIL = "saved_email"
    const val KEY_SAVED_PASSWORD = "saved_password"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    
    // Dummy responses (for demonstration)
    const val DUMMY_AUTH_TOKEN = "dummy_token_" 
}
