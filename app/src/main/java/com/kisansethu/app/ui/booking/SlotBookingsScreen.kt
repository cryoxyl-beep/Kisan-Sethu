package com.kisansethu.app.ui.booking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.kisansethu.app.data.Booking

enum class SlotBookingRoute { LIST, WIZARD, DETAIL }

@Composable
fun SlotBookingsScreen(
    farmerId: String,
    farmerName: String,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf(SlotBookingRoute.LIST) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    when (currentRoute) {
        SlotBookingRoute.WIZARD -> {
            BookingFlowScreen(
                farmerId = farmerId,
                farmerName = farmerName,
                onClose = { currentRoute = SlotBookingRoute.LIST },
                modifier = modifier
            )
        }
        SlotBookingRoute.DETAIL -> {
            selectedBooking?.let { booking ->
                BookingDetailScreen(
                    booking = booking,
                    onBack = { currentRoute = SlotBookingRoute.LIST },
                    modifier = modifier
                )
            } ?: run {
                currentRoute = SlotBookingRoute.LIST
            }
        }
        SlotBookingRoute.LIST -> {
            SlotBookingsListScreen(
                farmerId = farmerId,
                onBookSlotClick = { currentRoute = SlotBookingRoute.WIZARD },
                onViewBookingClick = { booking ->
                    selectedBooking = booking
                    currentRoute = SlotBookingRoute.DETAIL
                },
                modifier = modifier
            )
        }
    }
}
