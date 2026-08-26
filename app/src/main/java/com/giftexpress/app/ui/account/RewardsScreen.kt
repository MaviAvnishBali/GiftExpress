package com.giftexpress.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.data.model.WalletSummary
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun RewardsScreen(
    viewModel: RewardsViewModel,
    onBackClick: () -> Unit
) {
    val walletState by viewModel.walletState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    var subscribeEarnings by remember { mutableStateOf(false) }
    var subscribeExpire by remember { mutableStateOf(false) }

    LaunchedEffect(walletState) {
        if (walletState is UiState.Success) {
            val wallet = (walletState as UiState.Success<WalletSummary>).data
            subscribeEarnings = wallet.subscribeEarnings
            subscribeExpire = wallet.subscribeExpire
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                android.widget.Toast.makeText(
                    context,
                    "Notification preferences saved",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
            }
            is UiState.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (saveState as UiState.Error).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF333333), contentColor = Color.White) {
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
        when (val state = walletState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(paddingValues).shimmerEffect()
            )
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadWallet() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            is UiState.Success -> {
                val wallet = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bg_rewards),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            Text(
                                text = wallet.balance.toString(),
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
                                value = wallet.totalEarned.toString(),
                                label = "Total Earned",
                                valueColor = Color(0xFF2E7D32),
                                bgColor = Color(0xFFF1F8E9),
                                modifier = Modifier.weight(1f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(0.5.dp), color = Color.LightGray)
                            RewardStatItem(
                                value = wallet.totalSpent.toString(),
                                label = "Total Spent",
                                valueColor = Color(0xFF9E9D24),
                                bgColor = Color(0xFFF9FBE7),
                                modifier = Modifier.weight(1f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(0.5.dp), color = Color.LightGray)
                            RewardStatItem(
                                value = wallet.expired.toString(),
                                label = "Expired",
                                valueColor = Color(0xFFC62828),
                                bgColor = Color(0xFFFBE9E7),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            Divider(modifier = Modifier.padding(bottom = 24.dp), color = Color.LightGray.copy(0.3f))

                            Text(
                                text = "Notification options",
                                fontFamily = Gilroy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )

                            Spacer(Modifier.height(24.dp))

                            NotificationToggle(
                                checked = subscribeEarnings,
                                label = "Receive emails when reward points are added to the balance",
                                onToggle = { subscribeEarnings = !subscribeEarnings }
                            )

                            Spacer(Modifier.height(16.dp))

                            NotificationToggle(
                                checked = subscribeExpire,
                                label = "Receive emails when reward points are about to expire",
                                onToggle = { subscribeExpire = !subscribeExpire }
                            )

                            Spacer(Modifier.height(40.dp))

                            OutlinedButton(
                                onClick = { viewModel.saveSubscription(subscribeEarnings, subscribeExpire) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color.Black),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                                enabled = saveState !is UiState.Loading
                            ) {
                                if (saveState is UiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
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
            else -> {}
        }
    }
}

@Composable
private fun NotificationToggle(checked: Boolean, label: String, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFFF57C00) else Color.White)
                .border(1.dp, if (checked) Color(0xFFF57C00) else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = Gilroy,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.Black,
            lineHeight = 20.sp
        )
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
        modifier = modifier.fillMaxHeight().background(bgColor).padding(8.dp),
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
        Spacer(Modifier.height(4.dp))
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
