package com.kisansethu.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.fcm.FcmManager
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    farmerName: String,
    farmerId: String,
    onBookSlot: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(farmerName, farmerId) {
        viewModel.initialize(farmerName, farmerId)
        if (context is Activity) {
            FcmManager.requestNotificationPermission(context)
        }
        FcmManager.registerFcmToken(context, farmerId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6E6E6E)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.farmerName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = Color(0xFF2E5E41),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Here are your procurements",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6E6E6E)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1E1E1E),
                        modifier = Modifier.size(28.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B3B26)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.farmerName.firstOrNull()?.toString() ?: "F",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.hasActiveBooking && uiState.activeBooking != null) {
            val booking = uiState.activeBooking!!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2E5E41))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "1 procurement active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Head to the centre for check-in",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF2E5E41),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Procurement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF2E5E41),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBookSlot() } // In reality this would go to bookings tab, but we just want to avoid crashing
                )
            }
            
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                ActiveBookingCard(booking = booking, queueData = uiState.liveQueueData)
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                EmptyBookingState(onBookSlot = onBookSlot)
            }
        }
    }
}

@Composable
fun EmptyBookingState(onBookSlot: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No active procurement queue.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6E6E6E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to schedule your procurement?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E1E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBookSlot,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5E41)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = "Book a Procurement Slot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatHomeDate(dateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        LocalDate.parse(dateStr).format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun ActiveBookingCard(booking: com.kisansethu.app.data.Booking, queueData: LiveQueueData?) {
    val isConfirmed = booking.status == BookingStatus.CONFIRMED.name
    val isCheckedIn = booking.status == BookingStatus.CHECKED_IN.name
    val isWaiting = booking.status == BookingStatus.WAITING.name
    val isServing = booking.status == BookingStatus.NOW_SERVING.name
    val isProcessing = booking.status == BookingStatus.PROCESSING.name

    val displayStatus = when {
        isConfirmed -> "CONFIRMED"
        isCheckedIn -> "CHECKED IN"
        isWaiting -> "WAITING"
        isServing -> "NOW SERVING"
        isProcessing -> "PROCESSING"
        else -> booking.status
    }
    
    val statusBgColor = when {
        isConfirmed -> Color(0xFFE5F0E8)
        isCheckedIn -> Color(0xFFE3F2FD)
        isWaiting -> Color(0xFFFFF3E0)
        isServing -> Color(0xFFE8F5E9)
        isProcessing -> Color(0xFFE1F5FE)
        else -> Color(0xFFF5F5F5)
    }
    
    val statusTextColor = when {
        isConfirmed -> Color(0xFF2E5E41)
        isCheckedIn -> Color(0xFF1565C0)
        isWaiting -> Color(0xFFE65100)
        isServing -> Color(0xFF2E7D32)
        isProcessing -> Color(0xFF0277BD)
        else -> Color(0xFF757575)
    }

    val locationSubtitle = if (booking.centreName.contains(",")) {
        booking.centreName.substringAfter(",").trim()
    } else {
        "Medchal, Telangana"
    }
    val cleanTitle = if (booking.centreName.contains(",")) {
        booking.centreName.substringBefore(",").trim()
    } else {
        booking.centreName
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5F0E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = Color(0xFF2E5E41),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cleanTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = locationSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6E6E6E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = displayStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isCheckedIn || isWaiting || isServing || isProcessing) {
                Spacer(modifier = Modifier.height(16.dp))
                val message = when {
                    isCheckedIn -> "Your booking has been checked in."
                    isWaiting -> "Waiting in queue"
                    isServing -> "Now serving"
                    isProcessing -> "Procurement in progress"
                    else -> ""
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Date", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E6E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatHomeDate(booking.bookingDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Crop", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E6E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = booking.crop,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Time", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E6E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${booking.slotStartTime} - ${booking.slotEndTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val formattedQty = if (booking.quantity % 1.0 == 0.0) booking.quantity.toInt().toString() else booking.quantity.toString()
                    Text(text = "Quantity", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E6E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$formattedQty ${booking.quantityUnit.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Booking ID", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E6E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = booking.trackingId,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                }
            }

            if ((isWaiting || isServing) && queueData != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QueueMetric(label = "Your Token", value = queueData.myToken, highlight = true)
                    QueueMetric(label = "Serving Token", value = queueData.currentServingToken)
                }

                if (isWaiting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QueueMetric(label = "Your Position", value = queueData.myPosition.toString())
                        QueueMetric(label = "People Ahead", value = queueData.farmersAhead.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { /* Check In Flow */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5E41)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Rounded.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check In",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QueueMetric(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6E6E6E)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF2E5E41) else Color(0xFF1E1E1E)
        )
    }
}
