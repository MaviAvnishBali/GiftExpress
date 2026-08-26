package com.giftexpress.app.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Legacy model — used for checkout flow navigation args
@Parcelize
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
) : Parcelable {
    val fullName: String get() = "$firstName $lastName"
}

@Parcelize
enum class AddressType : Parcelable {
    BILLING, SHIPPING, OTHER
}

// Matches iOS CustomerAddress model — used for address book API
@Parcelize
data class CustomerAddressModel(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("customer_id") val customerId: Int? = null,
    @SerializedName("firstname") val firstname: String? = null,
    @SerializedName("lastname") val lastname: String? = null,
    @SerializedName("street") val street: List<String>? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: CustomerAddressRegion? = null,
    @SerializedName("region_id") val regionId: Int? = null,
    @SerializedName("country_id") val countryId: String? = null,
    @SerializedName("telephone") val telephone: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("default_shipping") val defaultShipping: Boolean? = null,
    @SerializedName("default_billing") val defaultBilling: Boolean? = null
) : Parcelable {
    val fullName: String get() = "${firstname ?: ""} ${lastname ?: ""}".trim()

    val formattedAddress: String get() = buildString {
        street?.joinToString(", ")?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
        val parts = listOfNotNull(city, region?.region, postcode).filter { it.isNotBlank() }
        if (parts.isNotEmpty()) appendLine(parts.joinToString(", "))
        countryId?.takeIf { it.isNotBlank() }?.let { append(it) }
    }.trimEnd()
}

@Parcelize
data class CustomerAddressRegion(
    @SerializedName("region_code") val regionCode: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("region_id") val regionId: Int? = null
) : Parcelable

// Matches iOS exactly:  { "address": { ... "region": { "region": "...", "region_id": ... } } }
data class SaveAddressRequest(
    @SerializedName("address") val address: AddressBody
)

data class AddressBody(
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("street") val street: List<String>,
    @SerializedName("city") val city: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("region") val region: AddressRegionBody,
    @SerializedName("postcode") val postcode: String,
    @SerializedName("telephone") val telephone: String,
    @SerializedName("default_billing") val defaultBilling: Boolean = false,
    @SerializedName("default_shipping") val defaultShipping: Boolean = false
)

data class AddressRegionBody(
    @SerializedName("region") val region: String,
    @SerializedName("region_id") val regionId: Int = 0
)

// Matches iOS Country & Region models for GET directory/countries
@Parcelize
data class CountryModel(
    @SerializedName("id") val id: String,
    @SerializedName("two_letter_abbreviation") val twoLetterAbbreviation: String? = null,
    @SerializedName("three_letter_abbreviation") val threeLetterAbbreviation: String? = null,
    @SerializedName("full_name_locale") val fullNameLocale: String? = null,
    @SerializedName("full_name_english") val fullNameEnglish: String? = null,
    @SerializedName("available_regions") val availableRegions: List<RegionModel>? = null
) : Parcelable {
    val displayName: String get() = fullNameEnglish?.takeIf { it.isNotBlank() } ?: fullNameLocale?.takeIf { it.isNotBlank() } ?: id
}

@Parcelize
data class RegionModel(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null
) : Parcelable {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: code?.takeIf { it.isNotBlank() } ?: id
}
