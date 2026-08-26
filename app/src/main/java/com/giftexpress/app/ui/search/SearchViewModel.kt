package com.giftexpress.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.giftexpress.app.data.model.SearchApiResponse
import com.giftexpress.app.data.model.TrendingApiResponse
import com.giftexpress.app.data.model.SearchCategory
import com.giftexpress.app.data.model.SearchProductItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchViewModel @Inject constructor(
    @Named("wizzy") private val okHttpClient: OkHttpClient
) : ViewModel() {

    companion object {
        private const val BASE_URL = "https://api.wizzy.ai/v1/"
        private const val STORE_ID = "62c607f016db11f0aee40a0c8095feae"
        private const val API_KEY = "SWpJcGpqNjBDQWxaaFZFeEhyY1QzZU9aQS9FMDZEcVRRMzdFRDJJa2hOb1VEdFcwRUF4Rk0vS3RxRGxFYjFFbmV1dkkxRkpSUGxhdko4VzJLMTJZc2c9PQ=="
    }

    private val gson = Gson()

    private val _trendingTopics = MutableStateFlow<List<String>>(emptyList())
    val trendingTopics: StateFlow<List<String>> = _trendingTopics.asStateFlow()

    private val _searchCategories = MutableStateFlow<List<SearchCategory>>(emptyList())
    val searchCategories: StateFlow<List<SearchCategory>> = _searchCategories.asStateFlow()

    private val _searchProducts = MutableStateFlow<List<SearchProductItem>>(emptyList())
    val searchProducts: StateFlow<List<SearchProductItem>> = _searchProducts.asStateFlow()

    private val _isLoadingTrending = MutableStateFlow(false)
    val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending.asStateFlow()

    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchTrending()
    }

    fun fetchTrending() {
        viewModelScope.launch {
            _isLoadingTrending.value = true
            try {
                val request = Request.Builder()
                    .url("${BASE_URL}trendingSearches")
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("x-store-id", STORE_ID)
                    .addHeader("x-api-key", API_KEY)
                    .build()

                val responseBody = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute().use { it.body?.string() }
                }

                if (responseBody != null) {
                    val parsed = gson.fromJson(responseBody, TrendingApiResponse::class.java)
                    _trendingTopics.value = parsed.payload?.queries ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoadingTrending.value = false
            }
        }
    }

    fun fetchAutocomplete(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchCategories.value = emptyList()
            _searchProducts.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _isLoadingSearch.value = true
            try {
                val body = JSONObject().apply {
                    put("q", query)
                    put("suggestionsCount", 10)
                    put("productsCount", 10)
                }.toString()

                val requestBody = body.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${BASE_URL}autocomplete")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("x-store-id", STORE_ID)
                    .addHeader("x-api-key", API_KEY)
                    .build()

                val responseBody = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute().use { it.body?.string() }
                }

                if (responseBody != null) {
                    val jsonObj = gson.fromJson(responseBody, com.google.gson.JsonObject::class.java)
                    val statusCode = jsonObj.get("statusCode")?.asInt ?: 200
                    if (statusCode != 200) {
                        _error.value = jsonObj.get("message")?.asString ?: "Search failed"
                    } else {
                        val parsed = gson.fromJson(responseBody, SearchApiResponse::class.java)
                        val payload = parsed.payload
                        val cats = (payload?.categories ?: emptyList()) + (payload?.brands ?: emptyList())
                        _searchCategories.value = cats
                        _searchProducts.value = payload?.products?.result ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoadingSearch.value = false
            }
        }
    }

    fun clearSearch() {
        _searchCategories.value = emptyList()
        _searchProducts.value = emptyList()
    }
}
