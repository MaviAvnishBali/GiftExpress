package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("customer")
    val customer: CustomerData,
    @SerializedName("password")
    val password: String
)

data class CustomerData(
    @SerializedName("email")
    val email: String,
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("dob")
    val dob: String,
    @SerializedName("store_id")
    val storeId: Int = 1,
    @SerializedName("website_id")
    val websiteId: Int = 1,
    @SerializedName("extension_attributes")
    val extensionAttributes: ExtensionAttributes = ExtensionAttributes()
)

data class ExtensionAttributes(
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean = false
)
