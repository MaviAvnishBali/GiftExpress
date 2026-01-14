package com.giftexpress.app.data.model

data class Address(
    val id: String,
    val firstName: String,
    val lastName: String,
    val company: String? = null,
    val streetAddress: String,
    val streetAddress2: String? = null,
    val streetAddress3: String? = null,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val phone: String,
    val type: AddressType
) {
    val fullName: String
        get() = "$firstName $lastName"
}

enum class AddressType {
    BILLING, SHIPPING, OTHER
}
