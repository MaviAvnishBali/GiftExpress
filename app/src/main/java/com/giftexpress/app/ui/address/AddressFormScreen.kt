package com.giftexpress.app.ui.address

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.Address
import com.giftexpress.app.data.model.AddressType
import com.giftexpress.app.ui.components.AppDropdown
import com.giftexpress.app.ui.components.AppTextField
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun AddressFormScreen(
    existingAddress: Address? = null,
    onBackClick: () -> Unit,
    onSaveAddressClick: (Address) -> Unit
) {
    val isEditMode = existingAddress != null
    
    var firstName by remember { mutableStateOf(existingAddress?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existingAddress?.lastName ?: "") }
    var company by remember { mutableStateOf(existingAddress?.company ?: "") }
    var phone by remember { mutableStateOf(existingAddress?.phone ?: "") }
    var streetAddress by remember { mutableStateOf(existingAddress?.streetAddress ?: "") }
    var zipCode by remember { mutableStateOf(existingAddress?.zipCode ?: "") }
    var streetAddress2 by remember { mutableStateOf(existingAddress?.streetAddress2 ?: "") }
    var streetAddress3 by remember { mutableStateOf(existingAddress?.streetAddress3 ?: "") }
    var city by remember { mutableStateOf(existingAddress?.city ?: "") }
    var state by remember { mutableStateOf(existingAddress?.state ?: "") }
    var country by remember { mutableStateOf(existingAddress?.country ?: "") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = if (isEditMode) "Edit Address Book" else "Add New Address",
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
                    val newAddress = Address(
                        id = existingAddress?.id ?: "", // Should generate ID if new
                        firstName = firstName,
                        lastName = lastName,
                        company = company.ifEmpty { null },
                        streetAddress = streetAddress,
                        streetAddress2 = streetAddress2.ifEmpty { null },
                        streetAddress3 = streetAddress3.ifEmpty { null },
                        city = city,
                        state = state,
                        zipCode = zipCode,
                        country = country,
                        phone = phone,
                        type = existingAddress?.type ?: AddressType.OTHER // Default type
                    )
                    onSaveAddressClick(newAddress)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (isEditMode) "SAVE ADDRESS" else "ADD ADDRESS",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Contact Information",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                AppTextField(
                    label = "First Name",
                    value = firstName,
                    onValueChange = { firstName = it },
                    isRequired = true
                )
            }
            item {
                AppTextField(
                    label = "Last Name",
                    value = lastName,
                    onValueChange = { lastName = it },
                    isRequired = true
                )
            }
            item {
                AppTextField(
                    label = "Company",
                    value = company,
                    onValueChange = { company = it }
                )
            }
            item {
                AppTextField(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    isRequired = true
                )
            }
            item {
                Text(
                    text = "Address",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                AppTextField(
                    label = "Street Address",
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    isRequired = true
                )
            }
            item {
                AppTextField(
                    label = "Zip/Postal Code",
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    isRequired = true
                )
            }
            item {
                AppTextField(
                    label = "Street Address 2",
                    value = streetAddress2,
                    onValueChange = { streetAddress2 = it }
                )
            }
            item {
                AppTextField(
                    label = "Street Address 3",
                    value = streetAddress3,
                    onValueChange = { streetAddress3 = it }
                )
            }
            item {
                AppTextField(
                    label = "City",
                    value = city,
                    onValueChange = { city = it },
                    isRequired = true
                )
            }
            item {
                AppDropdown(
                    label = "State/Province",
                    value = state,
                    onValueChange = { state = it },
                    isRequired = true
                )
            }
            item {
                AppDropdown(
                    label = "Country",
                    value = country,
                    onValueChange = { country = it },
                    isRequired = true
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddressFormScreen() {
    AddressFormScreen(
        onBackClick = {},
        onSaveAddressClick = {}
    )
}
