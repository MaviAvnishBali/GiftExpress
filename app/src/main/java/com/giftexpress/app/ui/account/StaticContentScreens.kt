package com.giftexpress.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.charlex.compose.HtmlText
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun StaticContentScreen(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
                    text = title,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CmsWebViewContent(html: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (html != null) {
            val decodedHtml = html
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                
            HtmlText(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                text = decodedHtml,
                fontSize = 15.sp,
                color = Color(0xFF333333)
            )
        } else {
            CircularProgressIndicator(color = Color.Black)
        }
    }
}

@Composable
fun AboutUsScreen(cmsViewModel: CmsViewModel? = null, onBackClick: () -> Unit) {
    val pages = cmsViewModel?.pages?.collectAsState()?.value ?: emptyList()
    val liveContent = pages.firstOrNull { it.title?.contains("about", ignoreCase = true) == true }?.content
    StaticContentScreen(title = "About Gift Express", onBackClick = onBackClick) {
        CmsWebViewContent(html = liveContent)
    }
}

@Composable
fun ShippingInfoScreen(cmsViewModel: CmsViewModel? = null, onBackClick: () -> Unit) {
    val pages = cmsViewModel?.pages?.collectAsState()?.value ?: emptyList()
    val liveContent = pages.firstOrNull { it.title?.contains("shipping", ignoreCase = true) == true }?.content
    StaticContentScreen(title = "Shipping Information", onBackClick = onBackClick) {
        CmsWebViewContent(html = liveContent)
    }
}

@Composable
fun PrivacyPolicyScreen(cmsViewModel: CmsViewModel? = null, onBackClick: () -> Unit) {
    val pages = cmsViewModel?.pages?.collectAsState()?.value ?: emptyList()
    val liveContent = pages.firstOrNull { it.title?.contains("privacy", ignoreCase = true) == true }?.content
    StaticContentScreen(title = "Privacy Policy", onBackClick = onBackClick) {
        CmsWebViewContent(html = liveContent)
    }
}

@Composable
fun TermsConditionsScreen(cmsViewModel: CmsViewModel? = null, onBackClick: () -> Unit) {
    val pages = cmsViewModel?.pages?.collectAsState()?.value ?: emptyList()
    val liveContent = pages.firstOrNull { it.title?.contains("terms", ignoreCase = true) == true }?.content
    StaticContentScreen(title = "Terms and Conditions", onBackClick = onBackClick) {
        CmsWebViewContent(html = liveContent)
    }
}


