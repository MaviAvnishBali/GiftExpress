package com.giftexpress.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CategoriesMainScreenResponse
import com.giftexpress.app.data.repository.CategoryRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<UiState<CategoriesMainScreenResponse>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<CategoriesMainScreenResponse>> = _categoriesState

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        _categoriesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoriesMainScreen()) {
                is NetworkResult.Success -> {
                    _categoriesState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _categoriesState.value = UiState.Error(result.message ?: "Failed to fetch categories")
                }
                else -> {}
            }
        }
    }
}
