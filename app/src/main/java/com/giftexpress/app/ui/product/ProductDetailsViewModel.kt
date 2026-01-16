package com.giftexpress.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.data.repository.ProductRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.giftexpress.app.data.repository.CartRepository

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<ProductDetailsResponse>>(UiState.Loading)
    val productState: StateFlow<UiState<ProductDetailsResponse>> = _productState.asStateFlow()

    private val _cartState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val cartState: StateFlow<UiState<Unit>> = _cartState.asStateFlow()

    fun getProductDetails(sku: String) {
        viewModelScope.launch {
            _productState.value = UiState.Loading
            when (val result = repository.getProductDetails(sku)) {
                is NetworkResult.Success -> {
                    if (result.data != null) {
                        _productState.value = UiState.Success(result.data)
                    } else {
                        _productState.value = UiState.Error("Product not found")
                    }
                }
                is NetworkResult.Error -> {
                    _productState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    _productState.value = UiState.Loading
                }
            }
        }
    }

    fun addToCart(sku: String, qty: Int) {
        viewModelScope.launch {
            _cartState.value = UiState.Loading
            when (val result = cartRepository.addItemToCart(sku, qty)) {
                is NetworkResult.Success -> {
                    _cartState.value = UiState.Success(Unit)
                }
                is NetworkResult.Error -> {
                    _cartState.value = UiState.Error(result.message ?: "Failed to add to cart")
                }
                is NetworkResult.Loading -> {
                    _cartState.value = UiState.Loading
                }
            }
        }
    }
}
