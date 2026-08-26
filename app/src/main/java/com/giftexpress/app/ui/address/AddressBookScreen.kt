package com.giftexpress.app.ui.address

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun AddressBookScreen(
    viewModel: AddressBookViewModel,
    isSelectionMode: Boolean = false,
    onBackClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (CustomerAddressModel) -> Unit,
    onAddressClick: ((CustomerAddressModel) -> Unit)? = null
) {
    val addressesState by viewModel.addressesState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<CustomerAddressModel?>(null) }

    // Reload after delete
    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) viewModel.resetDeleteState()
        if (deleteState is UiState.Error) viewModel.resetDeleteState()
    }

    // Delete confirmation dialog — matches iOS UIAlertController
    showDeleteDialog?.let { address ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Address", fontFamily = FontFamily(Font(R.font.gilroy_bold))) },
            text = { Text("Are you sure you want to delete this address?", fontFamily = Gilroy) },
            confirmButton = {
                TextButton(onClick = {
                    address.id?.let { viewModel.deleteAddress(it) }
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = Color(0xFFC62828), fontFamily = FontFamily(Font(R.font.gilroy_bold)))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", fontFamily = Gilroy)
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
                    text = "Address Book",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        bottomBar = {
            Button(
                onClick = onAddAddressClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ADD NEW ADDRESS",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = addressesState) {
                is UiState.Loading -> AddressShimmer()

                is UiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message, fontFamily = Gilroy, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadAddresses() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }

                is UiState.Success -> {
                    val addresses = state.data

                    // Classify — matches iOS classifyAddresses()
                    val defaultBilling = addresses.firstOrNull { it.defaultBilling == true }
                    var defaultShipping = addresses.firstOrNull { it.defaultShipping == true }
                    if (defaultShipping == null) defaultShipping = defaultBilling
                    val others = addresses.filter { it.defaultBilling != true && it.defaultShipping != true }

                    if (addresses.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No addresses saved.\nTap + to add one.", fontFamily = Gilroy, color = Color.Gray, lineHeight = 22.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            // DEFAULT BILLING ADDRESS
                            defaultBilling?.let { addr ->
                                item {
                                    AddressSectionHeader("DEFAULT BILLING ADDRESS")
                                    Spacer(Modifier.height(8.dp))
                                    AddressCard(
                                        address = addr,
                                        showDelete = false,
                                        onEditClick = { onEditAddressClick(addr) },
                                        onDeleteClick = {},
                                        onClick = { onAddressClick?.invoke(addr) }
                                    )
                                }
                            }

                            // DEFAULT SHIPPING ADDRESS (only if different from billing)
                            if (defaultShipping != null && defaultShipping.id != defaultBilling?.id) {
                                item {
                                    AddressSectionHeader("DEFAULT SHIPPING ADDRESS")
                                    Spacer(Modifier.height(8.dp))
                                    AddressCard(
                                        address = defaultShipping!!,
                                        showDelete = false,
                                        onEditClick = { onEditAddressClick(defaultShipping!!) },
                                        onDeleteClick = {},
                                        onClick = { onAddressClick?.invoke(defaultShipping!!) }
                                    )
                                }
                            }

                            // ADDRESS BOOK (other addresses)
                            if (others.isNotEmpty()) {
                                item {
                                    AddressSectionHeader("ADDRESS BOOK")
                                    Spacer(Modifier.height(8.dp))
                                }
                                items(others) { addr ->
                                    AddressCard(
                                        address = addr,
                                        showDelete = !isSelectionMode,
                                        onEditClick = { onEditAddressClick(addr) },
                                        onDeleteClick = { showDeleteDialog = addr },
                                        onClick = { onAddressClick?.invoke(addr) }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun AddressSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
        fontSize = 20.sp,
        color = Color.Black
    )
}

/**
 * Matches iOS AddressListTableViewCell — name, address, tel, edit/delete
 */
@Composable
fun AddressCard(
    address: CustomerAddressModel,
    showDelete: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FC)),
        modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.LightGray.copy(0.3f), RoundedCornerShape(8.dp)).then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Name — matches iOS lblName
                Text(
                    text = address.fullName,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 15.sp,
                    color = Color.Black
                )
                // Address — matches iOS lblAddress: "street\ncity, region postcode\ncountry"
                Text(
                    text = address.formattedAddress,
                    fontFamily = Gilroy,
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    lineHeight = 18.sp
                )
                // Tel — matches iOS lblTel: "Tel: telephone"
                address.telephone?.takeIf { it.isNotBlank() }?.let { tel ->
                    Text(
                        text = "Tel: $tel",
                        fontFamily = Gilroy,
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                }
            }

            // Edit + Delete buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                }
                if (showDelete) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressShimmer() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) {
            Box(Modifier.fillMaxWidth().height(100.dp).shimmerEffect())
        }
    }
}
