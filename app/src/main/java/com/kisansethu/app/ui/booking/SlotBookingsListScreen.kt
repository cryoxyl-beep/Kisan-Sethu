package com.kisansethu.app.ui.booking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.loadingindicator.LoadingIndicator
import com.kisansethu.app.data.Booking

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
    val isDark = isSystemInDarkTheme()

    val DarkGreen = if (isDark) Color(0xFF81C784) else Color(0xFF2E5E41)
    val LightGreenPill = if (isDark) Color(0xFF1B3B26) else Color(0xFFE5F0E8)
    val OffWhiteBg = if (isDark) Color(0xFF121212) else Color(0xFFFAFAFA)
    val DarkCharcoal = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1E1E1E)
    val MutedGray = if (isDark) Color(0xFFA0A0A0) else Color(0xFF6E6E6E)
    val CardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val BorderColor = if (isDark) Color(0xFF333333) else Color.LightGray.copy(alpha = 0.3f)

    LaunchedEffect(farmerId) {
        viewModel.loadBookings(farmerId)
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OffWhiteBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onBookSlotClick,
                containerColor = DarkGreen,
                contentColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .size(64.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Book Slot", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = "Hello,", style = MaterialTheme.typography.bodyLarge, color = MutedGray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = farmerName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
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
                    .background(CardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
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
                    Text(
                        "Upcoming",
                        color = if (selectedTabIndex == 0) (if(isDark) Color(0xFF1E1E1E) else Color.White) else MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Text(
                        "Completed",
                        color = if (selectedTabIndex == 1) (if(isDark) Color(0xFF1E1E1E) else Color.White) else MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AndroidView(
                        factory = { context -> LoadingIndicator(context) },
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
                            Text("Retry", color = if (isDark) Color(0xFF1E1E1E) else Color.White)
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
                        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(list) { booking ->
                            BookingCard(
                                booking = booking,
                                isDark = isDark,
                                onViewClick = onViewBookingClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, isDark: Boolean, onViewClick: (Booking) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val DarkGreen = if (isDark) Color(0xFF81C784) else Color(0xFF2E5E41)
    val LightGreenPill = if (isDark) Color(0xFF1B3B26) else Color(0xFFE5F0E8)
    val DarkCharcoal = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1E1E1E)
    val MutedGray = if (isDark) Color(0xFFA0A0A0) else Color(0xFF6E6E6E)
    val CardBg = if (isDark) Color(0xFF1E1E1E) else Color.White

    val displayStatus = when (booking.status) {
        "BOOKED" -> "Booking Pending"
        "CONFIRMED" -> "Booking Confirmed"
        "CHECKED_IN" -> "CHECKED IN"
        else -> booking.status
    }
    
    val isConfirmed = booking.status == "CONFIRMED"
    val isCheckedIn = booking.status == "CHECKED_IN"
    
    val statusBgColor = when {
        isCheckedIn -> if (isDark) Color(0xFF1A3B4D) else Color(0xFFE1F5FE)
        isConfirmed -> LightGreenPill
        else -> if (isDark) Color(0xFF4A3B1C) else Color(0xFFFFF3E0)
    }
    val statusTextColor = when {
        isCheckedIn -> if (isDark) Color(0xFF81D4FA) else Color(0xFF0277BD)
        isConfirmed -> DarkGreen
        else -> if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
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
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Icon + Title/Location on left, Status Badge on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LightGreenPill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = cleanTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = locationSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isConfirmed || isCheckedIn) {
                            val icon = if (isCheckedIn) Icons.Rounded.Verified else Icons.Rounded.CheckCircle
                            Icon(icon, contentDescription = null, tint = statusTextColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = displayStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Grid Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Rounded.CalendarToday,
                        label = "Date",
                        value = booking.bookingDate,
                        isDark = isDark
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Rounded.AccessTime,
                        label = "Time",
                        value = "${booking.slotStartTime} - ${booking.slotEndTime}",
                        isDark = isDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Rounded.Eco,
                        label = "Crop",
                        value = booking.crop,
                        isDark = isDark
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    val formattedQty = if (booking.quantity % 1.0 == 0.0) {
                        booking.quantity.toInt().toString()
                    } else {
                        booking.quantity.toString()
                    }
                    DetailItem(
                        icon = Icons.Rounded.MonitorWeight,
                        label = "Quantity",
                        value = "$formattedQty ${booking.quantityUnit.lowercase()}",
                        isDark = isDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer Row: Copyable Booking ID & Functional View Details Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(booking.trackingId))
                            Toast.makeText(context, "Booking ID copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text(text = "Booking ID", style = MaterialTheme.typography.labelSmall, color = MutedGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = booking.trackingId,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = MutedGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(LightGreenPill)
                        .clickable { onViewClick(booking) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "View Details",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    val DarkGreen = if (isDark) Color(0xFF81C784) else Color(0xFF2E5E41)
    val LightGreenPill = if (isDark) Color(0xFF1B3B26) else Color(0xFFE5F0E8)
    val DarkCharcoal = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1E1E1E)
    val MutedGray = if (isDark) Color(0xFFA0A0A0) else Color(0xFF6E6E6E)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LightGreenPill),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedGray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.SemiBold,
                color = DarkCharcoal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
