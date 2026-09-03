package com.giftexpress.app.data.api

import com.giftexpress.app.data.model.*
import com.giftexpress.app.data.model.StripePaymentIntentRequest
import com.giftexpress.app.data.model.StripePlaceOrderRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface.
 * Authorization header is attached automatically by AuthInterceptor — no manual token needed.
 */
interface ApiService {

    // ─── Public (no auth required) ───────────────────────────────────────────

    @GET("giftexpress/menu/hyva-topmenu-mobile")
    suspend fun getHamburgerMenu(): Response<List<MenuItem>>

    @GET("giftexpress/sliders/")
    suspend fun getHomeSliders(): Response<List<SliderResponse>>

    @GET("giftexpress/combined-menu/app_menu")
    suspend fun getCategoriesMainScreen(): Response<CategoriesMainScreenResponse>

    @GET("giftexpress/product-details/{sku}")
    suspend fun getProductDetails(@Path("sku") sku: String): Response<ProductDetailsResponse>

    @GET("giftexpress/categoryofferandBanner/{categoryId}")
    suspend fun getCategoryOfferAndBanner(@Path("categoryId") categoryId: Int): Response<CategoryOfferAndBannerResponse>

    @GET("giftexpress/all-products")
    suspend fun getCategoryProducts(
        @Query("category_id") categoryId: Int,
        @Query("pageSize") pageSize: Int,
        @Query("currentPage") currentPage: Int,
        @Query("manufacturer") manufacturer: Int? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("special_flag") specialFlag: Int? = null,
        @QueryMap filters: Map<String, String> = emptyMap()
    ): Response<CategoryProductsResponse>

    @GET("giftexpress/all-products")
    suspend fun getSpecialProducts(
        @Query("special_flag") specialFlag: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("currentPage") currentPage: Int = 1,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("manufacturer") manufacturer: Int? = null,
        @QueryMap filters: Map<String, String> = emptyMap()
    ): Response<CategoryProductsResponse>

    @GET("giftexpress/brands")
    suspend fun getBrands(): Response<List<BrandResponse>>

    @GET("giftexpress/brand/{brandId}/products")
    suspend fun getBrandProducts(
        @Path("brandId") brandId: Int,
        @Query("pageSize") pageSize: Int,
        @Query("currentPage") currentPage: Int,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("manufacturer") manufacturer: Int? = null,
        @QueryMap filters: Map<String, String> = emptyMap()
    ): Response<ProductListResponse>

    @GET("giftexpress/reviews/{sku}")
    suspend fun getReviews(@Path("sku") sku: String): Response<List<ProductReview>>

    @POST("giftexpress/reviews/")
    suspend fun submitReview(@Body request: SubmitReviewRequest): Response<Boolean>

    @GET("giftexpress/cms-pages")
    suspend fun getCmsPages(): Response<List<CmsPage>>

    @GET("giftexpress/contact-us")
    suspend fun getContactUs(): Response<CmsPage>

    @POST("giftexpress/enquiry")
    suspend fun submitEnquiry(@Body request: EnquiryRequest): Response<Any>

    // ─── Auth ─────────────────────────────────────────────────────────────────

    // ─── Address Book — matches iOS AddressViewModel endpoints ───────────────

    // Matches iOS: GET giftexpress/customer/addresses
    @GET("giftexpress/customer/addresses")
    suspend fun getAddresses(): Response<List<CustomerAddressModel>>

    // Matches iOS: POST giftexpress/customer/address
    @POST("giftexpress/customer/address")
    suspend fun addAddress(@Body request: SaveAddressRequest): Response<CustomerAddressModel>

    // Matches iOS: PUT giftexpress/customer/address/{id}
    @PUT("giftexpress/customer/address/{id}")
    suspend fun updateAddress(
        @Path("id") id: Int,
        @Body request: SaveAddressRequest
    ): Response<CustomerAddressModel>

    // Matches iOS: DELETE giftexpress/customer/address/{id}
    @DELETE("giftexpress/customer/address/{id}")
    suspend fun deleteAddress(@Path("id") id: Int): Response<Boolean>

    // Matches iOS: GET directory/countries
    @GET("directory/countries")
    suspend fun getCountries(): Response<List<CountryModel>>

    // ─── Stripe Payment ───────────────────────────────────────────────────────

    // Matches iOS: POST giftexpress/stripe/payment-intent-v2 with {quoteId, customerId}
    @POST("giftexpress/stripe/payment-intent-v2")
    suspend fun getStripePaymentIntent(@Body request: StripePaymentIntentRequest): Response<PaymentIntentResponse>

    // Matches iOS: POST giftexpress/stripe/place-order with {payment_intent_id}
    @POST("giftexpress/stripe/place-order")
    suspend fun stripePlaceOrder(@Body request: StripePlaceOrderRequest): Response<PlaceOrderResponse>

    // ─── PayPal Payment ───────────────────────────────────────────────────────
    // NOTE: backend endpoints pending — mirror the Stripe flow:
    //   create-order → creates a PayPal Orders v2 order for the quote, returns paypal_order_id
    //   place-order  → captures the approved PayPal order and places the Magento order

    @POST("giftexpress/paypal/create-order")
    suspend fun createPayPalOrder(@Body request: PayPalCreateOrderRequest): Response<PayPalCreateOrderResponse>

    @POST("giftexpress/paypal/capture-order")
    suspend fun payPalPlaceOrder(@Body request: PayPalPlaceOrderRequest): Response<PlaceOrderResponse>

    // ─── Amazon Pay (web-redirect / Checkout v2) ────────────────────────────────
    // NOTE: backend endpoints pending — hosted-page flow, mirrors PayPal:
    //   create-checkout → opens an Amazon Pay session, returns redirect_url + reference_id
    //   place-order     → completes the approved session and places the Magento order
    @POST("giftexpress/amazonpay/create-checkout")
    suspend fun createAmazonPayCheckout(@Body request: WebCheckoutSessionRequest): Response<WebCheckoutSessionResponse>

    @POST("giftexpress/amazonpay/place-order")
    suspend fun amazonPayPlaceOrder(@Body request: WebCheckoutPlaceOrderRequest): Response<PlaceOrderResponse>

    // ─── Afterpay (web-redirect) ────────────────────────────────────────────────
    // NOTE: backend endpoints pending — same hosted-page contract as Amazon Pay.
    @POST("giftexpress/afterpay/create-checkout")
    suspend fun createAfterpayCheckout(@Body request: WebCheckoutSessionRequest): Response<WebCheckoutSessionResponse>

    @POST("giftexpress/afterpay/place-order")
    suspend fun afterpayPlaceOrder(@Body request: WebCheckoutPlaceOrderRequest): Response<PlaceOrderResponse>

    // Custom login endpoint — matches iOS getLoginURL() = "rest/V1/giftexpress/auth/login"
    // Returns {"access_token":"...","refresh_token":"...","customer_id":...}
    @POST("giftexpress/auth/login")
    suspend fun generateCustomerToken(@Body request: CustomerTokenRequest): Response<TokenResponse>

    @POST("giftexpress/social-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<TokenResponse>

    @POST("register")
    suspend fun signup(@Body request: SignupRequest): Response<BaseResponse<UserData>>

    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): Response<CustomerDetailsResponse>

    @PUT("customers/password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Boolean>

    // Matches iOS: POST rest/V1/giftexpress/auth/refresh with {"refreshToken":"..."}
    @POST("giftexpress/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    // ─── Authenticated — token attached by interceptor ────────────────────────

    @GET("customers/me")
    suspend fun getCustomerDetails(): Response<CustomerDetailsResponse>

    @PUT("customers/me")
    suspend fun updateCustomer(@Body request: UpdateCustomerRequest): Response<CustomerDetailsResponse>

    @PUT("customers/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Boolean>

    // Matches iOS: POST rest//V1/carts/mine → returns Int (quoteId)
    @POST("carts/mine")
    suspend fun createCart(): Response<Int>

    @GET("carts/mine")
    suspend fun getCart(): Response<com.giftexpress.app.data.model.CartResponse>

    // Matches iOS fetchCartList: GET rest/V1/carts/mine/items → [CartProduct]
    @GET("carts/mine/items")
    suspend fun getCartItems(): Response<List<CartItemDetail>>

    // Matches iOS CartManager.getCartCount: GET rest/V1/carts/mine/totals → CartTotalsResponse
    @GET("carts/mine/totals")
    suspend fun getCartTotals(): Response<OrderTotals>

    @POST("carts/mine/items")
    suspend fun addItemToCart(@Body request: AddCartItemRequest): Response<CartItemDetail>

    @PUT("carts/mine/items/{itemId}")
    suspend fun updateCartItem(
        @Path("itemId") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<CartItemDetail>

    @DELETE("carts/mine/items/{itemId}")
    suspend fun removeCartItem(@Path("itemId") itemId: Int): Response<Boolean>

    @GET("wishlist")
    suspend fun getWishlist(): Response<WishListResponse>

    @PUT("wishlist/{sku}")
    suspend fun addToWishlist(@Path("sku") sku: String): Response<Any>

    @DELETE("wishlist/{itemId}")
    suspend fun removeFromWishlist(@Path("itemId") itemId: Int): Response<Any>

    @POST("carts/mine/estimate-shipping-methods")
    suspend fun estimateShippingMethods(@Body request: EstimateShippingRequest): Response<List<ShippingMethod>>

    @POST("carts/mine/shipping-information")
    suspend fun saveShippingInformation(@Body request: ShippingInformationRequest): Response<PaymentMethodsResponse>

    @PUT("carts/mine/coupons/{couponCode}")
    suspend fun applyCoupon(@Path("couponCode") couponCode: String): Response<Boolean>

    @DELETE("carts/mine/coupons")
    suspend fun removeCoupon(): Response<Boolean>

    @PUT("carts/mine/points/{points}")
    suspend fun applyRewardPoints(@Path("points") points: Int): Response<Any>

    @DELETE("carts/mine/points/delete")
    suspend fun removeRewardPoints(): Response<Any>

    @POST("amasty_extrafee/carts/mine/totals-information")
    suspend fun applyShippingProtection(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<OrderTotals>

    @POST("carts/mine/payment-information")
    suspend fun placeOrder(@Body request: PlaceOrderRequest): Response<PlaceOrderResponse>

    @GET("giftexpress/customer/orders")
     suspend fun getOrders(): Response<List<OrderApiResponse>>

    @GET("giftexpress/customer/order/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Int): Response<OrderApiResponse>

    @GET("giftexpress/customer/reward-points")
    suspend fun getRewardPoints(): Response<WalletSummary>

    @POST("giftexpress/customer/reward-points/subscription")
    suspend fun updateRewardSubscription(@Body request: RewardSubscriptionRequest): Response<Boolean>

    @GET("rewards/mine/history")
    suspend fun getRewardHistory(): Response<List<RewardHistoryItem>>
}
