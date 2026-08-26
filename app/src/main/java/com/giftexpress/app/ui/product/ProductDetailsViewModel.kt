package com.giftexpress.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.data.model.ProductReview
import com.giftexpress.app.data.model.ReviewBody
import com.giftexpress.app.data.model.ReviewRatingInput
import com.giftexpress.app.data.model.SubmitReviewRequest
import com.giftexpress.app.data.repository.CartCountManager
import com.giftexpress.app.data.repository.CartRepository
import com.giftexpress.app.data.repository.ProductRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val cartRepository: CartRepository,
    private val cartCountManager: CartCountManager
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<ProductDetailsResponse>>(UiState.Loading)
    val productState: StateFlow<UiState<ProductDetailsResponse>> = _productState.asStateFlow()

    private val _cartState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val cartState: StateFlow<UiState<Unit>> = _cartState.asStateFlow()

    private val _reviewsState = MutableStateFlow<UiState<List<ProductReview>>>(UiState.Idle)
    val reviewsState: StateFlow<UiState<List<ProductReview>>> = _reviewsState.asStateFlow()

    private val _submitReviewState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val submitReviewState: StateFlow<UiState<Boolean>> = _submitReviewState.asStateFlow()

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

    fun loadReviews(sku: String) {
        _reviewsState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = repository.getReviews(sku)) {
                is NetworkResult.Success -> _reviewsState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error -> _reviewsState.value = UiState.Error(result.message ?: "Failed")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun submitReview(sku: String, nickname: String, title: String, detail: String, ratingValue: Int) {
        _submitReviewState.value = UiState.Loading
        viewModelScope.launch {
            val request = SubmitReviewRequest(
                review = ReviewBody(
                    sku = sku,
                    nickname = nickname,
                    title = title,
                    detail = detail,
                    ratings = listOf(ReviewRatingInput(value = ratingValue.toString()))
                )
            )
            when (val result = repository.submitReview(request)) {
                is NetworkResult.Success -> {
                    _submitReviewState.value = UiState.Success(true)
                    loadReviews(sku)
                }
                is NetworkResult.Error -> _submitReviewState.value = UiState.Error(result.message ?: "Failed")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun resetSubmitReviewState() {
        _submitReviewState.value = UiState.Idle
    }

    fun addToCart(sku: String, qty: Int) {
        viewModelScope.launch {
            _cartState.value = UiState.Loading
            when (val result = cartRepository.addItemToCart(sku, qty)) {
                is NetworkResult.Success -> {
                    cartCountManager.increment(qty)   // instant optimistic bump
                    _cartState.value = UiState.Success(Unit)
                    // Reconcile the badge to the real server cart (sum of item quantities).
                    (cartRepository.getCart() as? NetworkResult.Success)?.data?.let { items ->
                        cartCountManager.setCount(items.sumOf { it.qty ?: 0 })
                    }
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
