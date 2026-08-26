package com.giftexpress.app.ui.address

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.*
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun AddressFormScreen(
    existingAddress: CustomerAddressModel? = null,
    viewModel: AddressBookViewModel? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val isEditMode = existingAddress != null
    val saveState by viewModel?.saveState?.collectAsState() ?: remember { mutableStateOf<UiState<CustomerAddressModel>>(UiState.Idle) }
    val countriesState by viewModel?.countriesState?.collectAsState() ?: remember { mutableStateOf<UiState<List<CountryModel>>>(UiState.Idle) }
    val addressesState by viewModel?.addressesState?.collectAsState() ?: remember { mutableStateOf<UiState<List<CustomerAddressModel>>>(UiState.Idle) }

    val existingAddressCount = (addressesState as? UiState.Success)?.data?.size ?: 1

    val isSaving = saveState is UiState.Loading

    // Fetch countries list on enter (matches iOS getCountryList)
    LaunchedEffect(Unit) {
        viewModel?.fetchCountries()
    }

    var firstName by remember(existingAddress) { mutableStateOf(existingAddress?.firstname ?: "") }
    var lastName by remember(existingAddress) { mutableStateOf(existingAddress?.lastname ?: "") }
    var phone by remember(existingAddress) { mutableStateOf(existingAddress?.telephone ?: "") }
    var company by remember(existingAddress) { mutableStateOf("") } // Company optional field
    var streetLine1 by remember(existingAddress) { mutableStateOf(existingAddress?.street?.getOrNull(0) ?: "") }
    var streetLine2 by remember(existingAddress) { mutableStateOf(existingAddress?.street?.getOrNull(1) ?: "") }
    var streetLine3 by remember(existingAddress) { mutableStateOf(existingAddress?.street?.getOrNull(2) ?: "") }
    var city by remember(existingAddress) { mutableStateOf(existingAddress?.city ?: "") }
    var postcode by remember(existingAddress) { mutableStateOf(existingAddress?.postcode ?: "") }

    // Country & State selections
    var selectedCountry by remember { mutableStateOf<CountryModel?>(null) }
    var selectedRegion by remember { mutableStateOf<RegionModel?>(null) }
    var customRegionName by remember(existingAddress) { mutableStateOf(existingAddress?.region?.region ?: "") }

    // Dropdown Dialog controls
    var showCountryDialog by remember { mutableStateOf(false) }
    var showStateDialog by remember { mutableStateOf(false) }

    // Checkbox defaults — matching iOS lines 78-88 and 133-145
    val initialBillingDefault = when {
        existingAddressCount == 0 && !isEditMode -> true
        isEditMode -> existingAddress?.defaultBilling == true
        else -> true
    }
    val initialShippingDefault = when {
        existingAddressCount == 0 && !isEditMode -> true
        isEditMode -> existingAddress?.defaultShipping == true
        else -> true
    }

    var isDefaultBilling by remember(existingAddress) { mutableStateOf(initialBillingDefault) }
    var isDefaultShipping by remember(existingAddress) { mutableStateOf(initialShippingDefault) }

    // Disable checkboxes if 1st address OR if both defaults are already set on edit (matches iOS lines 83-84, 134-135)
    val areCheckboxesDisabled = (existingAddressCount == 0 && !isEditMode) ||
            (isEditMode && existingAddress != null && (existingAddress.defaultBilling == true && existingAddress.defaultShipping == true))

    // Automatically initialize country & state list when countries API returns (matches iOS setDefaultCountryForNewAddress / setCountryAndStateForEdit)
    LaunchedEffect(countriesState) {
        if (countriesState is UiState.Success) {
            val countryList = (countriesState as UiState.Success<List<CountryModel>>).data
            if (isEditMode && existingAddress != null) {
                val foundCountry = countryList.firstOrNull { it.id.equals(existingAddress.countryId, ignoreCase = true) }
                selectedCountry = foundCountry
                if (foundCountry?.availableRegions != null) {
                    val foundRegion = foundCountry.availableRegions.firstOrNull {
                        it.id == existingAddress.regionId?.toString() ||
                                it.name.equals(existingAddress.region?.region, ignoreCase = true) ||
                                it.code.equals(existingAddress.region?.regionCode, ignoreCase = true)
                    }
                    selectedRegion = foundRegion
                    if (foundRegion == null && !existingAddress.region?.region.isNullOrBlank()) {
                        customRegionName = existingAddress.region?.region ?: ""
                    }
                }
            } else if (selectedCountry == null) {
                // Matches iOS setDefaultCountryForNewAddress — find United States
                val usCountry = countryList.firstOrNull {
                    it.id.equals("US", ignoreCase = true) || it.displayName.lowercase().contains("united states")
                } ?: countryList.firstOrNull()

                selectedCountry = usCountry
                if (usCountry?.availableRegions?.isNotEmpty() == true) {
                    selectedRegion = usCountry.availableRegions.firstOrNull()
                }
            }
        }
    }



    // Country Picker Dialog
    if (showCountryDialog && countriesState is UiState.Success) {
        val countries = (countriesState as UiState.Success<List<CountryModel>>).data
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            title = { Text("Select Country", fontFamily = Gilroy, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(countries) { country ->
                        Text(
                            text = country.displayName,
                            fontFamily = Gilroy,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = country
                                    selectedRegion = country.availableRegions?.firstOrNull()
                                    customRegionName = ""
                                    showCountryDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        )
                        Divider(color = Color(0xFFEEEEEE))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryDialog = false }) {
                    Text("CANCEL", fontFamily = Gilroy, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        )
    }

    // State / Region Picker Dialog
    if (showStateDialog && selectedCountry?.availableRegions?.isNotEmpty() == true) {
        val regions = selectedCountry?.availableRegions ?: emptyList()
        AlertDialog(
            onDismissRequest = { showStateDialog = false },
            title = { Text("Select State / Province", fontFamily = Gilroy, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(regions) { region ->
                        Text(
                            text = region.displayName,
                            fontFamily = Gilroy,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRegion = region
                                    customRegionName = region.displayName
                                    showStateDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        )
                        Divider(color = Color(0xFFEEEEEE))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStateDialog = false }) {
                    Text("CANCEL", fontFamily = Gilroy, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back", tint = Color.Black)
                }
                Text(
                    text = if (isEditMode) "Edit Address" else "Add New Address",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    // Matches iOS validateAddressForm logic (lines 584-649)
                    val fn = firstName.trim()
                    val ln = lastName.trim()
                    val ph = phone.trim()
                    val st1 = streetLine1.trim()
                    val zp = postcode.trim()
                    val ct = city.trim()
                    val cntryId = selectedCountry?.id ?: ""
                    val stName = selectedRegion?.displayName ?: customRegionName.trim()

                    if (fn.isEmpty()) {
                        Toast.makeText(context, "Please enter First Name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (ln.isEmpty()) {
                        Toast.makeText(context, "Please enter Last Name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (ph.isEmpty()) {
                        Toast.makeText(context, "Please enter Phone Number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (ph.length < 8 || ph.length > 15 || !ph.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }) {
                        Toast.makeText(context, "Please enter a valid Phone Number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (st1.isEmpty()) {
                        Toast.makeText(context, "Please enter Street Address", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (zp.isEmpty()) {
                        Toast.makeText(context, "Please enter Zip / Postal Code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (zp.length < 4 || zp.length > 10) {
                        Toast.makeText(context, "Please enter a valid Zip / Postal Code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (ct.isEmpty()) {
                        Toast.makeText(context, "Please enter City", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (stName.isEmpty()) {
                        Toast.makeText(context, "Please select State", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (cntryId.isEmpty()) {
                        Toast.makeText(context, "Please select Country", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Build street array — matches iOS: [street1, street2, street3]
                    val street = listOfNotNull(
                        st1,
                        streetLine2.trim().takeIf { it.isNotEmpty() },
                        streetLine3.trim().takeIf { it.isNotEmpty() }
                    )

                    val regId = selectedRegion?.id?.toIntOrNull() ?: existingAddress?.regionId ?: 0

                    val request = SaveAddressRequest(
                        address = AddressBody(
                            firstname = fn,
                            lastname = ln,
                            street = street,
                            city = ct,
                            countryId = cntryId,
                            region = AddressRegionBody(
                                region = stName,
                                regionId = regId
                            ),
                            postcode = zp,
                            telephone = ph,
                            defaultBilling = isDefaultBilling,
                            defaultShipping = isDefaultShipping
                        )
                    )
                    viewModel?.saveAddress(request, existingAddress?.id)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isEditMode) "UPDATE ADDRESS" else "ADD ADDRESS",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Fields in iOS order (lines 249-278):
            // 1. First Name*
            item { FormFieldLabel("First Name*") }
            item { FormTextField(value = firstName, onValueChange = { firstName = it }, placeholder = "First Name") }

            // 2. Last Name*
            item { FormFieldLabel("Last Name*") }
            item { FormTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Last Name") }

            // 3. Street Address* (1, 2, 3)
            item { FormFieldLabel("Street Address*") }
            item { FormTextField(value = streetLine1, onValueChange = { streetLine1 = it }, placeholder = "Street Address 1") }
            item { FormTextField(value = streetLine2, onValueChange = { streetLine2 = it }, placeholder = "Street Address 2 (optional)") }
            item { FormTextField(value = streetLine3, onValueChange = { streetLine3 = it }, placeholder = "Street Address 3 (optional)") }

            // 4. Country* (Dropdown Picker - matches iOS line 260)
            item { FormFieldLabel("Country*") }
            item {
                DropdownField(
                    value = selectedCountry?.displayName ?: "Select Country",
                    onClick = { showCountryDialog = true }
                )
            }

            // 5. City* (matches iOS line 264)
            item { FormFieldLabel("City*") }
            item { FormTextField(value = city, onValueChange = { city = it }, placeholder = "City") }

            // 6. Zip / Postal Code* (matches iOS line 267)
            item { FormFieldLabel("Zip / Postal Code*") }
            item {
                FormTextField(
                    value = postcode,
                    onValueChange = { postcode = it },
                    placeholder = "Zip / Postal Code",
                    keyboardType = KeyboardType.Number
                )
            }

            // 7. State / Province* (Dropdown or Text - matches iOS line 270)
            item { FormFieldLabel("State / Province*") }
            item {
                if (selectedCountry?.availableRegions?.isNotEmpty() == true) {
                    DropdownField(
                        value = selectedRegion?.displayName ?: "Select State",
                        onClick = { showStateDialog = true }
                    )
                } else {
                    FormTextField(
                        value = customRegionName,
                        onValueChange = { customRegionName = it },
                        placeholder = "State / Province"
                    )
                }
            }

            // 8. Company (Optional - matches iOS line 274)
            item { FormFieldLabel("Company") }
            item { FormTextField(value = company, onValueChange = { company = it }, placeholder = "Company Name") }

            // 9. Phone Number* (matches iOS line 277)
            item { FormFieldLabel("Phone Number*") }
            item {
                FormTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Phone Number",
                    keyboardType = KeyboardType.Phone
                )
            }

            // 10 & 11. Default Billing / Shipping Checkboxes (matches iOS lines 302-340)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AddressCheckboxRow(
                        title = "Use as my default billing address",
                        isChecked = isDefaultBilling,
                        isDisabled = areCheckboxesDisabled,
                        onToggle = { if (!areCheckboxesDisabled) isDefaultBilling = !isDefaultBilling }
                    )
                    AddressCheckboxRow(
                        title = "Use as my default shipping address",
                        isChecked = isDefaultShipping,
                        isDisabled = areCheckboxesDisabled,
                        onToggle = { if (!areCheckboxesDisabled) isDefaultShipping = !isDefaultShipping }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FormFieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = Gilroy,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Color(0xFF333333)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontFamily = Gilroy, color = Color.Gray, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF333333),
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun DropdownField(
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value, fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black)
            Text(text = "▼", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun AddressCheckboxRow(
    title: String,
    isChecked: Boolean,
    isDisabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDisabled) 0.6f else 1.0f)
            .clickable(enabled = !isDisabled, onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (isChecked) Color(0xFF2E7D32) else Color.Transparent, RoundedCornerShape(3.dp))
                .border(1.5.dp, if (isChecked) Color(0xFF2E7D32) else Color.Gray, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(text = title, fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black)
    }
}
