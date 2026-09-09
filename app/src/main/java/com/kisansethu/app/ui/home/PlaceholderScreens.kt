package com.kisansethu.app.ui.home

import android.content.pm.ApplicationInfo
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.kisansethu.app.fcm.FcmManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SlotBookingsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Slot Bookings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun PaymentPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Payments",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ProfilePlaceholder(
    farmerId: String = "",
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
    onClearSession: (suspend () -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentToken by remember { mutableStateOf("Loading...") }
    var registrationStatus by remember { mutableStateOf("Checking...") }
    var lastUpdated by remember { mutableStateOf("-") }

    val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    LaunchedEffect(farmerId) {
        if (isDebug && farmerId.isNotEmpty()) {
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
            val ref = FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("farmers").child(farmerId).child("devices").child(deviceId)
            
            ref.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    currentToken = snapshot.child("fcmToken").getValue(String::class.java) ?: "Not found"
                    registrationStatus = "Registered in RTDB"
                    val timestamp = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                    lastUpdated = if (timestamp > 0) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                    } else "Unknown"
                } else {
                    currentToken = "None"
                    registrationStatus = "Not registered in RTDB"
                    lastUpdated = "-"
                }
            }.addOnFailureListener {
                registrationStatus = "Error checking RTDB: ${it.message}"
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            if (onSignOut != null) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onClearSession?.invoke()
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            }

            if (isDebug) {
                Spacer(modifier = Modifier.height(32.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Developer Debug (FCM)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Status: $registrationStatus", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Last Updated: $lastUpdated", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Token: $currentToken", 
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        registrationStatus = "Refreshing..."
                        FcmManager.registerFcmToken(context, farmerId)
                        // Wait a sec then reload UI
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(2000)
                            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
                            val ref = FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("farmers").child(farmerId).child("devices").child(deviceId)
                            ref.get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    currentToken = snapshot.child("fcmToken").getValue(String::class.java) ?: "Not found"
                                    registrationStatus = "Refreshed & Registered"
                                    val timestamp = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                                    lastUpdated = if (timestamp > 0) {
                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                                    } else "Unknown"
                                }
                            }
                        }
                    }
                ) {
                    Text("Refresh FCM Token")
                }
            }
        }
    }
}
