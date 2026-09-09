package com.kisansethu.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.isCompletedStatus
import com.kisansethu.app.data.isUpcomingStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SlotBookingsState(
    val isLoading: Boolean = true,
    val upcomingBookings: List<Booking> = emptyList(),
    val completedBookings: List<Booking> = emptyList(),
    val error: String? = null
)

class SlotBookingsViewModel : ViewModel() {
    private val repository = BookingRepository()
    private val _uiState = MutableStateFlow(SlotBookingsState())
    val uiState: StateFlow<SlotBookingsState> = _uiState.asStateFlow()

    private var listenerJob: Job? = null
    private var currentFarmerId: String? = null

    fun loadBookings(farmerId: String) {
        if (currentFarmerId == farmerId && listenerJob?.isActive == true) return
        currentFarmerId = farmerId
        
        listenerJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        listenerJob = viewModelScope.launch {
            repository.getFarmerBookingsRealtime(farmerId).collect { result ->
                if (result.isSuccess) {
                    val allBookings = result.getOrNull() ?: emptyList()
                    val upcoming = allBookings
                        .filter { isUpcomingStatus(it.status) }
                        .sortedWith(compareBy({ it.bookingDate }, { it.slotStartTime }))
                    val completed = allBookings
                        .filter { isCompletedStatus(it.status) }
                        .sortedWith(compareByDescending<Booking> { it.bookingDate }.thenByDescending { it.createdAt })

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            upcomingBookings = upcoming,
                            completedBookings = completed,
                            error = null
                        )
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to load bookings"
                    _uiState.update {
                        it.copy(isLoading = false, error = errorMsg)
                    }
                }
            }
        }
    }

    fun reset() {
        listenerJob?.cancel()
        listenerJob = null
        currentFarmerId = null
        _uiState.value = SlotBookingsState()
    }

    override fun onCleared() {
        super.onCleared()
        listenerJob?.cancel()
        listenerJob = null
    }
}
