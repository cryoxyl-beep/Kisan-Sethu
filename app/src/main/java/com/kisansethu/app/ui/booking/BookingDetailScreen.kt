package com.kisansethu.app.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.loadingindicator.LoadingIndicator
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.normalizeStatus
import com.kisansethu.app.utils.QrCodeGenerator

private val ForestGreen = Color(0xFF1B3B26)
private val SoftMint = Color(0xFFE5F3EA)
private val PageBg = Color(0xFFF7F8F7)
private val Charcoal = Color(0xFF1E1E1E)
private val MutedGray = Color(0xFF6E6E6E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking,
    onBack: () -> Unit,
    onViewLiveDetails: ((Booking) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BookingDetailViewModel = viewModel()
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

    val normStatus = normalizeStatus(displayBooking.status)
    val displayStatus = getDisplayStatus(displayBooking)

    val statusBgColor = when (normStatus) {
        BookingStatus.COMPLETED.name, BookingStatus.CONFIRMED.name -> SoftMint
        BookingStatus.PROCESSING.name -> Color(0xFFE1F5FE)
        BookingStatus.NOW_SERVING.name -> Color(0xFFE8F5E9)
        BookingStatus.WAITING.name -> Color(0xFFFFF3E0)
        BookingStatus.CHECKED_IN.name, BookingStatus.BOOKED.name -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    val statusTextColor = when (normStatus) {
        BookingStatus.COMPLETED.name, BookingStatus.CONFIRMED.name -> Color(0xFF1F6B45)
        BookingStatus.PROCESSING.name -> Color(0xFF0277BD)
        BookingStatus.NOW_SERVING.name -> Color(0xFF2E7D32)
        BookingStatus.WAITING.name -> Color(0xFFE65100)
        BookingStatus.CHECKED_IN.name, BookingStatus.BOOKED.name -> Color(0xFF1565C0)
        else -> Color(0xFF757575)
    }

    val statusMessage = when (normStatus) {
        BookingStatus.PROCESSING.name -> "Your procurement process has started."
        BookingStatus.NOW_SERVING.name -> "Now serving at procurement counter."
        BookingStatus.WAITING.name -> if (displayBooking.queueToken.isNotEmpty()) {
            "Waiting in queue • Token ${displayBooking.queueToken}"
        } else {
            "Waiting in queue"
        }
        BookingStatus.CHECKED_IN.name -> "Your booking has been checked in."
        BookingStatus.CONFIRMED.name -> "Your booking has been confirmed."
        BookingStatus.BOOKED.name -> if (displayStatus == "BOOKED") "Booked for today" else "Scheduled booking"
        BookingStatus.COMPLETED.name -> "Procurement completed successfully."
        else -> ""
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Booking Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Charcoal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Charcoal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBg
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            val isInitialBlank = booking.trackingId.isEmpty() && uiState.booking == null
            if (uiState.isLoading && isInitialBlank) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AndroidView(
                        factory = { context -> LoadingIndicator(context) },
                        update = { view -> view.setIndicatorColor(ForestGreen.toArgb()) },
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else if (uiState.error != null && isInitialBlank) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Unable to load booking details.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startListening(booking.trackingId) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main Booking Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8EEEA)),
                        shadowElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Centre Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
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
                                        text = displayBooking.centreName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFE8EEEA))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Status Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status",
                                    fontSize = 14.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = statusBgColor
                                ) {
                                    Text(
                                        text = displayStatus,
                                        fontSize = 12.sp,
                                        color = statusTextColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            if (statusMessage.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = statusMessage,
                                    fontSize = 13.sp,
                                    color = statusTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (displayBooking.queueToken.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Queue Token",
                                        fontSize = 14.sp,
                                        color = MutedGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = displayBooking.queueToken,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFE8EEEA))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Details Grid
                            DetailRow(
                                label = "Date",
                                value = formatBookingDate(displayBooking.bookingDate)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow(
                                label = "Time Slot",
                                value = "${displayBooking.slotStartTime} - ${displayBooking.slotEndTime}"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val formattedQty = if (displayBooking.quantity % 1.0 == 0.0) {
                                "${displayBooking.quantity.toInt()}.0"
                            } else {
                                displayBooking.quantity.toString()
                            }
                            DetailRow(
                                label = "Produce",
                                value = "${displayBooking.crop} • $formattedQty ${displayBooking.quantityUnit.lowercase()}"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow(
                                label = "Booking ID",
                                value = displayBooking.trackingId,
                                isHighlight = true
                            )
                        }
                    }

                    val isLiveQueueActive = normStatus in listOf(
                        BookingStatus.CHECKED_IN.name,
                        BookingStatus.WAITING.name,
                        BookingStatus.NOW_SERVING.name,
                        BookingStatus.PROCESSING.name
                    )

                    if (isLiveQueueActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onViewLiveDetails?.invoke(displayBooking) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Live Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (normStatus == BookingStatus.COMPLETED.name) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { onViewLiveDetails?.invoke(displayBooking) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            border = BorderStroke(1.5.dp, ForestGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Procurement Ticket",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Code Card
                    if (qrCodeBitmap != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE8EEEA)),
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    bitmap = qrCodeBitmap,
                                    contentDescription = "Booking QR Code",
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = displayBooking.trackingId,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scan this QR code at the procurement centre counter.",
                                    fontSize = 13.sp,
                                    color = MutedGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MutedGray,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) ForestGreen else Charcoal
        )
    }
}
