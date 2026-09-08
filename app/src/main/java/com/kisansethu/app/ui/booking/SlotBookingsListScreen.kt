package com.kisansethu.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.data.Booking

val OffWhiteBg = Color(0xFFFAFAFA)
val DarkGreen = Color(0xFF2E5E41)
val LightGreenPill = Color(0xFFE5F0E8)
val DarkCharcoal = Color(0xFF1E1E1E)
val MutedGray = Color(0xFF6E6E6E)

@Composable
fun SlotBookingsListScreen(
    farmerId: String,
    farmerName: String,
    onBookSlotClick: () -> Unit,
    onViewBookingClick: (Booking) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SlotBookingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(farmerId) {
        viewModel.loadBookings(farmerId)
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OffWhiteBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hello,", style = MaterialTheme.typography.bodyLarge, color = MutedGray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = farmerName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Rounded.Eco, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                    }
                    Text(text = "Here are your bookings", style = MaterialTheme.typography.bodyMedium, color = MutedGray)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LightGreenPill),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = farmerName.firstOrNull()?.toString() ?: "F",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Custom Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTabIndex == 0) DarkGreen else Color.Transparent)
                        .clickable { selectedTabIndex = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Upcoming", color = if (selectedTabIndex == 0) Color.White else MutedGray, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTabIndex == 1) DarkGreen else Color.Transparent)
                        .clickable { selectedTabIndex = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Completed", color = if (selectedTabIndex == 1) Color.White else MutedGray, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context -> com.google.android.material.loadingindicator.LoadingIndicator(context) },
                        update = { view -> view.setIndicatorColor(DarkGreen.toArgb()) },
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load booking details.", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBookings(farmerId) }, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                val list = if (selectedTabIndex == 0) uiState.upcomingBookings else uiState.completedBookings
                
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No ${if(selectedTabIndex == 0) "upcoming" else "completed"} bookings",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Book a procurement slot to see it here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedGray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 116.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(list) { booking ->
                            BookingCard(booking, onViewBookingClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, onViewClick: (Booking) -> Unit) {
    val displayStatus = when (booking.status) {
        "BOOKED" -> "Booking Pending"
        "CONFIRMED" -> "Booking Confirmed"
        else -> booking.status
    }
    
    val isConfirmed = booking.status == "CONFIRMED"
    val statusBgColor = if (isConfirmed) LightGreenPill else Color(0xFFFFF3E0)
    val statusTextColor = if (isConfirmed) DarkGreen else Color(0xFFE65100)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewClick(booking) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(LightGreenPill),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = booking.centreName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Text(text = "Procurement Centre", style = MaterialTheme.typography.bodySmall, color = MutedGray)
                    }
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(statusBgColor).padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isConfirmed) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = statusTextColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(text = displayStatus, style = MaterialTheme.typography.labelSmall, color = statusTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Grid Details
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(icon = Icons.Rounded.CalendarToday, label = "Date", value = booking.bookingDate)
                }
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(icon = Icons.Rounded.AccessTime, label = "Time", value = "${booking.slotStartTime} - ${booking.slotEndTime}")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(icon = Icons.Rounded.Eco, label = "Crop", value = booking.crop)
                }
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(icon = Icons.Rounded.MonitorWeight, label = "Quantity", value = "${booking.quantity} ${booking.quantityUnit.lowercase()}")
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Booking ID", style = MaterialTheme.typography.labelSmall, color = MutedGray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = booking.trackingId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy ID", tint = MutedGray, modifier = Modifier.size(14.dp))
                    }
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(LightGreenPill).padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Details", style = MaterialTheme.typography.labelMedium, color = DarkGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row {
        Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedGray)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkCharcoal)
        }
    }
}
