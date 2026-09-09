package com.kisansethu.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
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
    
    fun getUpcomingBookingsRealtime(farmerId: String): kotlinx.coroutines.flow.Flow<Result<List<Booking>>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = bookingsCollection
            .whereEqualTo("farmerId", farmerId)
            .whereIn("status", listOf(BookingStatus.BOOKED.name, BookingStatus.CONFIRMED.name, BookingStatus.CHECKED_IN.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java).sortedBy { it.bookingDate }
                    trySend(Result.success(bookings))
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    fun getCompletedBookingsRealtime(farmerId: String): kotlinx.coroutines.flow.Flow<Result<List<Booking>>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = bookingsCollection
            .whereEqualTo("farmerId", farmerId)
            .whereIn("status", listOf(
                BookingStatus.COMPLETED.name, 
                BookingStatus.CANCELLED.name, 
                BookingStatus.MISSED.name
            ))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java).sortedByDescending { it.bookingDate }
                    trySend(Result.success(bookings))
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getBookingRealtime(trackingId: String): kotlinx.coroutines.flow.Flow<Result<Booking>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = bookingsCollection.document(trackingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val booking = snapshot.toObject(Booking::class.java)
                if (booking != null) {
                    trySend(Result.success(booking))
                } else {
                    trySend(Result.failure(Exception("Failed to parse booking")))
                }
            } else {
                trySend(Result.failure(Exception("Booking not found")))
            }
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}
