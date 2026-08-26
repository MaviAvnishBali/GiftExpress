package com.giftexpress.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.api.AuthInterceptor
import com.giftexpress.app.data.api.TokenManager
import com.giftexpress.app.data.repository.AddressRepository
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.data.repository.ContentRepository
import com.giftexpress.app.data.repository.HomeRepository
import com.giftexpress.app.data.repository.OrderRepository
import com.giftexpress.app.data.repository.RewardRepository
import com.giftexpress.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies
 * Includes DataStore, Retrofit, OkHttp, and Repository instances
 */

// Extension property to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides DataStore instance for preference storage
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }



    /**
     * Provides AuthInterceptor
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager
    ): AuthInterceptor =
        AuthInterceptor(tokenManager)

    /**
     * Provides TokenAuthenticator — transparently refreshes the access token
     * on any 401 (handles the 2-hour cart token expiry).
     */
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager
    ): com.giftexpress.app.data.api.TokenAuthenticator =
        com.giftexpress.app.data.api.TokenAuthenticator(tokenManager)

    /**
     * Provides OkHttpClient with auth + logging interceptors and a token authenticator
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: com.giftexpress.app.data.api.TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // Auth first, then logging
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator) // Auto-refresh on 401
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(com.giftexpress.app.data.model.ProductDetailsResponse::class.java, com.giftexpress.app.data.api.ProductDetailsDeserializer())
            .registerTypeAdapter(com.giftexpress.app.data.model.CategoryOfferAndBannerResponse::class.java, com.giftexpress.app.data.api.CategoryOfferAndBannerDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(com.giftexpress.app.BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides ApiService for network calls
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    /**
     * Provides AuthRepository
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        dataStore: DataStore<Preferences>,
        tokenManager: TokenManager,
        cartCountManager: com.giftexpress.app.data.repository.CartCountManager
    ): AuthRepository {
        return AuthRepository(apiService, dataStore, tokenManager, cartCountManager)
    }

    /**
     * Provides HomeRepository
     */
    @Provides
    @Singleton
    fun provideHomeRepository(apiService: ApiService): HomeRepository {
        return HomeRepository(apiService)
    }

    /**
     * Provides ProductRepository
     */
    @Provides
    @Singleton
    fun provideProductRepository(apiService: ApiService, authRepository: com.giftexpress.app.data.repository.AuthRepository): com.giftexpress.app.data.repository.ProductRepository {
        return com.giftexpress.app.data.repository.ProductRepository(apiService, authRepository)
    }

    /**
     * Provides WishlistRepository
     */
    @Provides
    @Singleton
    fun provideWishlistRepository(apiService: ApiService, authRepository: com.giftexpress.app.data.repository.AuthRepository): com.giftexpress.app.data.repository.WishlistRepository =
        com.giftexpress.app.data.repository.WishlistRepository(apiService, authRepository)

    /**
     * Provides CheckoutRepository
     */
    @Provides
    @Singleton
    fun provideCheckoutRepository(apiService: ApiService): com.giftexpress.app.data.repository.CheckoutRepository =
        com.giftexpress.app.data.repository.CheckoutRepository(apiService)

    /**
     * Provides OrderRepository
     */
    @Provides
    @Singleton
    fun provideOrderRepository(apiService: ApiService): OrderRepository = OrderRepository(apiService)

    /**
     * Provides RewardRepository
     */
    @Provides
    @Singleton
    fun provideRewardRepository(apiService: ApiService): RewardRepository = RewardRepository(apiService)

    /**
     * Provides ContentRepository
     */
    @Provides
    @Singleton
    fun provideContentRepository(apiService: ApiService): ContentRepository =
        ContentRepository(apiService)

    /**
     * Provides AddressRepository
     */
    @Provides
    @Singleton
    fun provideAddressRepository(apiService: ApiService): AddressRepository =
        AddressRepository(apiService)

    /**
     * Provides WizzyOkHttpClient (separate client for Wizzy AI APIs)
     */
    @Provides
    @Singleton
    @javax.inject.Named("wizzy")
    fun provideWizzyOkHttpClient(): okhttp3.OkHttpClient {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
