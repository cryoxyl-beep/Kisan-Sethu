package com.kisansethu.app.ui.booking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SlotBookingsScreen(
    farmerId: String,
    farmerName: String,
    modifier: Modifier = Modifier
) {
    var showWizard by remember { mutableStateOf(false) }

    if (showWizard) {
        BookingFlowScreen(
            farmerId = farmerId,
            farmerName = farmerName,
            onClose = { showWizard = false },
            modifier = modifier
        )
    } else {
        SlotBookingsListScreen(
            farmerId = farmerId,
            onBookSlotClick = { showWizard = true },
            modifier = modifier
        )
    }
}
