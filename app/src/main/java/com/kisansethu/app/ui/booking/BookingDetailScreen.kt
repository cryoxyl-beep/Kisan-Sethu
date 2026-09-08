package com.kisansethu.app.ui.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kisansethu.app.data.Booking
import com.kisansethu.app.utils.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qrCodeBitmap = remember(booking.trackingId) {
        QrCodeGenerator.generateQrCode(booking.trackingId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("Booking Detail") },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 116.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReviewItem("Centre", booking.centreName)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReviewItem("Date & Time", "${booking.bookingDate} | ${booking.slotStartTime} - ${booking.slotEndTime}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReviewItem("Produce", "${booking.quantity} ${booking.quantityUnit.lowercase()} ${booking.crop}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReviewItem("Status", booking.status)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tracking ID: ${booking.trackingId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(150.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Show this QR code at the procurement centre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
