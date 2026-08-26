package com.giftexpress.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.giftexpress.app.data.model.RewardHistoryItem
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

@Composable
fun RewardsHistoryScreen(
    viewModel: RewardsViewModel,
    onBackClick: () -> Unit
) {
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
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
                    text = "My Rewards History",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        containerColor = Color(0xFFF8F8F8)
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = historyState) {
                is UiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(5) {
                            Box(Modifier.fillMaxWidth().height(90.dp).shimmerEffect())
                        }
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Gray, fontFamily = Gilroy)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val items = state.data
                    if (items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No reward history yet", fontFamily = Gilroy, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(items) { item ->
                                RewardTransactionCard(item)
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
fun RewardTransactionCard(item: RewardHistoryItem) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(4.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier
                        .width(70.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFF0F9FF))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.getDisplayDay(),
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = item.getDisplayMonth(),
                        fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                        fontSize = 12.sp,
                        color = Color(0xFF333333)
                    )
                }

                Row(
                    modifier = Modifier.weight(1f).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.comment.ifBlank { item.action },
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1.2f),
                        lineHeight = 18.sp
                    )

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        val amountColor = if (item.amount >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        val sign = if (item.amount >= 0) "+" else ""
                        Text(
                            text = "${sign}${item.amount} pts",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 14.sp,
                            color = amountColor
                        )
                        Text(
                            text = "Balance: ${item.pointsLeft}",
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
