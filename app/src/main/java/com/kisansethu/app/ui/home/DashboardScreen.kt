package com.kisansethu.app.ui.home

import android.app.Activity
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.normalizeStatus
import com.kisansethu.app.fcm.FcmManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ForestGreen = Color(0xFF1B3B26)
private val SoftMint = Color(0xFFE5F3EA)
private val PageBg = Color(0xFFF7F8F7)
private val MutedGray = Color(0xFF6E6E6E)
private val Charcoal = Color(0xFF1E1E1E)

@Composable
fun DashboardScreen(
    farmerName: String,
    farmerId: String,
    onBookSlot: () -> Unit,
    onViewAll: () -> Unit = onBookSlot,
    onViewBooking: ((Booking) -> Unit)? = null,
    onViewLiveTicket: ((Booking) -> Unit)? = null,
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
            .background(PageBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Header Greeting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello,",
                    fontSize = 15.sp,
                    color = MutedGray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.farmerName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = Color(0xFF2E5E41),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Here are your procurements",
                    fontSize = 14.sp,
                    color = MutedGray
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Charcoal,
                        modifier = Modifier.size(28.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ForestGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.farmerName.firstOrNull()?.uppercaseChar()?.toString() ?: "F",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.hasActiveBooking && uiState.activeBooking != null) {
            val primaryBooking = uiState.activeBooking!!
            val activeList = if (uiState.activeBookings.isNotEmpty()) uiState.activeBookings else listOf(primaryBooking)
            val count = activeList.size

            val bannerTitle = if (count == 1) {
                val isToday = try { primaryBooking.bookingDate == LocalDate.now().toString() } catch (e: Exception) { false }
                if (isToday) "1 procurement today" else "1 active procurement"
            } else {
                "$count active procurements"
            }

            val bannerSubtitle = when {
                normalizeStatus(primaryBooking.status) == BookingStatus.PROCESSING.name -> "Procurement in progress"
                normalizeStatus(primaryBooking.status) == BookingStatus.NOW_SERVING.name -> "Counter is now serving your slot"
                normalizeStatus(primaryBooking.status) == BookingStatus.WAITING.name -> "You are in the live queue"
                normalizeStatus(primaryBooking.status) == BookingStatus.CHECKED_IN.name -> "Checked in at the procurement centre"
                getDisplayStatus(primaryBooking) == "SCHEDULED" -> "Scheduled for ${formatHomeDate(primaryBooking.bookingDate)}"
                else -> "Head to the centre for check-in"
            }

            // Active Summary Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1C4A38), Color(0xFF163A2D))
                        )
                    )
                    .clickable { onViewAll() }
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.EventNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bannerTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bannerSubtitle,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
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
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            val normPrimaryStatus = normalizeStatus(primaryBooking.status)
            val isLiveQueueActive = normPrimaryStatus in listOf(
                BookingStatus.CHECKED_IN.name,
                BookingStatus.WAITING.name,
                BookingStatus.NOW_SERVING.name,
                BookingStatus.PROCESSING.name
            )

            if (isLiveQueueActive) {
                val tokenDisplay = uiState.liveQueueData?.myToken?.ifEmpty {
                    com.kisansethu.app.data.formatTokenDisplay(primaryBooking.queueToken, primaryBooking.tokenNumber)
                } ?: com.kisansethu.app.data.formatTokenDisplay(primaryBooking.queueToken, primaryBooking.tokenNumber)
                val statusText = when (normPrimaryStatus) {
                    BookingStatus.WAITING.name -> "Waiting"
                    BookingStatus.NOW_SERVING.name -> "Now Serving"
                    BookingStatus.PROCESSING.name -> "Processing"
                    BookingStatus.CHECKED_IN.name -> "Checked In"
                    else -> getDisplayStatus(primaryBooking)
                }
                val position = uiState.liveQueueData?.myPosition ?: 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8EEEA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE PROCUREMENT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E5E41),
                                    letterSpacing = 1.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when (normPrimaryStatus) {
                                    BookingStatus.NOW_SERVING.name -> Color(0xFFE8F5E9)
                                    BookingStatus.PROCESSING.name -> Color(0xFFE1F5FE)
                                    BookingStatus.WAITING.name -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                }
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (normPrimaryStatus) {
                                        BookingStatus.NOW_SERVING.name -> Color(0xFF2E7D32)
                                        BookingStatus.PROCESSING.name -> Color(0xFF0277BD)
                                        BookingStatus.WAITING.name -> Color(0xFFE65100)
                                        else -> Color(0xFF1565C0)
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Token $tokenDisplay",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Status: $statusText",
                                    fontSize = 14.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if ((normPrimaryStatus == BookingStatus.WAITING.name || normPrimaryStatus == BookingStatus.CHECKED_IN.name) && position > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Position: $position",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                    val ahead = uiState.liveQueueData?.farmersAhead ?: 0
                                    if (ahead > 0) {
                                        Text(
                                            text = "$ahead ahead",
                                            fontSize = 12.sp,
                                            color = MutedGray
                                        )
                                    }
                                }
                            } else if (normPrimaryStatus == BookingStatus.NOW_SERVING.name) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "Your Turn",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            } else if (normPrimaryStatus == BookingStatus.PROCESSING.name) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE1F5FE)
                                ) {
                                    Text(
                                        text = "Processing",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0277BD),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onViewLiveTicket?.invoke(primaryBooking) ?: onViewBooking?.invoke(primaryBooking)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Live Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            SectionHeader(
                title = if (count > 1) "Active Procurements" else "Today's Procurement",
                onViewAll = onViewAll
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                activeList.forEach { bookingItem ->
                    val isItemQueueActive = normalizeStatus(bookingItem.status) in listOf(
                        BookingStatus.CHECKED_IN.name,
                        BookingStatus.WAITING.name,
                        BookingStatus.NOW_SERVING.name,
                        BookingStatus.PROCESSING.name
                    )
                    ActiveBookingCard(
                        booking = bookingItem,
                        queueData = if (bookingItem.trackingId == primaryBooking.trackingId) uiState.liveQueueData else null,
                        onClick = {
                            if (isItemQueueActive && onViewLiveTicket != null) {
                                onViewLiveTicket.invoke(bookingItem)
                            } else {
                                onViewBooking?.invoke(bookingItem) ?: onViewAll()
                            }
                        }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                EmptyBookingState(onBookSlot = onBookSlot)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Text(
            text = "View all",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E5E41),
            modifier = Modifier.clickable { onViewAll() }
        )
    }
}

@Composable
fun EmptyBookingState(onBookSlot: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8EEEA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No active procurement queue.",
                fontSize = 15.sp,
                color = MutedGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ready to schedule your procurement?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onBookSlot,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Book a Procurement Slot",
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
fun ActiveBookingCard(
    booking: Booking,
    queueData: LiveQueueData?,
    onClick: (() -> Unit)? = null
) {
    val normStatus = normalizeStatus(booking.status)
    val isConfirmed = normStatus == BookingStatus.CONFIRMED.name
    val isCheckedIn = normStatus == BookingStatus.CHECKED_IN.name
    val isWaiting = normStatus == BookingStatus.WAITING.name
    val isServing = normStatus == BookingStatus.NOW_SERVING.name
    val isProcessing = normStatus == BookingStatus.PROCESSING.name
    val isBooked = normStatus == BookingStatus.BOOKED.name

    val displayStatus = getDisplayStatus(booking)

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

    val formattedQty = if (booking.quantity % 1.0 == 0.0) {
        "${booking.quantity.toInt()}.0"
    } else {
        booking.quantity.toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE8EEEA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Centre and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = Charcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = locationSubtitle,
                        fontSize = 13.sp,
                        color = MutedGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

            // Status explanatory message
            if (isCheckedIn || isWaiting || isServing || isProcessing) {
                Spacer(modifier = Modifier.height(12.dp))
                val message = when {
                    isProcessing -> "Your procurement process has started."
                    isServing -> "Now serving at counter"
                    isWaiting -> "Waiting in queue"
                    isCheckedIn -> "Your booking has been checked in."
                    else -> ""
                }
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = statusTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Grid: Row 1 (Crop, Quantity, Booking ID)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DetailColumn(
                    label = "Crop",
                    value = booking.crop,
                    modifier = Modifier.weight(1f)
                )
                DetailColumn(
                    label = "Quantity",
                    value = "$formattedQty ${booking.quantityUnit.lowercase()}",
                    modifier = Modifier.weight(1.2f)
                )
                DetailColumn(
                    label = "Booking ID",
                    value = booking.trackingId,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Grid: Row 2 (Date, Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DetailColumn(
                    label = "Date",
                    value = formatHomeDate(booking.bookingDate),
                    modifier = Modifier.weight(1f)
                )
                DetailColumn(
                    label = "Time",
                    value = "${booking.slotStartTime} - ${booking.slotEndTime}",
                    modifier = Modifier.weight(2.4f)
                )
            }

            // Live Queue Metrics
            if ((isWaiting || isServing || isProcessing) && (queueData != null || booking.queueToken.isNotEmpty())) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE8EEEA))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tokenDisplay = queueData?.myToken?.ifEmpty { booking.queueToken }
                        ?: booking.queueToken.ifEmpty { booking.trackingId }
                    QueueMetric(label = "Your Token", value = tokenDisplay, highlight = true)
                    val currentServing = queueData?.currentServingToken ?: ""
                    if (currentServing.isNotEmpty()) {
                        QueueMetric(label = "Serving Token", value = currentServing)
                    }
                }

                if (isWaiting && queueData != null && queueData.myPosition > 0) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            val buttonText = when {
                isCheckedIn || isWaiting || isServing || isProcessing -> "View Live Details"
                else -> "View QR & Details"
            }
            val buttonIcon = when {
                isCheckedIn || isWaiting || isServing || isProcessing -> Icons.Rounded.Visibility
                else -> Icons.Rounded.QrCodeScanner
            }

            Button(
                onClick = { onClick?.invoke() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    buttonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MutedGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QueueMetric(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MutedGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF2E5E41) else Charcoal
        )
    }
}
