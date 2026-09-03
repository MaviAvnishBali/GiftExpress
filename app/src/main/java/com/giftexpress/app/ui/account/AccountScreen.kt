package com.giftexpress.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToAddressBook: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToRewardPoints: () -> Unit,
    onNavigateToRewardHistory: () -> Unit,
    onNavigateToAboutUs: () -> Unit,
    onNavigateToShippingInfo: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsConditions: () -> Unit,
    onNavigateToContactUs: () -> Unit = {},
    onNavigateToPerfumeEnquiry: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val logoutState by viewModel.logoutState.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoggedIn = user != null

    Scaffold(
        bottomBar = {
            Button(
                onClick = { if (isLoggedIn) viewModel.logout() else onNavigateToLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoggedIn) colorResource(id = R.color.error) else Color(0xFF333333)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isLoggedIn) R.drawable.ic_power else R.drawable.ic_profile),
                    contentDescription = if (isLoggedIn) "Logout" else "Login",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoggedIn) "LOGOUT" else "LOGIN",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Top Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AccountOptionCard(
                        iconRes = R.drawable.ic_box,
                        text = "My Orders",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToOrders
                    )
                    AccountOptionCard(
                        iconRes = R.drawable.ic_heart,
                        text = "My Wish List",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWishlist
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Account Settings Header
            item {
                Text(
                    text = "ACCOUNT SETTING",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Settings List
            item {
                AccountSettingItem(
                    iconRes = R.drawable.ic_location,
                    text = "Address Book",
                    onClick = onNavigateToAddressBook
                )
                AccountSettingItem(
                    iconRes = R.drawable.ic_info,
                    text = "Account Information",
                    onClick = onNavigateToAccountInfo
                )
                AccountSettingItem(
                    iconRes = R.drawable.ic_reward,
                    text = "My Reward Points",
                    onClick = onNavigateToRewardPoints
                )
                AccountSettingItem(
                    iconRes = R.drawable.ic_document,
                    text = "Reward Points History",
                    onClick = onNavigateToRewardHistory
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "CUSTOMER SERVICES",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Customer Services List
            item {
                CustomerServiceItem(text = "About Us", onClick = onNavigateToAboutUs)
                CustomerServiceItem(text = "Shipping Information", onClick = onNavigateToShippingInfo)
                CustomerServiceItem(text = "Privacy Policy", onClick = onNavigateToPrivacyPolicy)
                CustomerServiceItem(text = "Terms & Condition", onClick = onNavigateToTermsConditions)
                CustomerServiceItem(text = "Contact Us", onClick = onNavigateToContactUs)
                CustomerServiceItem(text = "Perfume Enquiry", onClick = onNavigateToPerfumeEnquiry)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOptionCard(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AccountSettingItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF1976D2) // Blue color from image
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Black
        )
    }
    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
}

@Composable
fun CustomerServiceItem(
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}
