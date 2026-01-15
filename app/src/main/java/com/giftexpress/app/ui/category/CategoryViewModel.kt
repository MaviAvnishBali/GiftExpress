package com.giftexpress.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CategoryProductsResponse
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.data.model.SliderResponse
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
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoryDataState = MutableStateFlow<UiState<List<SliderResponse>>>(UiState.Idle)
    val categoryDataState: StateFlow<UiState<List<SliderResponse>>> = _categoryDataState

    private val _productsState = MutableStateFlow<UiState<List<SliderProduct>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<SliderProduct>>> = _productsState

    private var currentPage = 1
    private var totalPages = 1
    private var isFetchingNextPage = false
    private val allProducts = mutableListOf<SliderProduct>()

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

    fun fetchProducts(categoryId: Int, manufacturer: Int? = null, reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            allProducts.clear()
            _productsState.value = UiState.Loading
        } else {
            if (isFetchingNextPage || currentPage > totalPages) return
            isFetchingNextPage = true
        }

        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryProducts(categoryId, 20, currentPage, manufacturer)) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response != null) {
                        totalPages = response.totalPages ?: 1
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

    fun loadNextPage(categoryId: Int, manufacturer: Int? = null) {
        fetchProducts(categoryId, manufacturer, reset = false)
    }
}
