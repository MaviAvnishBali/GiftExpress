package com.giftexpress.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.OrderApiResponse
import com.giftexpress.app.data.repository.OrderRepository
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<UiState<List<OrderApiResponse>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<OrderApiResponse>>> = _ordersState

    init {
        loadOrders()
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedInSync()

    fun loadOrders() {
        if (!isLoggedIn()) {
            _ordersState.value = UiState.Success(emptyList())
            return
        }
        
        _ordersState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = orderRepository.getOrders()) {
                is NetworkResult.Success -> _ordersState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error -> _ordersState.value = UiState.Error(result.message ?: "Failed to load orders")
                is NetworkResult.Loading -> {}
            }
        }
    }
}
