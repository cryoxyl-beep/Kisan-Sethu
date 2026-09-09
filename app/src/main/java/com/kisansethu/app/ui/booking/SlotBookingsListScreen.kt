package com.kisansethu.app.ui.booking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    
    val DarkGreen = Color(0xFF2E5E41)
    val LightGreenPill = Color(0xFFE5F0E8)
    val OffWhiteBg = Color(0xFFFAFAFA)
    val DarkCharcoal = Color(0xFF1E1E1E)
    val MutedGray = Color(0xFF6E6E6E)

    LaunchedEffect(farmerId) {
        viewModel.loadBookings(farmerId)
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OffWhiteBg,
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = onBookSlotClick,
                    containerColor = DarkGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                        .size(64.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Book Slot", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "My Bookings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track all your procurements",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedGray
                )
            }

            // Custom Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = OffWhiteBg,
                contentColor = DarkGreen,
                modifier = Modifier.padding(horizontal = 24.dp),
                divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = DarkGreen,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Upcoming",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) DarkGreen else MutedGray
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Completed",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) DarkGreen else MutedGray
                        )
                    }
                )
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
                            Text("Retry", color = Color.White)
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
                                onViewClick = onViewBookingClick
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatBookingDate(dateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        LocalDate.parse(dateStr).format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun BookingCard(booking: Booking, onViewClick: (Booking) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val DarkGreen = Color(0xFF2E5E41)
    val LightGreenPill = Color(0xFFE5F0E8)
    val DarkCharcoal = Color(0xFF1E1E1E)
    val MutedGray = Color(0xFF6E6E6E)
    val CardBg = Color.White

    val displayStatus = when (booking.status) {
        "BOOKED" -> {
            val isToday = booking.bookingDate == LocalDate.now().toString()
            if (isToday) "Booked" else "Scheduled"
        }
        "COMPLETED" -> "Completed"
        "CONFIRMED" -> "Confirmed"
        "CHECKED_IN" -> "Checked In"
        else -> booking.status
    }
    
    val isCompleted = booking.status == "COMPLETED"
    val isBooked = booking.status == "BOOKED"
    val isConfirmed = booking.status == "CONFIRMED"
    
    val statusBgColor = when {
        isCompleted || isConfirmed -> LightGreenPill
        isBooked -> Color(0xFFE3F2FD) // light blue
        else -> Color(0xFFFFF3E0)
    }
    val statusTextColor = when {
        isCompleted || isConfirmed -> DarkGreen
        isBooked -> Color(0xFF1565C0) // blue
        else -> Color(0xFFE65100)
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
        modifier = Modifier.fillMaxWidth().clickable { onViewClick(booking) },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Icon + Title/Location on left, Status Badge on bottom of title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                    modifier = Modifier.weight(1f)
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
                
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MutedGray
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val formattedQty = if (booking.quantity % 1.0 == 0.0) {
                booking.quantity.toInt().toString()
            } else {
                booking.quantity.toString()
            }
            
            Text(
                text = "${booking.crop} • $formattedQty ${booking.quantityUnit.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkCharcoal
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = MutedGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${formatBookingDate(booking.bookingDate)} • ${booking.slotStartTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray
                )
            }
        }
    }
}
