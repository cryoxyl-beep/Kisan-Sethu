package com.kisansethu.app.ui.booking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.data.ProcurementCentre
import com.kisansethu.app.data.QuantityUnit
import com.kisansethu.app.data.TimeSlot
import com.kisansethu.app.data.Booking
import com.kisansethu.app.utils.QrCodeGenerator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    farmerId: String,
    farmerName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingFlowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        if (uiState.currentStep == BookingStep.DATE || uiState.confirmedBooking != null) {
            onClose()
        } else {
            viewModel.goBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.confirmedBooking != null) "Booking Confirmed" else "Book a Procurement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E1E1E)
                    )
                },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == BookingStep.DATE || uiState.confirmedBooking != null) {
                            onClose()
                        } else {
                            viewModel.goBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1E1E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.confirmedBooking == null) {
                CompactStepIndicator(currentStep = uiState.currentStep)
            }

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                
                if (uiState.confirmedBooking != null) {
                    BookingSuccessStep(
                        booking = uiState.confirmedBooking!!,
                        onBackToHome = onClose
                    )
                } else {
                    when (uiState.currentStep) {
                        BookingStep.DATE -> DateSelectionStep(
                            selectedDate = uiState.selectedDate,
                            onDateSelected = { viewModel.onDateSelected(it) }
                        )
                        BookingStep.CENTRE -> CentreSelectionStep(
                            centres = uiState.availableCentres,
                            selectedCentre = uiState.selectedCentre,
                            onCentreSelected = { viewModel.onCentreSelected(it) }
                        )
                        BookingStep.SLOT -> TimeSlotSelectionStep(
                            slots = uiState.availableSlots,
                            selectedSlot = uiState.selectedSlot,
                            onSlotSelected = { viewModel.onSlotSelected(it) }
                        )
                        BookingStep.PRODUCE -> ProduceSelectionStep(
                            crops = uiState.crops,
                            selectedCrop = uiState.selectedCrop,
                            quantity = uiState.quantity,
                            quantityUnit = uiState.quantityUnit,
                            onCropSelected = { viewModel.onCropSelected(it) },
                            onQuantityChanged = { viewModel.onQuantityChanged(it) },
                            onUnitChanged = { viewModel.onQuantityUnitChanged(it) }
                        )
                        BookingStep.REVIEW -> ReviewBookingStep(
                            state = uiState,
                            onConfirm = { viewModel.confirmBooking(farmerId, farmerName) }
                        )
                    }
                }
            }

            if (uiState.confirmedBooking == null && uiState.currentStep != BookingStep.REVIEW) {
                BottomBarAction(
                    enabled = uiState.canProceed,
                    onClick = { viewModel.proceedToNextStep() },
                    text = "Continue"
                )
            }
        }
    }
}

@Composable
fun CompactStepIndicator(currentStep: BookingStep) {
    val ForestGreen = Color(0xFF1B3B26)
    val Inactive = Color(0xFFD5D9D6)
    // Visual progress matches mock: 3 dots; map 5 booking steps into 3 visual stages
    val visualIndex = when (currentStep) {
        BookingStep.DATE, BookingStep.CENTRE -> 0
        BookingStep.SLOT, BookingStep.PRODUCE -> 1
        BookingStep.REVIEW -> 2
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(3) { index ->
            val active = index <= visualIndex
            Box(
                modifier = Modifier
                    .size(if (index == visualIndex) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (active) ForestGreen else Inactive)
            )
            if (index < 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 6.dp)
                        .background(if (index < visualIndex) ForestGreen else Inactive)
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: BookingStep) {
    CompactStepIndicator(currentStep)
}

@Composable
fun BottomBarAction(enabled: Boolean, onClick: () -> Unit, text: String) {
    val ForestGreen = Color(0xFF1B3B26)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                disabledContainerColor = ForestGreen.copy(alpha = 0.4f)
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(20.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------
// STEPS
// ------------------------------------------------------------------------

@Composable
fun DateSelectionStep(selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val dates = remember { (0..14).map { today.plusDays(it.toLong()) } }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select Date",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 116.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dates) { date ->
                val isSelected = date == selectedDate
                val formatter = DateTimeFormatter.ofPattern("EEE\ndd MMM")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable { onDateSelected(date) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = date.format(formatter),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CentreSelectionStep(
    centres: List<ProcurementCentre>,
    selectedCentre: ProcurementCentre?,
    onCentreSelected: (ProcurementCentre) -> Unit
) {
    val ForestGreen = Color(0xFF1B3B26)
    val SoftMint = Color(0xFFE5F3EA)
    val MutedGray = Color(0xFF6E6E6E)
    var query by remember { mutableStateOf("") }
    val filtered = remember(centres, query) {
        if (query.isBlank()) centres
        else centres.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Select Procurement Centre",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a nearby centre",
                fontSize = 14.sp,
                color = MutedGray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF2F4F3))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color(0xFF9AA39D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF1E1E1E)
                    ),
                    cursorBrush = SolidColor(ForestGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search centre or district",
                                fontSize = 15.sp,
                                color = Color(0xFF9AA39D)
                            )
                        }
                        inner()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { centre ->
                val isSelected = centre == selectedCentre
                val isFull = centre.status == "Full"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isFull) { onCentreSelected(centre) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ForestGreen else Color(0xFFE8EEEA)
                    ),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                text = centre.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFull) Color(0xFF9AA39D) else Color(0xFF1E1E1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = centre.location,
                                fontSize = 13.sp,
                                color = if (isFull) Color(0xFFB0B8B3) else MutedGray
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotSelectionStep(
    slots: List<TimeSlot>,
    selectedSlot: TimeSlot?,
    onSlotSelected: (TimeSlot) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select Time Slot",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(slots) { slot ->
                val isSelected = slot == selectedSlot
                val isFull = !slot.isAvailable
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isFull) { onSlotSelected(slot) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = slot.getDisplayString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isFull) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${slot.remaining} remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=if(isFull) 0.5f else 1f)
                            )
                        }
                        
                        Text(
                            text = if (isFull) "Full" else "Available",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduceSelectionStep(
    crops: List<String>,
    selectedCrop: String,
    quantity: String,
    quantityUnit: QuantityUnit,
    onCropSelected: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onUnitChanged: (QuantityUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var customCrop by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 116.dp)
    ) {
        Text(
            text = "Produce Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCrop,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Crop") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                crops.forEach { crop ->
                    DropdownMenuItem(
                        text = { Text(crop) },
                        onClick = { 
                            onCropSelected(crop)
                            expanded = false 
                        }
                    )
                }
            }
        }
        
        if (selectedCrop == "Other") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = customCrop,
                onValueChange = { 
                    customCrop = it 
                    onCropSelected(it) // Actually we might want a separate state, but this works for simple UI
                },
                label = { Text("Enter Crop Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChanged,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Unit toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1.2f).height(64.dp) // Match text field height
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    QuantityUnit.entries.forEach { unit ->
                        val isSelected = quantityUnit == unit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onUnitChanged(unit) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit.name.lowercase().capitalize(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewBookingStep(state: BookingFlowState, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 116.dp)
    ) {
        Text(
            text = "Review Booking",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReviewItem("Centre", state.selectedCentre?.name ?: "")
                Spacer(modifier = Modifier.height(12.dp))
                ReviewItem("Date", state.selectedDate?.toString() ?: "")
                Spacer(modifier = Modifier.height(12.dp))
                ReviewItem("Time", state.selectedSlot?.getDisplayString() ?: "")
                Spacer(modifier = Modifier.height(12.dp))
                ReviewItem("Crop", state.selectedCrop)
                Spacer(modifier = Modifier.height(12.dp))
                ReviewItem("Quantity", "${state.quantity} ${state.quantityUnit.name.lowercase()} (${state.quantityKg} kg)")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (state.submissionError != null) {
            Text(
                text = state.submissionError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Button(
            onClick = onConfirm,
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (state.isSubmitting) "Confirming..." else "Confirm Booking", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BookingSuccessStep(booking: Booking, onBackToHome: () -> Unit) {
    val qrCodeBitmap = remember(booking.trackingId) {
        QrCodeGenerator.generateQrCode(booking.trackingId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 116.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.CheckCircle, 
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Booking Confirmed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReviewItem("Centre", booking.centreName)
                Spacer(modifier = Modifier.height(8.dp))
                ReviewItem("Date & Time", "${booking.bookingDate} | ${booking.slotStartTime} - ${booking.slotEndTime}")
                Spacer(modifier = Modifier.height(8.dp))
                ReviewItem("Produce", "${booking.quantity} ${booking.quantityUnit.lowercase()} ${booking.crop}")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tracking ID: ${booking.trackingId}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (qrCodeBitmap != null) {
            Image(
                bitmap = qrCodeBitmap,
                contentDescription = "QR Code",
                modifier = Modifier.size(150.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Show this QR code at the procurement centre.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Home", style = MaterialTheme.typography.titleMedium)
        }
    }
}
