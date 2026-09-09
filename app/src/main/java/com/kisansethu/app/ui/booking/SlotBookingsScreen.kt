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
    initialRoute: SlotBookingRoute = SlotBookingRoute.LIST,
    initialSelectedBooking: Booking? = null,
    onResetInitialRoute: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember(initialRoute) { mutableStateOf(initialRoute) }
    var selectedBooking by remember(initialSelectedBooking) { mutableStateOf(initialSelectedBooking) }

    when (currentRoute) {
        SlotBookingRoute.WIZARD -> {
            androidx.activity.compose.BackHandler {
                currentRoute = SlotBookingRoute.LIST
                onResetInitialRoute?.invoke()
            }
            BookingFlowScreen(
                farmerId = farmerId,
                farmerName = farmerName,
                onClose = {
                    currentRoute = SlotBookingRoute.LIST
                    onResetInitialRoute?.invoke()
                },
                modifier = modifier
            )
        }
        SlotBookingRoute.DETAIL -> {
            androidx.activity.compose.BackHandler {
                selectedBooking = null
                currentRoute = SlotBookingRoute.LIST
                onResetInitialRoute?.invoke()
            }
            selectedBooking?.let { booking ->
                BookingDetailScreen(
                    booking = booking,
                    onBack = {
                        selectedBooking = null
                        currentRoute = SlotBookingRoute.LIST
                        onResetInitialRoute?.invoke()
                    },
                    modifier = modifier
                )
            } ?: run {
                currentRoute = SlotBookingRoute.LIST
            }
        }
        SlotBookingRoute.LIST -> {
            SlotBookingsListScreen(
                farmerId = farmerId,
                farmerName = farmerName,
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
