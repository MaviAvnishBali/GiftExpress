package com.giftexpress.app.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import androidx.compose.ui.graphics.Color as ComposeColor
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

fun Context.findActivity(): ComponentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

fun showLoginRequiredDialog(context: Context, message: String = "Please login to continue.", onLogin: () -> Unit) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    
    val composeView = ComposeView(context).apply {
        setContent {
            val gilroyFamily = FontFamily(
                Font(R.font.gilroy_regular, FontWeight.Normal),
                Font(R.font.gilroy_bold, FontWeight.Bold)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ComposeColor.White,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Login",
                        tint = ComposeColor(0xFF2B2B2B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Login Required",
                        fontFamily = gilroyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ComposeColor.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontFamily = gilroyFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = ComposeColor.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { dialog.dismiss() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ComposeColor.Black
                            )
                        ) {
                            Text("Cancel", fontFamily = gilroyFamily, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                dialog.dismiss()
                                onLogin()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ComposeColor.Black,
                                contentColor = ComposeColor.White
                            )
                        ) {
                            Text("Login", fontFamily = gilroyFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    
    dialog.setContentView(composeView)
    
    val activity = context.findActivity()
    if (activity != null) {
        dialog.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(activity)
            decorView.setViewTreeViewModelStoreOwner(activity)
            decorView.setViewTreeSavedStateRegistryOwner(activity)
        }
    }
    
    dialog.show()
}
