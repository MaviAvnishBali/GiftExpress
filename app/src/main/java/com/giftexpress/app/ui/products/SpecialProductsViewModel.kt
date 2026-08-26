package com.giftexpress.app.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.data.model.ProductItem
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.data.repository.BrandRepository
import com.giftexpress.app.data.repository.CategoryRepository
import com.giftexpress.app.ui.category.CategoryViewModel
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpecialProductsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<SliderProduct>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<SliderProduct>>> = _productsState

    // Sort / filter — reuse CategoryViewModel.SortOption so the same bottom-sheet UI is shared.
    private val _currentSort = MutableStateFlow<CategoryViewModel.SortOption?>(null)
    val currentSort: StateFlow<CategoryViewModel.SortOption?> = _currentSort

    private val _apiFiltersState = MutableStateFlow<List<com.giftexpress.app.data.model.ProductFilter>?>(null)
    val apiFiltersState: StateFlow<List<com.giftexpress.app.data.model.ProductFilter>?> = _apiFiltersState

    private val _selectedFilters = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val selectedFilters: StateFlow<Map<String, List<String>>> = _selectedFilters

    // Backwards compatibility if needed
    val selectedManufacturer: StateFlow<BrandResponse?> = MutableStateFlow(null)

    private val _brandsState = MutableStateFlow<List<BrandResponse>>(emptyList())
    val brandsState: StateFlow<List<BrandResponse>> = _brandsState

    private val allProducts = mutableListOf<SliderProduct>()
    private var currentPage = 1
    private var isFetching = false
    private var hasMore = true
    private val pageSize = 20

    private var lastSpecialFlag = 0
    private var lastCategoryId = 0
    private var lastBrandId = 0

    fun loadProducts(specialFlag: Int, categoryId: Int = 0, brandId: Int = 0, reset: Boolean = false) {
        if (reset) {
            lastSpecialFlag = specialFlag
            lastCategoryId = categoryId
            lastBrandId = brandId
            currentPage = 1
            hasMore = true
            allProducts.clear()
            _productsState.value = UiState.Loading
        } else {
            if (isFetching || !hasMore) return
        }
        isFetching = true

        val sort = _currentSort.value
        val queryFilters = _selectedFilters.value.mapValues { it.value.joinToString(",") }

        viewModelScope.launch {
            var newItems: List<SliderProduct>? = null
            var errorMsg: String? = null
            var responseFilters: List<com.giftexpress.app.data.model.ProductFilter>? = null

            when {
                brandId > 0 -> when (val r = brandRepository.getBrandProducts(
                    brandId, pageSize, currentPage, sort?.sortBy, sort?.sortOrder, null, queryFilters
                )) {
                    is NetworkResult.Success -> {
                        newItems = r.data?.items?.map { it.toSliderProduct() } ?: emptyList()
                        responseFilters = r.data?.filters
                    }
                    is NetworkResult.Error -> errorMsg = r.message
                    else -> {}
                }
                categoryId > 0 -> when (val r = categoryRepository.getCategoryProducts(
                    categoryId, pageSize, currentPage, null, sort?.sortBy, sort?.sortOrder, specialFlag, queryFilters
                )) {
                    is NetworkResult.Success -> {
                        newItems = r.data?.items ?: emptyList()
                        responseFilters = r.data?.filters
                    }
                    is NetworkResult.Error -> errorMsg = r.message
                    else -> {}
                }
                else -> when (val r = categoryRepository.getSpecialProducts(
                    specialFlag, pageSize, currentPage, sort?.sortBy, sort?.sortOrder, null, queryFilters
                )) {
                    is NetworkResult.Success -> {
                        newItems = r.data?.items ?: emptyList()
                        responseFilters = r.data?.filters
                    }
                    is NetworkResult.Error -> errorMsg = r.message
                    else -> {}
                }
            }

            when {
                newItems != null -> {
                    if (_apiFiltersState.value == null) {
                        _apiFiltersState.value = responseFilters
                    }
                    hasMore = newItems.size >= pageSize
                    allProducts.addAll(newItems)
                    _productsState.value = UiState.Success(allProducts.toList())
                    currentPage++
                }
                errorMsg != null && reset -> _productsState.value = UiState.Error(errorMsg)
            }
            isFetching = false
        }
    }

    private fun reload() = loadProducts(lastSpecialFlag, lastCategoryId, lastBrandId, reset = true)

    fun applySort(option: CategoryViewModel.SortOption?) {
        _currentSort.value = option
        reload()
    }

    fun clearSort() {
        _currentSort.value = null
        reload()
    }

    fun applyFilters(filters: Map<String, List<String>>) {
        _selectedFilters.value = filters
        reload()
    }

    fun clearFilter() {
        _selectedFilters.value = emptyMap()
        reload()
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

    private fun ProductItem.toSliderProduct(): SliderProduct = SliderProduct(
        name = name,
        price = price,
        image = image,
        sku = sku,
        attributes = attributes,
        perfumeType = attributes?.firstOrNull()
    )
}
