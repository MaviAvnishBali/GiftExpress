package com.giftexpress.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CmsPage
import com.giftexpress.app.data.repository.ContentRepository
import com.giftexpress.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CmsViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<CmsPage>>(emptyList())
    val pages: StateFlow<List<CmsPage>> = _pages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPages()
    }

    private fun loadPages() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = contentRepository.getCmsPages()) {
                is NetworkResult.Success -> _pages.value = result.data ?: emptyList()
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun getPageContent(keyword: String): String? =
        _pages.value.firstOrNull { it.title?.contains(keyword, ignoreCase = true) == true }?.content
}
