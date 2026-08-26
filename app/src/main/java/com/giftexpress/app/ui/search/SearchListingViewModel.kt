package com.giftexpress.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.giftexpress.app.data.model.SearchListingApiResponse
import com.giftexpress.app.data.model.SearchListingProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchListingViewModel @Inject constructor(
    @Named("wizzy") private val okHttpClient: OkHttpClient
) : ViewModel() {

    companion object {
        private const val BASE_URL = "https://api.wizzy.ai/v1/"
        private const val STORE_ID = "62c607f016db11f0aee40a0c8095feae"
        private const val API_KEY = "SWpJcGpqNjBDQWxaaFZFeEhyY1QzZU9aQS9FMDZEcVRRMzdFRDJJa2hOb1VEdFcwRUF4Rk0vS3RxRGxFYjFFbmV1dkkxRkpSUGxhdko4VzJLMTJZc2c9PQ=="
    }

    private val gson = Gson()

    private val _products = MutableStateFlow<List<SearchListingProduct>>(emptyList())
    val products: StateFlow<List<SearchListingProduct>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private var currentPage = 1
    private var currentQuery = ""
    private var isLastPage = false

    fun searchProducts(query: String, resetPage: Boolean = true) {
        if (resetPage) {
            currentPage = 1
            currentQuery = query
            _products.value = emptyList()
            isLastPage = false
        }
        if (isLastPage && !resetPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val params = buildString {
                    append("q=${currentQuery.encodeUrl()}")
                    append("&page=$currentPage")
                    append("&productsCount=20")
                    append("&currency=USD")
                    append("&inStock=[true]")
                }

                val requestBody = params.toRequestBody("application/x-www-form-urlencoded".toMediaType())

                val request = Request.Builder()
                    .url("${BASE_URL}products/search")
                    .post(requestBody)
                    .addHeader("Accept", "application/json")
                    .addHeader("x-store-id", STORE_ID)
                    .addHeader("x-api-key", API_KEY)
                    .addHeader("x-request-id", java.util.UUID.randomUUID().toString())
                    .addHeader("x-wizzy-userid", "android-app")
                    .build()

                val responseBody = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute().use { it.body?.string() }
                }

                if (responseBody != null) {
                    val jsonObj = gson.fromJson(responseBody, com.google.gson.JsonObject::class.java)
                    val statusCode = jsonObj.get("statusCode")?.asInt ?: 200
                    val message = jsonObj.get("message")?.asString

                    if (statusCode != 200) {
                        _error.value = message ?: "Search request failed with status $statusCode"
                    } else {
                        val parsed = gson.fromJson(responseBody, SearchListingApiResponse::class.java)
                        val newItems = parsed.payload?.result ?: emptyList()
                        val totalPages = parsed.payload?.pages ?: 1
                        _totalPages.value = totalPages
                        if (resetPage) {
                            _products.value = newItems
                        } else {
                            _products.value = _products.value + newItems
                        }
                        isLastPage = currentPage >= totalPages
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (!isLastPage && !_isLoading.value) {
            searchProducts(currentQuery, resetPage = false)
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}
