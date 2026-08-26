package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.CmsPage
import com.giftexpress.app.data.model.EnquiryRequest
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class ContentRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCmsPages(): NetworkResult<List<CmsPage>> {
        return try {
            val response = apiService.getCmsPages()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to load pages: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun getContactUs(): NetworkResult<CmsPage> {
        return try {
            val response = apiService.getContactUs()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to load contact info: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun submitEnquiry(request: EnquiryRequest): NetworkResult<Boolean> {
        return try {
            val response = apiService.submitEnquiry(request)
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Failed to submit enquiry: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
