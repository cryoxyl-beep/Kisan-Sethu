package com.kisansethu.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

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

    fun loadBookings(farmerId: String) {
        if (listenerJob != null) return // Already listening
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        listenerJob = viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.getUpcomingBookingsRealtime(farmerId),
                repository.getCompletedBookingsRealtime(farmerId)
            ) { upcomingResult, completedResult ->
                if (upcomingResult.isSuccess && completedResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            upcomingBookings = upcomingResult.getOrNull() ?: emptyList(),
                            completedBookings = completedResult.getOrNull() ?: emptyList()
                        )
                    }
                } else {
                    val errorMsg = upcomingResult.exceptionOrNull()?.message 
                        ?: completedResult.exceptionOrNull()?.message 
                        ?: "Failed to load bookings"
                    _uiState.update {
                        it.copy(isLoading = false, error = errorMsg)
                    }
                }
            }.collect { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerJob?.cancel()
    }
}
