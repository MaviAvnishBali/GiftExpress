package com.giftexpress.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CmsPage
import com.giftexpress.app.data.model.EnquiryRequest
import com.giftexpress.app.data.repository.ContentRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _contactState = MutableStateFlow<UiState<CmsPage>>(UiState.Loading)
    val contactState: StateFlow<UiState<CmsPage>> = _contactState

    private val _enquiryState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val enquiryState: StateFlow<UiState<Boolean>> = _enquiryState

    init {
        loadContactInfo()
    }

    private fun loadContactInfo() {
        viewModelScope.launch {
            when (val result = contentRepository.getContactUs()) {
                is NetworkResult.Success -> _contactState.value = UiState.Success(result.data!!)
                is NetworkResult.Error -> _contactState.value = UiState.Error(result.message ?: "Failed")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun submitEnquiry(request: EnquiryRequest) {
        _enquiryState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = contentRepository.submitEnquiry(request)) {
                is NetworkResult.Success -> _enquiryState.value = UiState.Success(true)
                is NetworkResult.Error -> _enquiryState.value = UiState.Error(result.message ?: "Failed to submit")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun resetEnquiryState() {
        _enquiryState.value = UiState.Idle
    }
}
