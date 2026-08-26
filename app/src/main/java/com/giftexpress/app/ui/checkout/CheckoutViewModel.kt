package com.giftexpress.app.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.AddressInformation
import com.giftexpress.app.data.model.CartItemDetail
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.data.model.EstimateShippingRequest
import com.giftexpress.app.data.model.OrderTotals
import com.giftexpress.app.data.model.PaymentMethodsResponse
import com.giftexpress.app.data.model.ShippingEstimateAddress
import com.giftexpress.app.data.model.ShippingInformationRequest
import com.giftexpress.app.data.model.ShippingMethod
import com.giftexpress.app.data.repository.AddressRepository
import com.giftexpress.app.data.repository.CartRepository
import com.giftexpress.app.data.repository.CheckoutRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: CheckoutRepository,
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _shippingMethods = MutableStateFlow<UiState<List<ShippingMethod>>>(UiState.Idle)
    val shippingMethods: StateFlow<UiState<List<ShippingMethod>>> = _shippingMethods.asStateFlow()

    private val _orderTotalsState = MutableStateFlow<UiState<OrderTotals>>(UiState.Idle)
    val orderTotalsState: StateFlow<UiState<OrderTotals>> = _orderTotalsState.asStateFlow()

    private val _couponState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val couponState: StateFlow<UiState<Boolean>> = _couponState.asStateFlow()

    private val _rewardsState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val rewardsState: StateFlow<UiState<Boolean>> = _rewardsState.asStateFlow()

    private val _selectedShippingMethod = MutableStateFlow<ShippingMethod?>(null)
    val selectedShippingMethod: StateFlow<ShippingMethod?> = _selectedShippingMethod.asStateFlow()

    // Uses CustomerAddressModel (matches iOS CustomerAddress)
    private val _selectedAddress = MutableStateFlow<CustomerAddressModel?>(null)
    val selectedAddress: StateFlow<CustomerAddressModel?> = _selectedAddress.asStateFlow()

    private val _allAddresses = MutableStateFlow<List<CustomerAddressModel>>(emptyList())
    val allAddresses: StateFlow<List<CustomerAddressModel>> = _allAddresses.asStateFlow()

    private val _couponCode = MutableStateFlow("")
    val couponCode: StateFlow<String> = _couponCode.asStateFlow()

    private val _appliedCoupon = MutableStateFlow("")
    val appliedCoupon: StateFlow<String> = _appliedCoupon.asStateFlow()

    private val _rewardsApplied = MutableStateFlow(false)
    val rewardsApplied: StateFlow<Boolean> = _rewardsApplied.asStateFlow()

    private val _isProtectionSelected = MutableStateFlow(false)
    val isProtectionSelected: StateFlow<Boolean> = _isProtectionSelected.asStateFlow()

    private val _rewardsInput = MutableStateFlow("")
    val rewardsInput: StateFlow<String> = _rewardsInput.asStateFlow()

    // Cart items shown in the "Order Summary" section (mockup screen 40)
    private val _cartItems = MutableStateFlow<List<CartItemDetail>>(emptyList())
    val cartItems: StateFlow<List<CartItemDetail>> = _cartItems.asStateFlow()

    private val _cartItemUpdating = MutableStateFlow(false)
    val cartItemUpdating: StateFlow<Boolean> = _cartItemUpdating.asStateFlow()

    /**
     * Matches iOS viewDidLoad → getAddress() → setDefaultAddressIfNeeded → getShippingMethods → saveShippingInformation → getTotal
     * Call on CheckoutFragment.onViewCreated()
     */
    fun loadCheckout() {
        loadCartItems()
        getCartTotals()
        viewModelScope.launch {
            when (val result = addressRepository.getAddresses()) {
                is NetworkResult.Success -> {
                    val addresses = result.data ?: emptyList()
                    _allAddresses.value = addresses

                    if (addresses.isNotEmpty() && _selectedAddress.value == null) {
                        // Matches iOS setDefaultAddressIfNeeded — auto-select default shipping
                        val defaultShipping = addresses.firstOrNull { it.defaultShipping == true }
                            ?: addresses.first()
                        _selectedAddress.value = defaultShipping
                        estimateShipping(defaultShipping)
                    }
                }
                is NetworkResult.Error -> { /* show no address state */ }
                else -> {}
            }
        }
    }

    fun setAddress(address: CustomerAddressModel) {
        _selectedAddress.value = address
        estimateShipping(address)
    }

    fun selectShippingMethod(method: ShippingMethod) {
        _selectedShippingMethod.value = method
        val address = _selectedAddress.value ?: return
        saveShippingInfo(address, method)
    }

    // Matches iOS getShippingMethods → POST carts/mine/estimate-shipping-methods
    private fun estimateShipping(address: CustomerAddressModel) {
        viewModelScope.launch {
            _shippingMethods.value = UiState.Loading
            val request = EstimateShippingRequest(address.toShippingEstimateAddress())
            when (val result = repository.estimateShippingMethods(request)) {
                is NetworkResult.Success -> {
                    val available = result.data?.filter { it.available == true } ?: emptyList()
                    _shippingMethods.value = UiState.Success(available)
                    // Matches iOS: auto-select first method
                    if (available.isNotEmpty()) {
                        available.firstOrNull()?.let { selectShippingMethod(it) }
                    } else {
                        getCartTotals()
                    }
                }
                is NetworkResult.Error -> {
                    _shippingMethods.value = UiState.Error(result.message ?: "Failed to load shipping methods")
                    getCartTotals()
                }
                else -> {}
            }
        }
    }

    // Matches iOS saveShippingInformation → POST carts/mine/shipping-information → then getTotal
    private fun saveShippingInfo(address: CustomerAddressModel, method: ShippingMethod) {
        viewModelScope.launch {
            _orderTotalsState.value = UiState.Loading
            val shippingAddr = address.toShippingEstimateAddress()

            // Billing fallback: use default billing, or fall back to shipping (matches iOS)
            val billingAddress = _allAddresses.value
                .firstOrNull { it.defaultBilling == true }
                ?.toShippingEstimateAddress()
                ?: shippingAddr

            val request = ShippingInformationRequest(
                addressInformation = AddressInformation(
                    shippingAddress = shippingAddr,
                    billingAddress = billingAddress,
                    shippingCarrierCode = method.carrierCode ?: "freeshipping",
                    shippingMethodCode = method.methodCode ?: "freeshipping"
                )
            )
            when (repository.saveShippingInformation(request)) {
                is NetworkResult.Success -> {
                    // After saving shipping info, fetch fresh totals (matches iOS getTotal)
                    getCartTotals()
                }
                is NetworkResult.Error -> {
                    getCartTotals()
                }
                else -> {}
            }
        }
    }

    // Matches iOS getTotal → CartManager.getCartCount → GET carts/mine/totals
    fun getCartTotals() {
        viewModelScope.launch {
            if (_orderTotalsState.value !is UiState.Success) {
                _orderTotalsState.value = UiState.Loading
            }
            when (val result = repository.getCartTotals()) {
                is NetworkResult.Success -> {
                    result.data?.let {
                        _orderTotalsState.value = UiState.Success(it)
                        updateProtectionState(it)
                    } ?: run { _orderTotalsState.value = UiState.Error("Empty totals") }
                }
                is NetworkResult.Error -> _orderTotalsState.value = UiState.Error(result.message ?: "Failed to calculate total")
                else -> {}
            }
        }
    }

    private fun updateProtectionState(totals: OrderTotals) {
        val amastySegment = totals.totalSegments?.find { it.code == "amasty_extrafee" }
        val feeValue = amastySegment?.extensionAttributes?.taxAmastyExtrafeeDetails?.valueInclTax
            ?: amastySegment?.value ?: 0.0
        _isProtectionSelected.value = feeValue > 0
    }

    fun toggleShippingProtection() {
        val address = _selectedAddress.value ?: return
        val currentSelected = _isProtectionSelected.value
        val newState = !currentSelected
        
        viewModelScope.launch {
            _orderTotalsState.value = UiState.Loading
            
            val request = mapOf(
                "cartId" to "mine",
                "information" to mapOf(
                    "fee_id" to 1,
                    "options_ids" to if (newState) listOf(1) else emptyList<Int>()
                ),
                "addressInformation" to mapOf(
                    "address" to mapOf(
                        "country_id" to (address.countryId ?: "US"),
                        "postcode" to (address.postcode ?: ""),
                        "region" to (address.region?.region ?: ""),
                        "region_id" to (address.region?.regionId ?: address.regionId ?: 0),
                        "city" to (address.city ?: ""),
                        "street" to (address.street ?: emptyList<String>())
                    )
                )
            )
            
            when (val result = repository.applyShippingProtection(request)) {
                is NetworkResult.Success -> {
                    result.data?.let {
                        _orderTotalsState.value = UiState.Success(it)
                        updateProtectionState(it)
                    }
                }
                is NetworkResult.Error -> _orderTotalsState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> _cartItems.value = result.data ?: emptyList()
                else -> {}
            }
        }
    }

    /** Qty stepper on an order-summary item. Min qty is 1 — use the trash icon to remove. */
    fun updateItemQty(item: CartItemDetail, newQty: Int) {
        val itemId = item.itemId ?: return
        if (newQty < 1 || _cartItemUpdating.value) return
        viewModelScope.launch {
            _cartItemUpdating.value = true
            when (cartRepository.updateCartItem(itemId, newQty)) {
                is NetworkResult.Success -> {
                    loadCartItems()
                    refreshTotals()
                }
                else -> {}
            }
            _cartItemUpdating.value = false
        }
    }

    fun removeItem(item: CartItemDetail) {
        val itemId = item.itemId ?: return
        if (_cartItemUpdating.value) return
        viewModelScope.launch {
            _cartItemUpdating.value = true
            when (cartRepository.removeCartItem(itemId)) {
                is NetworkResult.Success -> {
                    loadCartItems()
                    refreshTotals()
                }
                else -> {}
            }
            _cartItemUpdating.value = false
        }
    }

    fun updateCouponCode(code: String) { _couponCode.value = code }

    fun updateRewardsInput(value: String) {
        // digits only — points are integers
        _rewardsInput.value = value.filter { it.isDigit() }
    }

    fun applyCoupon() {
        val code = _couponCode.value.trim()
        if (code.isBlank()) return
        viewModelScope.launch {
            _couponState.value = UiState.Loading
            when (val result = repository.applyCoupon(code)) {
                is NetworkResult.Success -> {
                    _couponState.value = UiState.Success(true)
                    _appliedCoupon.value = code
                    refreshTotals()
                }
                is NetworkResult.Error -> _couponState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun removeCoupon() {
        viewModelScope.launch {
            _couponState.value = UiState.Loading
            when (val result = repository.removeCoupon()) {
                is NetworkResult.Success -> {
                    _couponState.value = UiState.Idle
                    _appliedCoupon.value = ""
                    _couponCode.value = ""
                    refreshTotals()
                }
                is NetworkResult.Error -> _couponState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun applyRewards(points: Int) {
        viewModelScope.launch {
            _rewardsState.value = UiState.Loading
            when (val result = repository.applyRewardPoints(points)) {
                is NetworkResult.Success -> {
                    _rewardsState.value = UiState.Success(true)
                    _rewardsApplied.value = true
                    refreshTotals()
                }
                is NetworkResult.Error -> _rewardsState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun removeRewards() {
        viewModelScope.launch {
            _rewardsState.value = UiState.Loading
            when (val result = repository.removeRewardPoints()) {
                is NetworkResult.Success -> {
                    _rewardsState.value = UiState.Idle
                    _rewardsApplied.value = false
                    refreshTotals()
                }
                is NetworkResult.Error -> _rewardsState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    private fun refreshTotals() {
        val address = _selectedAddress.value ?: return
        val method = _selectedShippingMethod.value ?: return
        saveShippingInfo(address, method)
    }

    fun resetCouponState() { _couponState.value = UiState.Idle }
}

/**
 * Convert CustomerAddressModel → ShippingEstimateAddress
 * Matches iOS convertToAddressDict()
 */
private fun CustomerAddressModel.toShippingEstimateAddress(): ShippingEstimateAddress {
    return ShippingEstimateAddress(
        region = region?.region ?: "",
        regionId = region?.regionId ?: regionId ?: 0,
        regionCode = region?.regionCode ?: "",
        countryId = countryId ?: "US",
        street = street ?: emptyList(),
        postcode = postcode ?: "",
        city = city ?: "",
        firstName = firstname ?: "",
        lastName = lastname ?: "",
        email = "",
        telephone = telephone ?: ""
    )
}
