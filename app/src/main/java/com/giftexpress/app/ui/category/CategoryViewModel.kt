package com.giftexpress.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.data.model.CategoryProductsResponse
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.data.repository.BrandRepository
import com.giftexpress.app.data.repository.CategoryRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository
) : ViewModel() {

    private val _categoryDataState = MutableStateFlow<UiState<List<SliderResponse>>>(UiState.Idle)
    val categoryDataState: StateFlow<UiState<List<SliderResponse>>> = _categoryDataState

    private val _productsState = MutableStateFlow<UiState<List<SliderProduct>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<SliderProduct>>> = _productsState

    private var currentPage = 1
    private var totalPages = 1
    private var isFetchingNextPage = false
    private val allProducts = mutableListOf<SliderProduct>()

    private val _currentSort = MutableStateFlow<SortOption?>(null)
    val currentSort: StateFlow<SortOption?> = _currentSort

    private val _apiFiltersState = MutableStateFlow<List<com.giftexpress.app.data.model.ProductFilter>?>(null)
    val apiFiltersState: StateFlow<List<com.giftexpress.app.data.model.ProductFilter>?> = _apiFiltersState

    private val _selectedFilters = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val selectedFilters: StateFlow<Map<String, List<String>>> = _selectedFilters

    // For backwards compatibility with CategoryBottomActions if needed, but we will update it too
    val selectedManufacturer: StateFlow<BrandResponse?> = MutableStateFlow(null)

    private val _brandsState = MutableStateFlow<List<BrandResponse>>(emptyList())
    val brandsState: StateFlow<List<BrandResponse>> = _brandsState

    enum class SortOption(val label: String, val sortBy: String, val sortOrder: String) {
        NAME_ASC("Product Name A-Z", "name", "asc"),
        NAME_DESC("Product Name Z-A", "name", "desc"),
        PRICE_ASC("Price - Low to High", "price", "asc"),
        PRICE_DESC("Price - High to Low", "price", "desc")
    }

    fun fetchCategoryData(categoryId: Int) {
        _categoryDataState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryOfferAndBanner(categoryId)) {
                is NetworkResult.Success -> {
                    _categoryDataState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _categoryDataState.value = UiState.Error(result.message ?: "Failed to fetch category data")
                }
                else -> {}
            }
        }
    }

    fun applySort(categoryId: Int, option: SortOption?) {
        _currentSort.value = option
        fetchProducts(categoryId, reset = true)
    }

    private var lastCategoryId: Int = 0

    fun fetchProducts(categoryId: Int, reset: Boolean = false) {
        if (reset) {
            if (lastCategoryId != categoryId) {
                _apiFiltersState.value = null
                _selectedFilters.value = emptyMap()
            }
            lastCategoryId = categoryId
            currentPage = 1
            allProducts.clear()
            _productsState.value = UiState.Loading
        } else {
            if (isFetchingNextPage || currentPage > totalPages) return
            isFetchingNextPage = true
        }
        val sort = _currentSort.value
        
        // Build filters map (join list of strings by comma)
        val queryFilters = _selectedFilters.value.mapValues { it.value.joinToString(",") }

        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryProducts(
                categoryId = categoryId,
                pageSize = 20,
                currentPage = currentPage,
                manufacturer = null,
                sortBy = sort?.sortBy,
                sortOrder = sort?.sortOrder,
                specialFlag = null,
                filters = queryFilters
            )) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response != null) {
                        totalPages = response.totalPages ?: 1
                        if (_apiFiltersState.value == null) {
                            _apiFiltersState.value = response.filters
                        }
                        allProducts.addAll(response.items ?: emptyList())
                        _productsState.value = UiState.Success(allProducts.toList())
                        currentPage++
                    }
                }
                is NetworkResult.Error -> {
                    if (reset) {
                        _productsState.value = UiState.Error(result.message ?: "Failed to fetch products")
                    }
                }
                else -> {}
            }
            isFetchingNextPage = false
        }
    }

    fun loadNextPage(categoryId: Int) {
        fetchProducts(categoryId, reset = false)
    }

    fun clearSort(categoryId: Int) {
        _currentSort.value = null
        fetchProducts(categoryId, reset = true)
    }

    fun applyFilters(categoryId: Int, filters: Map<String, List<String>>) {
        _selectedFilters.value = filters
        fetchProducts(categoryId, reset = true)
    }

    fun clearFilter(categoryId: Int) {
        _selectedFilters.value = emptyMap()
        fetchProducts(categoryId, reset = true)
    }

    fun loadBrandsIfNeeded() {
        if (_brandsState.value.isNotEmpty()) return
        viewModelScope.launch {
            when (val result = brandRepository.getBrands()) {
                is NetworkResult.Success -> _brandsState.value = result.data ?: emptyList()
                else -> {}
            }
        }
    }
}
