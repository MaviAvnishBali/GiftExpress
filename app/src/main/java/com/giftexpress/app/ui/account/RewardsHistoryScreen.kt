package com.giftexpress.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.Gilroy

data class RewardTransaction(
    val day: String,
    val monthYear: String,
    val name: String,
    val amount: String,
    val points: String
)

@Composable
fun RewardsHistoryScreen(
    onBackClick: () -> Unit
) {
    val transactions = listOf(
        RewardTransaction("30", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("01", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("12", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("08", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("23", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("19", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2"),
        RewardTransaction("27", "Dec 25", "Raman Kumar S O Mehar CHA,IN", "INR 250.00", "2")
    )

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
                    text = "My Rewards History",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        containerColor = Color(0xFFF8F8F8)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(transactions) { transaction ->
                RewardTransactionCard(transaction)
            }
        }
    }
}

@Composable
fun RewardTransactionCard(transaction: RewardTransaction) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Date Section
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
                        text = transaction.day,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = transaction.monthYear,
                        fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                        fontSize = 12.sp,
                        color = Color(0xFF333333)
                    )
                }

                // Middle Section
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.name,
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1.2f),
                        lineHeight = 18.sp
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Transaction",
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Amount: ${transaction.amount}",
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Bottom Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Point Earned By You:",
                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = transaction.points,
                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewRewardsHistoryScreen() {
    RewardsHistoryScreen(onBackClick = {})
}
