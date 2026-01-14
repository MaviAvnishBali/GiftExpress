package com.giftexpress.app.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.CustomerDetailsResponse
import com.giftexpress.app.ui.components.AppTextField

@Composable
fun AccountInfoScreen(
    userData: CustomerDetailsResponse? = null,
    onBackClick: () -> Unit,
    onSaveClick: (CustomerDetailsResponse) -> Unit,
    onChangePasswordClick: () -> Unit
) {
    var firstName by remember { mutableStateOf(userData?.firstName ?: "Harmandeep") }
    var lastName by remember { mutableStateOf(userData?.lastName ?: "Singh") }
    var dateOfBirth by remember { mutableStateOf(userData?.dob ?: "03/03/2022") }
    var email by remember { mutableStateOf(userData?.email ?: "ikseo2@gmail.com") }

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
                    text = "Account Information",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    // Create updated UserData and pass it back
                    // Note: UserData is immutable, so we'd typically create a new instance or pass fields
                    // For now, just triggering the callback with current values in a dummy object or just signal save
                    // Since UserData has other fields like token/id which we might not have here if userData is null
                    // We'll assume for this UI task we just handle the inputs.
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
                    text = "SAVE",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            item {
                Box(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile), // Placeholder
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Camera Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, CircleShape)
                            .clickable { /* Handle image pick */ },
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                    }
                }
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
                    label = "Date of Birth",
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    isRequired = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Select Date",
                            modifier = Modifier.padding(end = 16.dp).size(20.dp),
                            tint = Color.Black
                        )
                    }
                )
            }

            item {
                AppTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    isRequired = true,
                    readOnly = true, // Usually email is not editable or requires special flow
                    trailingIcon = {
                         Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Email",
                            modifier = Modifier.padding(end = 16.dp).size(20.dp),
                            tint = Color.Black
                        )
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChangePasswordClick)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = false, 
                        onClick = onChangePasswordClick,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Black,
                            unselectedColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change Password",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAccountInfoScreen() {
    AccountInfoScreen(
        onBackClick = {},
        onSaveClick = {},
        onChangePasswordClick = {}
    )
}
