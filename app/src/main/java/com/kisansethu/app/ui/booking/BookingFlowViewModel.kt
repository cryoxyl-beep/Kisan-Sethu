package com.kisansethu.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.ProcurementCentre
import com.kisansethu.app.data.QuantityUnit
import com.kisansethu.app.data.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class BookingStep(val title: String) {
    DATE("Date"),
    CENTRE("Centre"),
    SLOT("Slot"),
    PRODUCE("Produce"),
    REVIEW("Review")
}

data class BookingFlowState(
    val currentStep: BookingStep = BookingStep.DATE,
    val selectedDate: LocalDate? = null,
    val selectedCentre: ProcurementCentre? = null,
    val selectedSlot: TimeSlot? = null,
    val selectedCrop: String = "",
    val quantity: String = "",
    val quantityUnit: QuantityUnit = QuantityUnit.QUINTAL,
    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val confirmedBooking: Booking? = null,
    // Mock data for UI
    val availableCentres: List<ProcurementCentre> = emptyList(),
    val availableSlots: List<TimeSlot> = emptyList(),
    val crops: List<String> = listOf("Paddy", "Wheat", "Maize", "Cotton", "Soybean", "Groundnut", "Bajra", "Jowar", "Other")
) {
    val quantityKg: Double? get() {
        val qty = quantity.toDoubleOrNull() ?: return null
        return if (quantityUnit == QuantityUnit.QUINTAL) qty * 100.0 else qty
    }
    
    val canProceed: Boolean get() = when (currentStep) {
        BookingStep.DATE -> selectedDate != null
        BookingStep.CENTRE -> selectedCentre != null
        BookingStep.SLOT -> selectedSlot != null
        BookingStep.PRODUCE -> selectedCrop.isNotEmpty() && quantity.isNotEmpty() && (quantity.toDoubleOrNull() ?: 0.0) > 0.0
        BookingStep.REVIEW -> true
    }
}

class BookingFlowViewModel : ViewModel() {
    private val repository = BookingRepository()
    private val _uiState = MutableStateFlow(BookingFlowState())
    val uiState: StateFlow<BookingFlowState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate) {
        _uiState.update { 
            it.copy(
                selectedDate = date, 
                // Load mock centres when date is selected
                availableCentres = loadMockCentres()
            ) 
        }
    }

    fun onCentreSelected(centre: ProcurementCentre) {
        _uiState.update { 
            it.copy(
                selectedCentre = centre,
                // Load mock slots when centre is selected
                availableSlots = loadMockSlots()
            ) 
        }
    }

    fun onSlotSelected(slot: TimeSlot) {
        if (slot.isAvailable) {
            _uiState.update { it.copy(selectedSlot = slot) }
        }
    }

    fun onCropSelected(crop: String) {
        _uiState.update { it.copy(selectedCrop = crop) }
    }

    fun onQuantityChanged(qty: String) {
        _uiState.update { it.copy(quantity = qty) }
    }

    fun onQuantityUnitChanged(unit: QuantityUnit) {
        _uiState.update { it.copy(quantityUnit = unit) }
    }

    fun proceedToNextStep() {
        _uiState.update { state ->
            val nextStep = when (state.currentStep) {
                BookingStep.DATE -> BookingStep.CENTRE
                BookingStep.CENTRE -> BookingStep.SLOT
                BookingStep.SLOT -> BookingStep.PRODUCE
                BookingStep.PRODUCE -> BookingStep.REVIEW
                BookingStep.REVIEW -> BookingStep.REVIEW
            }
            state.copy(currentStep = nextStep)
        }
    }

    fun goBack() {
        _uiState.update { state ->
            val prevStep = when (state.currentStep) {
                BookingStep.DATE -> BookingStep.DATE
                BookingStep.CENTRE -> BookingStep.DATE
                BookingStep.SLOT -> BookingStep.CENTRE
                BookingStep.PRODUCE -> BookingStep.SLOT
                BookingStep.REVIEW -> BookingStep.PRODUCE
            }
            state.copy(currentStep = prevStep)
        }
    }
    
    fun confirmBooking(farmerId: String, farmerName: String) {
        val state = _uiState.value
        if (!state.canProceed || state.currentStep != BookingStep.REVIEW) return
        
        _uiState.update { it.copy(isSubmitting = true, submissionError = null) }
        
        viewModelScope.launch {
            val booking = Booking(
                farmerId = farmerId,
                farmerName = farmerName,
                centreId = state.selectedCentre!!.id,
                centreName = state.selectedCentre.name,
                bookingDate = state.selectedDate.toString(),
                slotId = state.selectedSlot!!.id,
                slotStartTime = state.selectedSlot.startTime,
                slotEndTime = state.selectedSlot.endTime,
                crop = state.selectedCrop,
                quantity = state.quantity.toDoubleOrNull() ?: 0.0,
                quantityUnit = state.quantityUnit.name,
                quantityKg = state.quantityKg ?: 0.0,
                status = BookingStatus.BOOKED.name
            )
            
            val result = repository.createBooking(booking)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isSubmitting = false, 
                        confirmedBooking = result.getOrNull()
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isSubmitting = false, 
                        submissionError = result.exceptionOrNull()?.message ?: "Unknown error"
                    ) 
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(submissionError = null) }
    }

    private fun loadMockCentres(): List<ProcurementCentre> {
        return listOf(
            ProcurementCentre("C1", "Medchal Procurement Centre", "Medchal, Telangana", 18, "Open"),
            ProcurementCentre("C2", "Kukatpally APMC", "Kukatpally, Telangana", 12, "Open"),
            ProcurementCentre("C3", "Shamshabad Krishi Market", "Shamshabad, Telangana", 0, "Full"),
            ProcurementCentre("C4", "Gachibowli Farmers Market", "Gachibowli, Telangana", 5, "Open")
        )
    }

    private fun loadMockSlots(): List<TimeSlot> {
        return listOf(
            TimeSlot("S1", "09:00 AM", "09:30 AM", 8, 8), // Full
            TimeSlot("S2", "09:30 AM", "10:00 AM", 8, 6),
            TimeSlot("S3", "10:00 AM", "10:30 AM", 8, 2),
            TimeSlot("S4", "10:30 AM", "11:00 AM", 8, 5),
            TimeSlot("S5", "11:00 AM", "11:30 AM", 8, 8), // Full
            TimeSlot("S6", "11:30 AM", "12:00 PM", 8, 0),
            TimeSlot("S7", "12:00 PM", "12:30 PM", 8, 0)
        )
    }
}
