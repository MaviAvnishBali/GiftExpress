package com.giftexpress.app.ui.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.data.repository.BrandRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Brands tab (AllBrandsScreen) — just the list of all brands.
 * Brand product listings now reuse the unified SpecialProductsFragment, so no
 * per-brand product/banner/selection state lives here anymore.
 */
@HiltViewModel
class BrandViewModel @Inject constructor(
    private val brandRepository: BrandRepository
) : ViewModel() {

    private val _brandsState = MutableStateFlow<UiState<List<BrandResponse>>>(UiState.Idle)
    val brandsState: StateFlow<UiState<List<BrandResponse>>> = _brandsState

    init {
        fetchBrands()
    }

    fun fetchBrands() {
        _brandsState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = brandRepository.getBrands()) {
                is NetworkResult.Success ->
                    _brandsState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error ->
                    _brandsState.value = UiState.Error(result.message ?: "Failed to fetch brands")
                else -> {}
            }
        }
    }
}
