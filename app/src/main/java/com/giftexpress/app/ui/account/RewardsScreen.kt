package com.giftexpress.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun RewardsScreen(
    onBackClick: () -> Unit
) {
    var isEmailNotificationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF333333),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "My Rewards",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_rewards), // Using ic_reward as placeholder for the illustration
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Balance Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "3,781",
                        fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
                        fontSize = 64.sp,
                        color = Color.Black,
                        lineHeight = 64.sp
                    )
                    Text(
                        text = "Current Balance",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(100.dp)
                        .border(BorderStroke(0.5.dp, Color.LightGray), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    RewardStatItem(
                        value = "1440",
                        label = "Total Earned",
                        valueColor = Color(0xFF2E7D32),
                        bgColor = Color(0xFFF1F8E9),
                        modifier = Modifier.weight(1f)
                    )
                    Divider(modifier = Modifier.fillMaxHeight().width(0.5.dp), color = Color.LightGray)
                    RewardStatItem(
                        value = "189",
                        label = "Total Spent",
                        valueColor = Color(0xFF9E9D24),
                        bgColor = Color(0xFFF9FBE7),
                        modifier = Modifier.weight(1f)
                    )
                    Divider(modifier = Modifier.fillMaxHeight().width(0.5.dp), color = Color.LightGray)
                    RewardStatItem(
                        value = "2300",
                        label = "Expired",
                        valueColor = Color(0xFFC62828),
                        bgColor = Color(0xFFFBE9E7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Notification Options
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 24.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    
                    Text(
                        text = "Notification options",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEmailNotificationEnabled = !isEmailNotificationEnabled },
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isEmailNotificationEnabled) Color(0xFFF57C00) else Color.White)
                                .border(1.dp, if (isEmailNotificationEnabled) Color(0xFFF57C00) else Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isEmailNotificationEnabled) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Receive emails when reward points are added to the balance",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.Black,
                            lineHeight = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    OutlinedButton(
                        onClick = { /* Handle save */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Text(
                            text = "SAVE NOTIFICATION OPTIONS",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardStatItem(
    value: String,
    label: String,
    valueColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = valueColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = Gilroy,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun PreviewRewardsScreen() {
    RewardsScreen(onBackClick = {})
}
