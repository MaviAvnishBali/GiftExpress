package com.giftexpress.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.RewardHistoryItem
import com.giftexpress.app.data.model.WalletSummary
import com.giftexpress.app.data.repository.RewardRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _walletState = MutableStateFlow<UiState<WalletSummary>>(UiState.Loading)
    val walletState: StateFlow<UiState<WalletSummary>> = _walletState

    private val _historyState = MutableStateFlow<UiState<List<RewardHistoryItem>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<RewardHistoryItem>>> = _historyState

    private val _saveState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val saveState: StateFlow<UiState<Boolean>> = _saveState

    init {
        loadWallet()
    }

    fun loadWallet() {
        _walletState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = rewardRepository.getRewardPoints()) {
                is NetworkResult.Success -> _walletState.value = UiState.Success(result.data!!)
                is NetworkResult.Error -> _walletState.value = UiState.Error(result.message ?: "Failed")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun loadHistory() {
        _historyState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = rewardRepository.getRewardHistory()) {
                is NetworkResult.Success -> _historyState.value = UiState.Success(result.data ?: emptyList())
                is NetworkResult.Error -> _historyState.value = UiState.Error(result.message ?: "Failed")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun saveSubscription(subscribeEarnings: Boolean, subscribeExpire: Boolean) {
        _saveState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = rewardRepository.updateSubscription(subscribeEarnings, subscribeExpire)) {
                is NetworkResult.Success -> _saveState.value = UiState.Success(true)
                is NetworkResult.Error -> _saveState.value = UiState.Error(result.message ?: "Failed to save")
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = UiState.Idle
    }
}
