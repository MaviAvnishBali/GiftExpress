package com.giftexpress.app.ui.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.data.model.SaveAddressRequest
import com.giftexpress.app.data.repository.AddressRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressBookViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _addressesState = MutableStateFlow<UiState<List<CustomerAddressModel>>>(UiState.Loading)
    val addressesState: StateFlow<UiState<List<CustomerAddressModel>>> = _addressesState

    private val _saveState = MutableStateFlow<UiState<CustomerAddressModel>>(UiState.Idle)
    val saveState: StateFlow<UiState<CustomerAddressModel>> = _saveState

    private val _deleteState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Boolean>> = _deleteState

    private val _countriesState = MutableStateFlow<UiState<List<com.giftexpress.app.data.model.CountryModel>>>(UiState.Idle)
    val countriesState: StateFlow<UiState<List<com.giftexpress.app.data.model.CountryModel>>> = _countriesState

    fun fetchCountries() {
        if (_countriesState.value is UiState.Success) return
        _countriesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = addressRepository.getCountries()) {
                is NetworkResult.Success -> _countriesState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error -> _countriesState.value = UiState.Error(result.message ?: "Failed to fetch countries")
                else -> {}
            }
        }
    }

    // Matches iOS viewWillAppear → getAddress
    fun loadAddresses() {
        _addressesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = addressRepository.getAddresses()) {
                is NetworkResult.Success -> _addressesState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error -> _addressesState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun saveAddress(request: SaveAddressRequest, existingId: Int? = null) {
        _saveState.value = UiState.Loading
        viewModelScope.launch {
            val result = if (existingId != null)
                addressRepository.updateAddress(existingId, request)
            else
                addressRepository.addAddress(request)
            when (result) {
                is NetworkResult.Success -> {
                    _saveState.value = UiState.Success(result.data!!)
                    loadAddresses()
                }
                is NetworkResult.Error -> _saveState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun deleteAddress(id: Int) {
        _deleteState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = addressRepository.deleteAddress(id)) {
                is NetworkResult.Success -> {
                    _deleteState.value = UiState.Success(true)
                    loadAddresses()
                }
                is NetworkResult.Error -> _deleteState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun resetSaveState() { _saveState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }
}
