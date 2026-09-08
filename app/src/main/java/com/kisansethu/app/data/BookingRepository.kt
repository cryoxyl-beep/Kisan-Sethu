package com.kisansethu.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    
    // Generates a unique tracking ID like KS26A7F42Q
    // "KS" + Year + 6 random alphanumeric chars
    private fun generateTrackingId(): String {
        val year = java.time.Year.now().value.toString().takeLast(2)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        val suffix = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "KS$year$suffix"
    }
    
    suspend fun createBooking(booking: Booking): Result<Booking> {
        return try {
            // Ensure uniqueness
            var trackingId = generateTrackingId()
            var exists = true
            while (exists) {
                val doc = bookingsCollection.document(trackingId).get().await()
                if (!doc.exists()) {
                    exists = false
                } else {
                    trackingId = generateTrackingId()
                }
            }
            
            val finalBooking = booking.copy(
                trackingId = trackingId,
                qrCodeData = trackingId // Only encoding tracking ID
            )
            
            bookingsCollection.document(trackingId).set(finalBooking).await()
            Result.success(finalBooking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingBookings(farmerId: String): Result<List<Booking>> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("farmerId", farmerId)
                .whereEqualTo("status", BookingStatus.BOOKED.name)
                .get()
                .await()
            val bookings = snapshot.toObjects(Booking::class.java).sortedBy { it.bookingDate }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCompletedBookings(farmerId: String): Result<List<Booking>> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("farmerId", farmerId)
                .whereEqualTo("status", BookingStatus.COMPLETED.name)
                .get()
                .await()
            val bookings = snapshot.toObjects(Booking::class.java).sortedByDescending { it.bookingDate }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
