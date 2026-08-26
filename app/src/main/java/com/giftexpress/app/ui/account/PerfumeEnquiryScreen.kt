package com.giftexpress.app.ui.account

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.EnquiryRequest
import com.giftexpress.app.ui.components.AppTextField
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun PerfumeEnquiryScreen(
    viewModel: ContactUsViewModel,
    onBackClick: () -> Unit
) {
    val enquiryState by viewModel.enquiryState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var perfumeName by remember { mutableStateOf("") }
    var referenceUrl by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(enquiryState) {
        if (enquiryState is UiState.Success) {
            showSuccess = true
            viewModel.resetEnquiryState()
            name = ""; email = ""; phone = ""; perfumeName = ""; referenceUrl = ""; message = ""
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Perfume Enquiry",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && perfumeName.isNotBlank()) {
                        viewModel.submitEnquiry(
                            EnquiryRequest(
                                name = name,
                                email = email,
                                phone = phone,
                                perfumeName = perfumeName,
                                referenceUrl = referenceUrl,
                                message = message
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(4.dp),
                enabled = enquiryState !is UiState.Loading
            ) {
                if (enquiryState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "SUBMIT ENQUIRY",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (showSuccess) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Your enquiry has been submitted successfully! We'll get back to you soon.",
                            fontFamily = Gilroy,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (enquiryState is UiState.Error) {
                item {
                    Text(
                        text = (enquiryState as UiState.Error).message,
                        fontFamily = Gilroy,
                        fontSize = 13.sp,
                        color = Color(0xFFC62828)
                    )
                }
            }

            item {
                Text(
                    text = "Can't find a perfume? Let us know and we'll try to source it for you.",
                    fontFamily = Gilroy,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }

            item { AppTextField(label = "Name *", value = name, onValueChange = { name = it }, isRequired = true) }
            item { AppTextField(label = "Email *", value = email, onValueChange = { email = it }, isRequired = true) }
            item { AppTextField(label = "Phone", value = phone, onValueChange = { phone = it }) }
            item { AppTextField(label = "Perfume Name *", value = perfumeName, onValueChange = { perfumeName = it }, isRequired = true) }
            item { AppTextField(label = "Reference URL", value = referenceUrl, onValueChange = { referenceUrl = it }) }
            item {
                Column {
                    Text(
                        text = "Message",
                        fontFamily = Gilroy,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        textStyle = TextStyle(fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black),
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray.copy(0.5f)
                        ),
                        minLines = 4
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
