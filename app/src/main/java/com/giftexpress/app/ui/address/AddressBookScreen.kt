package com.giftexpress.app.ui.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.Address
import com.giftexpress.app.data.model.AddressType
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun AddressBookScreen(
    onBackClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (Address) -> Unit,
    onDeleteAddressClick: (Address) -> Unit
) {
    // Mock Data
    val billingAddress = Address(
        id = "1",
        firstName = "Harmandeep",
        lastName = "Singh",
        streetAddress = "3775 Park Avenue",
        city = "Edison",
        state = "New Jersey",
        zipCode = "8820",
        country = "United States",
        phone = "7328902046",
        type = AddressType.BILLING
    )

    val shippingAddress = Address(
        id = "2",
        firstName = "Harmandeep",
        lastName = "Singh",
        streetAddress = "3775 Park Avenue",
        city = "Edison",
        state = "New Jersey",
        zipCode = "8820",
        country = "United States",
        phone = "7328902046",
        type = AddressType.SHIPPING
    )

    Scaffold(
        topBar = {
            AddressBookTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            Button(
                onClick = onAddAddressClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD NEW ADDRESS",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "DEFAULT BILLING ADDRESS",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                AddressCard(
                    address = billingAddress,
                    onEditClick = { onEditAddressClick(billingAddress) },
                    onDeleteClick = { onDeleteAddressClick(billingAddress) }
                )
            }

            item {
                Text(
                    text = "DEFAULT SHIPPING ADDRESS",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                AddressCard(
                    address = shippingAddress,
                    onEditClick = { onEditAddressClick(shippingAddress) },
                    onDeleteClick = { onDeleteAddressClick(shippingAddress) }
                )
            }
        }
    }
}

@Composable
fun AddressBookTopBar(onBackClick: () -> Unit) {
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
            text = "Address Book",
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun AddressCard(
    address: Address,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address.fullName,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = address.streetAddress,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "${address.city}, ${address.state}, ${address.zipCode}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = address.country,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "T: ${address.phone}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            
            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddressBookScreen() {
    AddressBookScreen(
        onBackClick = {},
        onAddAddressClick = {},
        onEditAddressClick = {},
        onDeleteAddressClick = {}
    )
}
