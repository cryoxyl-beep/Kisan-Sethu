package com.kisansethu.app.data

enum class BookingStatus {
    BOOKED,
    CHECKED_IN,
    WAITING,
    PROCESSING,
    COMPLETED,
    MISSED,
    CANCELLED
}

enum class QuantityUnit {
    KG,
    QUINTAL
}

data class ProcurementCentre(
    val id: String,
    val name: String,
    val location: String,
    val availableSlots: Int,
    val status: String
)

data class TimeSlot(
    val id: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val booked: Int
) {
    val remaining: Int get() = capacity - booked
    val isAvailable: Boolean get() = remaining > 0
    
    fun getDisplayString(): String {
        return "$startTime - $endTime"
    }
}

data class Booking(
    val trackingId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val centreId: String = "",
    val centreName: String = "",
    val bookingDate: String = "", // ISO format YYYY-MM-DD
    val slotId: String = "",
    val slotStartTime: String = "",
    val slotEndTime: String = "",
    val crop: String = "",
    val quantity: Double = 0.0,
    val quantityUnit: String = QuantityUnit.QUINTAL.name,
    val quantityKg: Double = 0.0,
    val status: String = BookingStatus.BOOKED.name,
    val qrCodeData: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
