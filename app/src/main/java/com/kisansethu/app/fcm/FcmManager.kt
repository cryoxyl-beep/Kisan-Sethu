package com.kisansethu.app.fcm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object FcmManager {

    private const val TAG = "FcmManager"

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    fun registerFcmToken(context: Context, farmerId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Token: \$token")

            saveTokenToDatabase(context, farmerId, token)
        }
    }

    private fun saveTokenToDatabase(context: Context, farmerId: String, token: String) {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"

        val database = FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("farmers").child(farmerId).child("devices").child(deviceId)

        val deviceData = mapOf(
            "fcmToken" to token,
            "platform" to "android",
            "updatedAt" to System.currentTimeMillis(),
            // createdAt will be set via updateChildren only if it doesn't exist, but for simplicity:
            "createdAt" to System.currentTimeMillis()
        )

        ref.updateChildren(deviceData).addOnSuccessListener {
            Log.d(TAG, "FCM Token saved successfully for farmer \$farmerId on device \$deviceId")
        }.addOnFailureListener {
            Log.e(TAG, "Failed to save FCM token", it)
        }
    }
}
