package com.giftexpress.app.ui.account

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
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
import androidx.compose.ui.platform.LocalContext
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
import com.giftexpress.app.utils.UiState
import java.util.Calendar
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AccountInfoScreen(
    viewModel: AccountViewModel? = null,
    userData: CustomerDetailsResponse? = null,
    onBackClick: () -> Unit,
    onSaveClick: (CustomerDetailsResponse) -> Unit,
    onChangePasswordClick: () -> Unit
) {
    val customerDetails: CustomerDetailsResponse? = viewModel?.customerDetails?.collectAsState()?.value ?: userData
    val isSaving: Boolean = viewModel?.updateProfileState?.collectAsState()?.value is UiState.Loading

    var firstName by remember(customerDetails) { mutableStateOf(customerDetails?.firstName ?: "") }
    var lastName by remember(customerDetails) { mutableStateOf(customerDetails?.lastName ?: "") }
    var dateOfBirth by remember(customerDetails) { mutableStateOf(customerDetails?.dob ?: "") }
    var email by remember(customerDetails) { mutableStateOf(customerDetails?.email ?: "") }
    
    val context = LocalContext.current
    var isChangePasswordChecked by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isEmailEditable by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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
                    viewModel?.updateProfile(firstName, lastName, email, dateOfBirth)
                },
                enabled = !isSaving,
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


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
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dateOfBirth = "%04d-%02d-%02d".format(year, month + 1, day)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "Select Date",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black
                            )
                        }
                    }
                )
            }

            item {
                AppTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    isRequired = true,
                    readOnly = !isEmailEditable,
                    trailingIcon = {
                        IconButton(onClick = { isEmailEditable = !isEmailEditable }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Email",
                                modifier = Modifier.size(20.dp),
                                tint = if (isEmailEditable) Color(0xFFF57C00) else Color.Black
                            )
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChangePasswordChecked = !isChangePasswordChecked }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isChangePasswordChecked) Color(0xFFF57C00) else Color.White)
                            .border(1.dp, if (isChangePasswordChecked) Color(0xFFF57C00) else Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChangePasswordChecked) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Change Password",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            if (isChangePasswordChecked) {
                item {
                    AppTextField(
                        label = "Current Password",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        isRequired = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    painter = painterResource(id = if (currentPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                }
                item {
                    AppTextField(
                        label = "New Password",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        isRequired = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    painter = painterResource(id = if (newPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                }
                item {
                    AppTextField(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isRequired = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(id = if (confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
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
