package com.giftexpress.app.ui.account

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.CmsPage
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun ContactUsScreen(
    viewModel: ContactUsViewModel,
    onBackClick: () -> Unit
) {
    val contactState by viewModel.contactState.collectAsState()
    val context = LocalContext.current

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
                    text = "Contact Us",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = contactState) {
                is UiState.Loading -> Box(Modifier.fillMaxSize().shimmerEffect())
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Gray)
                    }
                }
                is UiState.Success -> ContactUsContent(
                    page = state.data,
                    onPhoneClick = { phone ->
                        val clean = phone.replace(" ", "").replace("-", "")
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
                        context.startActivity(intent)
                    },
                    onEmailClick = { email ->
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                        context.startActivity(intent)
                    }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun ContactUsContent(
    page: CmsPage,
    onPhoneClick: (String) -> Unit,
    onEmailClick: (String) -> Unit
) {
    val content = page.content ?: ""

    val phones = remember(content) { extractPhones(content) }
    val emails = remember(content) { extractEmails(content) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray.copy(0.4f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (phones.isNotEmpty()) {
                        Text(
                            text = "Phone",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(12.dp))
                        phones.forEach { phone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPhoneClick(phone) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = phone,
                                    fontFamily = Gilroy,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    if (emails.isNotEmpty()) {
                        Text(
                            text = "Email",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(12.dp))
                        emails.forEach { email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEmailClick(email) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = email,
                                    fontFamily = Gilroy,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }

                    if (phones.isEmpty() && emails.isEmpty()) {
                        Text(
                            text = content,
                            fontFamily = Gilroy,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

private fun extractPhones(content: String): List<String> {
    val pattern = Regex("""(\+[\d\s\-]{7,})""")
    return pattern.findAll(content)
        .map { it.value.trim() }
        .distinct()
        .toList()
}

private fun extractEmails(content: String): List<String> {
    val pattern = Regex("""[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}""")
    return pattern.findAll(content)
        .map { it.value }
        .distinct()
        .toList()
}
