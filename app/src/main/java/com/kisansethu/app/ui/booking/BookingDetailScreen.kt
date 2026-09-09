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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(booking.trackingId) {
        viewModel.startListening(booking.trackingId)
        onDispose {
            viewModel.stopListening()
        }
    }

    val displayBooking = uiState.booking ?: booking
    val qrCodeBitmap = remember(displayBooking.trackingId) {
        QrCodeGenerator.generateQrCode(displayBooking.trackingId)
    }

    val displayStatus = when (displayBooking.status) {
        "BOOKED" -> "Booking Pending Confirmation"
        "CONFIRMED" -> "Booking Confirmed"
        "CHECKED_IN" -> "CHECKED IN"
        else -> displayBooking.status
    }
    
    val statusColor = when (displayBooking.status) {
        "CONFIRMED" -> MaterialTheme.colorScheme.primary
        "CHECKED_IN" -> Color(0xFF0288D1) // Bright blue to match active checked-in state (adjusts ok on dark mode)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading && uiState.booking == null) {
                val primaryColor = MaterialTheme.colorScheme.primary
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { context ->
                        com.google.android.material.loadingindicator.LoadingIndicator(context)
                    },
                    update = { view ->
                        view.setIndicatorColor(primaryColor.toArgb())
                    },
                    modifier = Modifier.size(64.dp)
                )
            } else if (uiState.error != null && uiState.booking == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to load booking details.", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.startListening(booking.trackingId) }) {
                        Text("Retry")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 116.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ReviewItem("Centre", displayBooking.centreName)
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewItem("Date & Time", "${displayBooking.bookingDate} | ${displayBooking.slotStartTime} - ${displayBooking.slotEndTime}")
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewItem("Produce", "${displayBooking.quantity} ${displayBooking.quantityUnit.lowercase()} ${displayBooking.crop}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Column {
                                Text(text = "Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = displayStatus, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Tracking ID: ${displayBooking.trackingId}",
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
    }
}
