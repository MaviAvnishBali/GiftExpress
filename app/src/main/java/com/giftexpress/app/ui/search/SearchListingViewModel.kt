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

    private val _apiFiltersState = MutableStateFlow<List<com.giftexpress.app.data.model.ProductFilter>?>(null)
    val apiFiltersState: StateFlow<List<com.giftexpress.app.data.model.ProductFilter>?> = _apiFiltersState.asStateFlow()

    private val _selectedFilters = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val selectedFilters: StateFlow<Map<String, List<String>>> = _selectedFilters.asStateFlow()

    private val _currentSort = MutableStateFlow<com.giftexpress.app.ui.category.CategoryViewModel.SortOption?>(null)
    val currentSort: StateFlow<com.giftexpress.app.ui.category.CategoryViewModel.SortOption?> = _currentSort.asStateFlow()

    fun searchProducts(query: String, resetPage: Boolean = true) {
        if (resetPage) {
            currentPage = 1
            if (currentQuery != query) {
                _apiFiltersState.value = null
                _selectedFilters.value = emptyMap()
                _currentSort.value = null
            }
            currentQuery = query
            _products.value = emptyList()
            isLastPage = false
        }
        if (isLastPage && !resetPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val isFilteringOrSorting = _selectedFilters.value.isNotEmpty() || _currentSort.value != null
                val endpoint = if (isFilteringOrSorting) "products/filter" else "products/search"

                val params = buildString {
                    if (isFilteringOrSorting) {
                        val filterObj = com.google.gson.JsonObject()
                        filterObj.addProperty("q", currentQuery)
                        filterObj.addProperty("page", currentPage)
                        filterObj.addProperty("productsCount", 20)
                        filterObj.addProperty("currency", "USD")
                        filterObj.addProperty("getAllVariants", "false")
                        filterObj.addProperty("showOOSProductsInOrder", "false")
                        filterObj.addProperty("attributeFacetValuesLimit", 20)
                        filterObj.addProperty("type", "DEFAULT")
                        
                        val inStockArr = com.google.gson.JsonArray()
                        inStockArr.add(true)
                        filterObj.add("inStock", inStockArr)

                        _currentSort.value?.let { sortOpt ->
                            val sortArr = com.google.gson.JsonArray()
                            val sortItem = com.google.gson.JsonObject()
                            val field = if (sortOpt.sortBy == "price") "sellingPrice" else sortOpt.sortBy
                            sortItem.addProperty("field", field)
                            sortItem.addProperty("order", sortOpt.sortOrder.lowercase())
                            sortArr.add(sortItem)
                            filterObj.add("sort", sortArr)
                        }

                        val attrsObj = com.google.gson.JsonObject()
                        _selectedFilters.value.forEach { (key, values) ->
                            if (values.isEmpty()) return@forEach
                            val cleanValues = values.map { it.trim() }.filter { it.isNotEmpty() }
                            if (cleanValues.isEmpty()) return@forEach
                            
                            val valArray = com.google.gson.JsonArray()
                            
                            when (key) {
                                "sellingPrice", "price" -> {
                                    val priceArr = com.google.gson.JsonArray()
                                    cleanValues.forEach { v ->
                                        val parts = v.split("-")
                                        if (parts.size == 2) {
                                            val gte = parts[0].toIntOrNull()
                                            val lte = parts[1].toIntOrNull()
                                            if (gte != null && lte != null) {
                                                val pObj = com.google.gson.JsonObject()
                                                pObj.addProperty("gte", gte)
                                                pObj.addProperty("lte", lte)
                                                priceArr.add(pObj)
                                            }
                                        }
                                    }
                                    if (priceArr.size() > 0) filterObj.add("sellingPrice", priceArr)
                                }
                                "brand" -> {
                                    cleanValues.forEach { valArray.add(it) }
                                    filterObj.add("brands", valArray)
                                }
                                "gender" -> {
                                    cleanValues.forEach { valArray.add(it) }
                                    filterObj.add("genders", valArray)
                                }
                                "color", "colors" -> {
                                    cleanValues.forEach { valArray.add(it) }
                                    filterObj.add("colors", valArray)
                                }
                                "size", "sizes" -> {
                                    cleanValues.forEach { valArray.add(it) }
                                    filterObj.add("sizes", valArray)
                                }
                                else -> {
                                    cleanValues.forEach { valArray.add(it) }
                                    attrsObj.add(key, valArray)
                                }
                            }
                        }
                        if (attrsObj.size() > 0) {
                            filterObj.add("attributes", attrsObj)
                        }
                        
                        append("filters=${filterObj.toString().encodeUrl()}")
                        append("&group=page")
                    } else {
                        append("q=${currentQuery.encodeUrl()}")
                        append("&page=$currentPage")
                        append("&productsCount=20")
                        append("&currency=USD")
                        append("&inStock=[true]")
                    }
                }

                val requestBody = params.toRequestBody("application/x-www-form-urlencoded".toMediaType())

                val request = Request.Builder()
                    .url("${BASE_URL}$endpoint")
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
                        
                        if (_apiFiltersState.value == null && resetPage && newItems.isNotEmpty()) {
                            _apiFiltersState.value = buildProductFilters(newItems)
                        }
                        
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

    private fun buildProductFilters(products: List<SearchListingProduct>): List<com.giftexpress.app.data.model.ProductFilter> {
        val brandMap = mutableMapOf<String, Int>()
        val perfumeTypeMap = mutableMapOf<String, Int>()
        val specialMap = mutableMapOf<String, Int>()
        val genderMap = mutableMapOf<String, Int>()
        
        var bucket0_25 = 0
        var bucket25_50 = 0
        var bucket50_100 = 0
        var bucket100_plus = 0
        
        products.forEach { product ->
            val price = product.displayPrice
            when {
                price < 25 -> bucket0_25++
                price < 50 -> bucket25_50++
                price < 100 -> bucket50_100++
                else -> bucket100_plus++
            }
            
            product.brand?.let { brandMap[it] = brandMap.getOrDefault(it, 0) + 1 }
            product.gender?.let { genderMap[it] = genderMap.getOrDefault(it, 0) + 1 }
            
            product.attributes?.forEach { attr ->
                val id = attr.id ?: return@forEach
                val values = attr.values?.mapNotNull { it.value?.firstOrNull() } ?: emptyList()
                when (id) {
                    "perfume_type" -> values.forEach { perfumeTypeMap[it] = perfumeTypeMap.getOrDefault(it, 0) + 1 }
                    "special_flag" -> values.forEach { specialMap[it] = specialMap.getOrDefault(it, 0) + 1 }
                    "gender" -> values.forEach { genderMap[it] = genderMap.getOrDefault(it, 0) + 1 }
                }
            }
        }
        
        fun makeOptions(map: Map<String, Int>): List<com.giftexpress.app.data.model.FilterOption> {
            return map.map { 
                com.giftexpress.app.data.model.FilterOption(
                    labelRaw = com.google.gson.JsonPrimitive(it.key), 
                    value = it.key, 
                    count = it.value
                ) 
            }.sortedBy { it.value }
        }
        
        val priceOptions = listOf(
            com.giftexpress.app.data.model.FilterOption(labelRaw = com.google.gson.JsonPrimitive("$0 - $25"), value = "0-25", count = bucket0_25),
            com.giftexpress.app.data.model.FilterOption(labelRaw = com.google.gson.JsonPrimitive("$25 - $50"), value = "25-50", count = bucket25_50),
            com.giftexpress.app.data.model.FilterOption(labelRaw = com.google.gson.JsonPrimitive("$50 - $100"), value = "50-100", count = bucket50_100),
            com.giftexpress.app.data.model.FilterOption(labelRaw = com.google.gson.JsonPrimitive("$100+"), value = "100-9999", count = bucket100_plus)
        )
        
        return listOf(
            com.giftexpress.app.data.model.ProductFilter(name = "Price", requestVar = "sellingPrice", options = priceOptions),
            com.giftexpress.app.data.model.ProductFilter(name = "Perfume Type", requestVar = "perfume_type", options = makeOptions(perfumeTypeMap)),
            com.giftexpress.app.data.model.ProductFilter(name = "Brand", requestVar = "brand", options = makeOptions(brandMap)),
            com.giftexpress.app.data.model.ProductFilter(name = "Special", requestVar = "special_flag", options = makeOptions(specialMap)),
            com.giftexpress.app.data.model.ProductFilter(name = "Gender", requestVar = "gender", options = makeOptions(genderMap))
        )
    }

    fun applySort(option: com.giftexpress.app.ui.category.CategoryViewModel.SortOption?) {
        _currentSort.value = option
        searchProducts(currentQuery, resetPage = true)
    }

    fun applyFilters(filters: Map<String, List<String>>) {
        _selectedFilters.value = filters
        searchProducts(currentQuery, resetPage = true)
    }

    fun clearFilter() {
        _selectedFilters.value = emptyMap()
        searchProducts(currentQuery, resetPage = true)
    }

    fun loadNextPage() {
        if (!isLastPage && !_isLoading.value) {
            searchProducts(currentQuery, resetPage = false)
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}
