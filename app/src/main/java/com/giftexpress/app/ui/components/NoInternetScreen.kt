package com.giftexpress.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R

@Composable
fun NoInternetScreen(
    onRetry: () -> Unit
) {
    // Pulsating animation setup
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern animated Wifi icon (Replaces old Lottie)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Outer pulsating ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.primary).copy(alpha = alphaAnim * 0.3f))
            )
            // Inner pulsating ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale * 1.05f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.primary).copy(alpha = alphaAnim * 0.5f))
            )
            // Center icon background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_wifi_off),
                    contentDescription = "No Internet",
                    modifier = Modifier.size(40.dp),
                    tint = colorResource(id = R.color.primary)
                )
            }
        }
 
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Whoops!",
            fontSize = 28.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No Internet Connection Found.\nCheck your connection or try again.",
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.primary)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "TRY AGAIN",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}
