package com.giftexpress.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.OrderApiResponse
import com.giftexpress.app.data.repository.OrderRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<UiState<OrderApiResponse>>(UiState.Loading)
    val orderState: StateFlow<UiState<OrderApiResponse>> = _orderState

    fun loadOrder(orderId: Int) {
        _orderState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = orderRepository.getOrderDetails(orderId)) {
                is NetworkResult.Success -> _orderState.value = UiState.Success(result.data!!)
                is NetworkResult.Error -> _orderState.value = UiState.Error(result.message ?: "Failed to load order")
                is NetworkResult.Loading -> {}
            }
        }
    }
}
