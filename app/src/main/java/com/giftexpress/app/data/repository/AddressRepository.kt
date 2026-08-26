package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.data.model.SaveAddressRequest
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class AddressRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Matches iOS: addressViewModel.getAddress → GET giftexpress/customer/addresses
    suspend fun getAddresses(): NetworkResult<List<CustomerAddressModel>> {
        return try {
            val response = apiService.getAddresses()
            if (response.isSuccessful && response.body() != null)
                NetworkResult.Success(response.body()!!)
            else
                NetworkResult.Error("Failed to load addresses: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Matches iOS: addressViewModel.addAddress → POST giftexpress/customer/address
    suspend fun addAddress(request: SaveAddressRequest): NetworkResult<CustomerAddressModel> {
        return try {
            val response = apiService.addAddress(request)
            if (response.isSuccessful)
                NetworkResult.Success(response.body() ?: CustomerAddressModel())
            else
                NetworkResult.Error("Failed to save address: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Matches iOS: addressViewModel.updateAddress → PUT giftexpress/customer/address/{id}
    suspend fun updateAddress(id: Int, request: SaveAddressRequest): NetworkResult<CustomerAddressModel> {
        return try {
            val response = apiService.updateAddress(id, request)
            if (response.isSuccessful)
                NetworkResult.Success(response.body() ?: CustomerAddressModel())
            else
                NetworkResult.Error("Failed to update address: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Matches iOS: addressViewModel.deleteAddress → DELETE giftexpress/customer/address/{id}
    suspend fun deleteAddress(id: Int): NetworkResult<Boolean> {
        return try {
            val response = apiService.deleteAddress(id)
            if (response.isSuccessful)
                NetworkResult.Success(true)
            else
                NetworkResult.Error("Failed to delete address: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Matches iOS: addressViewModel.fetchCountries → GET directory/countries
    suspend fun getCountries(): NetworkResult<List<com.giftexpress.app.data.model.CountryModel>> {
        return try {
            val response = apiService.getCountries()
            if (response.isSuccessful && response.body() != null)
                NetworkResult.Success(response.body()!!)
            else
                NetworkResult.Error("Failed to fetch countries: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
