package com.giftexpress.app.data.api

import com.giftexpress.app.data.model.BaseResponse
import com.giftexpress.app.data.model.ChangePasswordRequest
import com.giftexpress.app.data.model.CreateCustomerRequest
import com.giftexpress.app.data.model.CustomerDetailsResponse
import com.giftexpress.app.data.model.CustomerTokenRequest
import com.giftexpress.app.data.model.ForgotPasswordRequest
import com.giftexpress.app.data.model.GoogleLoginRequest
import com.giftexpress.app.data.model.LoginRequest
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.Post
import com.giftexpress.app.data.model.SignupRequest
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.data.model.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit API interface
 * Defines all API endpoints for the application
 */
interface ApiService {

    /**
     * Hamburger Menu endpoint
     */
    @GET("giftexpress/menu/hyva-topmenu-mobile")
    suspend fun getHamburgerMenu(): Response<List<MenuItem>>

    /**
     * Home Page Sliders endpoint
     */
    @GET("giftexpress/sliders/")
    suspend fun getHomeSliders(): Response<List<SliderResponse>>

    /**
     * Login endpoint
     */
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<BaseResponse<UserData>>

    /**
     * Google Login endpoint
     */
    @POST("google-login")
    suspend fun googleLogin(@Body googleLoginRequest: GoogleLoginRequest): Response<BaseResponse<UserData>>

    /**
     * Signup endpoint
     */
    @POST("register")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<BaseResponse<UserData>>

    /**
     * Generate Customer Token
     */
    @POST("integration/customer/token")
    suspend fun generateCustomerToken(@Body request: CustomerTokenRequest): Response<String>

    /**
     * Get Customer Details
     */
    @GET("customers/me")
    suspend fun getCustomerDetails(@Header("Authorization") token: String): Response<CustomerDetailsResponse>

    /**
     * Change Password
     */
    @PUT("customers/me/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Boolean>

    /**
     * Create New Customer
     */
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): Response<CustomerDetailsResponse>

    /**
     * Forgot Password
     */
    @PUT("customers/password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Boolean>

}
