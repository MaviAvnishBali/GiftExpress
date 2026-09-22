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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (html != null) {
            val decodedHtml = sanitizeCmsHtml(html)

            HtmlText(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                text = decodedHtml,
                fontSize = 15.sp,
                color = Color(0xFF333333),
                onUriClick = { rawUri ->
                    handleCmsUriClick(context, rawUri)
                }
            )
        } else {
            CircularProgressIndicator(color = Color.Black)
        }
    }
}

fun sanitizeCmsHtml(html: String): String {
    var decoded = html
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    // 1. Remove Website contact entries (e.g. <li>Website: <a ...>www.giftexpress.com</a></li>)
    decoded = decoded.replace(Regex("(?i)<li\\b[^>]*>\\s*Website:\\s*(?:<a\\b[^>]*>.*?</a>|[^<]*)\\s*</li>"), "")
    decoded = decoded.replace(Regex("(?i)<p\\b[^>]*>\\s*Website:\\s*(?:<a\\b[^>]*>.*?</a>|[^<]*)\\s*</p>"), "")
    decoded = decoded.replace(Regex("(?i)Website:\\s*<a\\b[^>]*>.*?</a>"), "")

    // 2. Unwrap and clean anchor tags pointing to giftexpress.com or Magento template url directives
    decoded = decoded.replace(Regex("(?i)<a\\b(?:(?!href)[^>])*\\bhref\\s*=\\s*[\"']?(?:https?://)?(?:www\\.)?giftexpress\\.com[^>]*>(.*?)</a>")) { matchResult ->
        cleanAnchorText(matchResult.groupValues[1])
    }
    decoded = decoded.replace(Regex("(?i)<a\\b(?:(?!href)[^>])*\\bhref\\s*=\\s*[\"']?\\{\\{(?:store|view)\\s+url=[^>]*>(.*?)</a>")) { matchResult ->
        cleanAnchorText(matchResult.groupValues[1])
    }

    // 3. Keep media URL (images) if any, but strip remaining template directives
    decoded = decoded.replace(
        Regex("\\{\\{media url=[\"'](.*?)[\"']\\}\\}"),
        "https://magento-1620955-6409883.cloudwaysapps.com/media/$1"
    )
    decoded = decoded.replace(Regex("\\{\\{.*?\\}\\}"), "")

    // 4. Replace standalone website domain mentions while preserving email addresses
    decoded = decoded.replace(Regex("(?i)(?<!@)(?<!mailto:)https?://(?:www\\.)?giftexpress\\.com/?"), "GiftExpress")
    decoded = decoded.replace(Regex("(?i)(?<!@)(?<!mailto:)www\\.giftexpress\\.com/?"), "GiftExpress")
    decoded = decoded.replace(Regex("(?i)(?<!@)(?<!mailto:)\\bgiftexpress\\.com\\b"), "GiftExpress")

    return decoded
}

private fun cleanAnchorText(innerText: String): String {
    return innerText
        .replace(Regex("(?i)https?://"), "")
        .replace(Regex("(?i)www\\."), "")
        .replace(Regex("(?i)giftexpress\\.com"), "GiftExpress")
}

private fun handleCmsUriClick(context: Context, rawUri: String) {
    try {
        val trimmed = rawUri.trim()
        if (trimmed.isBlank()) return

        if (trimmed.startsWith("mailto:", ignoreCase = true) || (trimmed.contains("@") && !trimmed.contains("/"))) {
            val mailUri = if (trimmed.startsWith("mailto:", ignoreCase = true)) trimmed else "mailto:$trimmed"
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailUri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(trimmed)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        // Suppress opening website links to giftexpress.com
        if (trimmed.contains("giftexpress.com", ignoreCase = true)) {
            return
        }

        val resolvedUri = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resolvedUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("CmsWebViewContent", "Failed to open link: $rawUri", e)
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
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


