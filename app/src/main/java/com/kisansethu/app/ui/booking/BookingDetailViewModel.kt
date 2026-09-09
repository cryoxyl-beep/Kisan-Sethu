package com.kisansethu.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingDetailState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val error: String? = null
)

class BookingDetailViewModel : ViewModel() {
    private val repository = BookingRepository()
    private val _uiState = MutableStateFlow(BookingDetailState())
    val uiState: StateFlow<BookingDetailState> = _uiState.asStateFlow()

    private var currentTrackingId: String? = null
    private var listenerJob: Job? = null

    fun startListening(trackingId: String) {
        if (currentTrackingId == trackingId && listenerJob?.isActive == true) return
        currentTrackingId = trackingId
        
        listenerJob?.cancel()
        
        _uiState.update { it.copy(isLoading = true, error = null) }

        listenerJob = viewModelScope.launch {
            repository.getAuthoritativeBookingRealtime(trackingId).collect { result ->
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, booking = result.getOrNull()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Unable to load booking details.") }
                }
            }
        }
    }

    fun stopListening() {
        listenerJob?.cancel()
        listenerJob = null
        currentTrackingId = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
