package com.kisansethu.app.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.loadingindicator.LoadingIndicator
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.formatCurrencyAmount
import com.kisansethu.app.data.formatQuantityDisplay
import com.kisansethu.app.data.formatRateDisplay
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.normalizeStatus
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

    val DarkGreen = Color(0xFF1B3B26)
    val OffWhiteBg = Color(0xFFF7F8F7)
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
            FloatingActionButton(
                onClick = onBookSlotClick,
                containerColor = DarkGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 4.dp)
                    .size(60.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Book Slot",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "My Bookings",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track all your procurements",
                    fontSize = 14.sp,
                    color = MutedGray
                )
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = OffWhiteBg,
                contentColor = DarkGreen,
                modifier = Modifier.padding(horizontal = 20.dp),
                divider = {
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                },
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (selectedTabIndex == 1) DarkGreen else MutedGray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        Button(
                            onClick = { viewModel.loadBookings(farmerId) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
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
                                text = if (selectedTabIndex == 0) "No upcoming bookings" else "No completed bookings",
                                fontSize = 16.sp,
                                color = DarkCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedTabIndex == 0) "Book a procurement slot to see it here." else "Completed procurements will appear here.",
                                fontSize = 14.sp,
                                color = MutedGray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 8.dp,
                            end = 20.dp,
                            bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.trackingId }) { booking ->
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
    val SoftMint = Color(0xFFE5F3EA)
    val DarkCharcoal = Color(0xFF1E1E1E)
    val MutedGray = Color(0xFF6E6E6E)
    val ForestGreen = Color(0xFF1B3B26)

    val normStatus = normalizeStatus(booking.status)
    val displayStatus = getDisplayStatus(booking)

    val locationSubtitle = if (booking.centreName.contains(",")) {
        booking.centreName.substringAfter(",").trim()
    } else {
        "Procurement Centre"
    }
    val cleanTitle = if (booking.centreName.contains(",")) {
        booking.centreName.substringBefore(",").trim()
    } else {
        booking.centreName
    }

    if (normStatus == BookingStatus.COMPLETED.name) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewClick(booking) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFFDCECE2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header badge and tracking ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftMint
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1F6B45),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PROCUREMENT COMPLETED",
                                fontSize = 11.sp,
                                color = Color(0xFF1F6B45),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        text = "ID: ${booking.trackingId}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedGray
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Crop and Final Quantity (Left) & Final Amount (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = booking.getEffectiveCrop(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkCharcoal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatQuantityDisplay(booking.getEffectiveQuantity(), booking.getEffectiveUnit()),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E5E41)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftMint,
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Final Amount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F6B45)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCurrencyAmount(booking.finalPayableAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F3F1), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Rate & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rate",
                            fontSize = 11.sp,
                            color = MutedGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatRateDisplay(booking.finalRate, booking.getEffectiveUnit()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Status",
                            fontSize = 11.sp,
                            color = MutedGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Completed",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F6B45)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Centre and Procurement Date footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = MutedGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cleanTitle,
                        fontSize = 12.sp,
                        color = MutedGray,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = MutedGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatBookingDate(booking.bookingDate),
                        fontSize = 12.sp,
                        color = MutedGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFB0B8B3),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        val statusBgColor = when (normStatus) {
            BookingStatus.CONFIRMED.name -> SoftMint
            BookingStatus.PROCESSING.name -> Color(0xFFE1F5FE)
            BookingStatus.NOW_SERVING.name -> Color(0xFFE8F5E9)
            BookingStatus.WAITING.name -> Color(0xFFFFF3E0)
            BookingStatus.CHECKED_IN.name, BookingStatus.BOOKED.name -> Color(0xFFE3F2FD)
            else -> Color(0xFFF5F5F5)
        }

        val statusTextColor = when (normStatus) {
            BookingStatus.CONFIRMED.name -> Color(0xFF1F6B45)
            BookingStatus.PROCESSING.name -> Color(0xFF0277BD)
            BookingStatus.NOW_SERVING.name -> Color(0xFF2E7D32)
            BookingStatus.WAITING.name -> Color(0xFFE65100)
            BookingStatus.CHECKED_IN.name, BookingStatus.BOOKED.name -> Color(0xFF1565C0)
            else -> Color(0xFF757575)
        }

        val formattedQty = if (booking.quantity % 1.0 == 0.0) {
            "${booking.quantity.toInt()}.0"
        } else {
            booking.quantity.toString()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewClick(booking) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFFE8EEEA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SoftMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF2E5E41),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cleanTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = locationSubtitle,
                        fontSize = 13.sp,
                        color = MutedGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusBgColor
                        ) {
                            Text(
                                text = displayStatus,
                                fontSize = 11.sp,
                                color = statusTextColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "ID: ${booking.trackingId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedGray
                        )
                    }

                    if (normStatus == BookingStatus.PROCESSING.name) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your procurement process has started.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusTextColor
                        )
                    }

                    if (booking.queueToken.isNotEmpty() && (normStatus == BookingStatus.WAITING.name || normStatus == BookingStatus.NOW_SERVING.name || normStatus == BookingStatus.PROCESSING.name)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Token: ${booking.queueToken}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B3B26)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${booking.crop} • $formattedQty ${booking.quantityUnit.lowercase()}",
                        fontSize = 14.sp,
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
                            fontSize = 12.sp,
                            color = MutedGray
                        )
                    }
                }

                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFB0B8B3),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(22.dp)
                )
            }
        }
    }
}
