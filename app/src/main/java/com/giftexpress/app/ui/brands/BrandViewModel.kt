package com.giftexpress.app.ui.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.data.model.ProductItem
import com.giftexpress.app.data.model.ProductListResponse
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
class BrandViewModel @Inject constructor(
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _brandsState = MutableStateFlow<UiState<List<BrandResponse>>>(UiState.Idle)
    val brandsState: StateFlow<UiState<List<BrandResponse>>> = _brandsState

    private val _brandProductsState = MutableStateFlow<UiState<List<SliderProduct>>>(UiState.Idle)
    val brandProductsState: StateFlow<UiState<List<SliderProduct>>> = _brandProductsState

    private val _brandBannersState = MutableStateFlow<UiState<List<SliderResponse>>>(UiState.Idle)
    val brandBannersState: StateFlow<UiState<List<SliderResponse>>> = _brandBannersState

    private val _selectedBrand = MutableStateFlow<Pair<Int, String>?>(null)
    val selectedBrand: StateFlow<Pair<Int, String>?> = _selectedBrand

    private var currentPage = 1
    private var totalPages = 1
    private var isFetchingNextPage = false
    private var currentBrandId: Int? = null
    private val allProducts = mutableListOf<SliderProduct>()

    init {
        fetchBrands()
    }

    fun fetchBrands() {
        _brandsState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = brandRepository.getBrands()) {
                is NetworkResult.Success -> {
                    _brandsState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _brandsState.value = UiState.Error(result.message ?: "Failed to fetch brands")
                }
                else -> {}
            }
        }
    }

    fun fetchBrandProducts(brandId: Int, reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            currentBrandId = brandId
            allProducts.clear()
            _brandProductsState.value = UiState.Loading
        } else {
            if (isFetchingNextPage || currentPage > totalPages) return
            isFetchingNextPage = true
        }

        viewModelScope.launch {
            when (val result = brandRepository.getBrandProducts(brandId, 20, currentPage)) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response != null) {
                        totalPages = response.totalPages
                        val newProducts = response.items.map { it.toSliderProduct() }
                        allProducts.addAll(newProducts)
                        _brandProductsState.value = UiState.Success(allProducts.toList())
                        currentPage++
                    }
                }
                is NetworkResult.Error -> {
                    if (reset) {
                        _brandProductsState.value = UiState.Error(result.message ?: "Failed to fetch brand products")
                    }
                }
                else -> {}
            }
            isFetchingNextPage = false
        }
    }

    fun loadNextPage() {
        currentBrandId?.let { brandId ->
            if (!isFetchingNextPage && currentPage <= totalPages) {
                fetchBrandProducts(brandId, reset = false)
            }
        }
    }

    fun resetPagination() {
        currentPage = 1
        totalPages = 1
        isFetchingNextPage = false
        allProducts.clear()
    }

    private fun ProductItem.toSliderProduct(): SliderProduct {
        return SliderProduct(
            name = name,
            price = price,
            image = image,
            sku = sku,
            attributes = attributes,
            perfumeType = attributes?.firstOrNull()
        )
    }

    fun fetchBrandBanners(brandId: Int) {
        _brandBannersState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryOfferAndBanner(brandId)) {
                is NetworkResult.Success -> {
                    _brandBannersState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _brandBannersState.value = UiState.Error(result.message ?: "Failed to fetch brand banners")
                }
                else -> {}
            }
        }
    }

    fun selectBrand(id: Int, name: String) {
        _selectedBrand.value = id to name
    }

    fun clearSelectedBrand() {
        _selectedBrand.value = null
    }
}
