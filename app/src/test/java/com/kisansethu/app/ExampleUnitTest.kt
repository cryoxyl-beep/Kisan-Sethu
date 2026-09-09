package com.kisansethu.app

import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.QueueEntry
import com.kisansethu.app.data.getAuthoritativeStatus
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.isCompletedStatus
import com.kisansethu.app.data.isUpcomingStatus
import com.kisansethu.app.data.mergeBookingWithQueueEntry
import com.kisansethu.app.data.normalizeStatus
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.util.Date

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testNormalizeStatusCasingAndDelimiters() {
        assertEquals("BOOKED", normalizeStatus("booked"))
        assertEquals("BOOKED", normalizeStatus("BOOKED"))
        assertEquals("BOOKED", normalizeStatus(" Booked "))

        assertEquals("CONFIRMED", normalizeStatus("confirmed"))
        assertEquals("CONFIRMED", normalizeStatus("CONFIRMED"))

        assertEquals("CHECKED_IN", normalizeStatus("checked_in"))
        assertEquals("CHECKED_IN", normalizeStatus("CHECKED_IN"))
        assertEquals("CHECKED_IN", normalizeStatus("CHECKED IN"))
        assertEquals("CHECKED_IN", normalizeStatus("checked in"))
        assertEquals("CHECKED_IN", normalizeStatus("checked-in"))
        assertEquals("CHECKED_IN", normalizeStatus("checkedin"))

        assertEquals("WAITING", normalizeStatus("waiting"))
        assertEquals("WAITING", normalizeStatus("WAITING"))
        assertEquals("WAITING", normalizeStatus("WAITING_IN_QUEUE"))
        assertEquals("WAITING", normalizeStatus("waiting in queue"))
        assertEquals("WAITING", normalizeStatus("IN_QUEUE"))

        assertEquals("NOW_SERVING", normalizeStatus("now_serving"))
        assertEquals("NOW_SERVING", normalizeStatus("NOW_SERVING"))
        assertEquals("NOW_SERVING", normalizeStatus("now serving"))
        assertEquals("NOW_SERVING", normalizeStatus("NOW SERVING"))
        assertEquals("NOW_SERVING", normalizeStatus("serving"))

        assertEquals("PROCESSING", normalizeStatus("processing"))
        assertEquals("PROCESSING", normalizeStatus("PROCESSING"))
        assertEquals("PROCESSING", normalizeStatus("in_progress"))
        assertEquals("PROCESSING", normalizeStatus("IN PROGRESS"))

        assertEquals("COMPLETED", normalizeStatus("completed"))
        assertEquals("COMPLETED", normalizeStatus("COMPLETED"))
        assertEquals("COMPLETED", normalizeStatus("Completed"))
        assertEquals("COMPLETED", normalizeStatus("complete"))
        assertEquals("COMPLETED", normalizeStatus("DONE"))

        assertEquals("CANCELLED", normalizeStatus("cancelled"))
        assertEquals("CANCELLED", normalizeStatus("canceled"))
        assertEquals("CANCELLED", normalizeStatus("CANCELLED"))

        assertEquals("MISSED", normalizeStatus("missed"))

        assertEquals("BOOKED", normalizeStatus(null))
        assertEquals("BOOKED", normalizeStatus(""))
        assertEquals("BOOKED", normalizeStatus("   "))
    }

    @Test
    fun testAuthoritativeStatusLifecycleProgression() {
        // Step 1: CONFIRMED
        assertEquals("CONFIRMED", getAuthoritativeStatus("CONFIRMED", null))

        // Step 2: CHECKED_IN
        assertEquals("CHECKED_IN", getAuthoritativeStatus("CONFIRMED", "CHECKED_IN"))
        assertEquals("CHECKED_IN", getAuthoritativeStatus("CHECKED_IN", null))

        // Step 3: WAITING
        assertEquals("WAITING", getAuthoritativeStatus("CHECKED_IN", "WAITING"))

        // Step 4: NOW_SERVING
        assertEquals("NOW_SERVING", getAuthoritativeStatus("CHECKED_IN", "NOW_SERVING"))
        assertEquals("NOW_SERVING", getAuthoritativeStatus("WAITING", "NOW_SERVING"))

        // Step 5: PROCESSING
        assertEquals("PROCESSING", getAuthoritativeStatus("CHECKED_IN", "PROCESSING"))
        assertEquals("PROCESSING", getAuthoritativeStatus("NOW_SERVING", "PROCESSING"))

        // Step 6: COMPLETED (the live bug scenario: booking is CHECKED_IN, queue is COMPLETED)
        assertEquals("COMPLETED", getAuthoritativeStatus("CHECKED_IN", "COMPLETED"))
        assertEquals("COMPLETED", getAuthoritativeStatus("PROCESSING", "COMPLETED"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "PROCESSING")) // completed booking takes precedence over stale queue
    }

    @Test
    fun testTerminalStatusesPreserved() {
        assertEquals("CANCELLED", getAuthoritativeStatus("CANCELLED", "PROCESSING"))
        assertEquals("MISSED", getAuthoritativeStatus("MISSED", "WAITING"))
        // Completed booking is terminal and cannot be reverted by active or stale queue entry
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "PROCESSING"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "WAITING"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "CANCELLED"))
    }

    @Test
    fun testNormalizeStatusScheduledAndQueueSynonyms() {
        // SCHEDULED represents a BOOKED procurement not occurring today
        assertEquals("BOOKED", normalizeStatus("SCHEDULED"))
        assertEquals("BOOKED", normalizeStatus("scheduled"))
        assertEquals("BOOKED", normalizeStatus("SCHEDULED_BOOKING"))
        assertTrue(isUpcomingStatus("SCHEDULED"))
        assertFalse(isCompletedStatus("SCHEDULED"))

        assertEquals("WAITING", normalizeStatus("QUEUED"))
        assertEquals("WAITING", normalizeStatus("queued"))

        assertEquals("COMPLETED", normalizeStatus("FINISHED"))
        assertEquals("COMPLETED", normalizeStatus("finished"))
        assertEquals("COMPLETED", normalizeStatus("PROCURED"))
        assertEquals("COMPLETED", normalizeStatus("procured"))

        assertEquals("CANCELLED", normalizeStatus("REJECTED"))
        assertEquals("CANCELLED", normalizeStatus("rejected"))
    }

    @Test
    fun testUpcomingVsCompletedClassification() {
        val upcomingStatuses = listOf(
            BookingStatus.BOOKED.name,
            BookingStatus.CONFIRMED.name,
            BookingStatus.CHECKED_IN.name,
            BookingStatus.WAITING.name,
            BookingStatus.NOW_SERVING.name,
            BookingStatus.PROCESSING.name,
            "checked in",
            "now serving",
            "in progress"
        )

        for (status in upcomingStatuses) {
            assertTrue("Expected $status to be upcoming", isUpcomingStatus(status))
            assertFalse("Expected $status not to be completed", isCompletedStatus(status))
        }

        val completedStatuses = listOf(
            BookingStatus.COMPLETED.name,
            "completed",
            "Completed",
            "done",
            BookingStatus.CANCELLED.name,
            BookingStatus.MISSED.name
        )

        for (status in completedStatuses) {
            assertTrue("Expected $status to be completed", isCompletedStatus(status))
            assertFalse("Expected $status not to be upcoming", isUpcomingStatus(status))
        }
    }

    @Test
    fun testScheduledBookingLogic() {
        val today = LocalDate.now().toString()
        val futureDate = LocalDate.now().plusDays(3).toString()
        val pastDate = LocalDate.now().minusDays(1).toString()

        // BOOKED today -> "BOOKED"
        val bookedToday = Booking(status = "BOOKED", bookingDate = today)
        assertEquals("BOOKED", getDisplayStatus(bookedToday))

        // BOOKED not today -> "SCHEDULED"
        val bookedFuture = Booking(status = "BOOKED", bookingDate = futureDate)
        assertEquals("SCHEDULED", getDisplayStatus(bookedFuture))

        val bookedPast = Booking(status = "BOOKED", bookingDate = pastDate)
        assertEquals("SCHEDULED", getDisplayStatus(bookedPast))

        // Status takes priority over date: CONFIRMED/CHECKED_IN/PROCESSING in future should NOT be "SCHEDULED"
        val confirmedFuture = Booking(status = "CONFIRMED", bookingDate = futureDate)
        assertEquals("CONFIRMED", getDisplayStatus(confirmedFuture))

        val checkedInFuture = Booking(status = "CHECKED_IN", bookingDate = futureDate)
        assertEquals("CHECKED IN", getDisplayStatus(checkedInFuture))

        val waitingFuture = Booking(status = "WAITING", bookingDate = futureDate)
        assertEquals("WAITING", getDisplayStatus(waitingFuture))

        val nowServingFuture = Booking(status = "NOW_SERVING", bookingDate = futureDate)
        assertEquals("NOW SERVING", getDisplayStatus(nowServingFuture))

        val processingFuture = Booking(status = "PROCESSING", bookingDate = futureDate)
        assertEquals("PROCESSING", getDisplayStatus(processingFuture))

        val completedFuture = Booking(status = "COMPLETED", bookingDate = futureDate)
        assertEquals("COMPLETED", getDisplayStatus(completedFuture))
    }

    @Test
    fun testMergeBookingWithQueueEntry() {
        val checkInDate = Date(1788944590000L)
        val booking = Booking(
            trackingId = "KS26LV7WI9",
            farmerId = "ZVMXMZ",
            farmerName = "Sai Santosh",
            centreId = "C1",
            centreName = "Medchal Procurement Centre",
            bookingDate = "2026-09-09",
            crop = "Maize",
            quantity = 555.0,
            status = "CHECKED_IN"
        )

        val queueEntry = QueueEntry(
            id = "KS26LV7WI9",
            bookingId = "KS26LV7WI9",
            trackingId = "KS26LV7WI9",
            farmerId = "ZVMXMZ",
            tokenLabel = "MP-002",
            tokenNumber = 2L,
            status = "COMPLETED",
            checkInTime = checkInDate
        )

        val merged = mergeBookingWithQueueEntry(booking, queueEntry)

        assertEquals("COMPLETED", merged.status)
        assertEquals("MP-002", merged.queueToken)
        assertEquals(checkInDate, merged.checkedInAt)
        assertEquals("KS26LV7WI9", merged.trackingId)
        assertEquals("Medchal Procurement Centre", merged.centreName)
        assertEquals(555.0, merged.quantity, 0.001)

        // Verify it categorizes into Completed section
        assertTrue(isCompletedStatus(merged.status))
        assertFalse(isUpcomingStatus(merged.status))
    }

    @Test
    fun testFullLifecycleRequirement15Sequence() {
        // Step 1: Farmer has CONFIRMED booking
        var booking = Booking(
            trackingId = "KS26TEST01",
            farmerId = "FARMER1",
            status = "CONFIRMED"
        )
        assertEquals("CONFIRMED", getAuthoritativeStatus(booking.status, null))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 2: Admin changes it to CHECKED_IN
        var queue = QueueEntry(bookingId = "KS26TEST01", trackingId = "KS26TEST01", status = "CHECKED_IN", tokenLabel = "TK-101")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("CHECKED_IN", booking.status)
        assertEquals("CHECKED IN", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 3: Admin moves it to WAITING
        queue = queue.copy(status = "WAITING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("WAITING", booking.status)
        assertEquals("WAITING", getDisplayStatus(booking))
        assertEquals("TK-101", booking.queueToken)
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 4: Admin moves it to NOW_SERVING
        queue = queue.copy(status = "NOW_SERVING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("NOW_SERVING", booking.status)
        assertEquals("NOW SERVING", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 5: Admin moves it to PROCESSING
        queue = queue.copy(status = "PROCESSING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("PROCESSING", booking.status)
        assertEquals("PROCESSING", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 6: Admin completes the procurement
        queue = queue.copy(status = "COMPLETED")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("COMPLETED", booking.status)
        assertEquals("COMPLETED", getDisplayStatus(booking))
        assertFalse(isUpcomingStatus(booking.status))
        assertTrue(isCompletedStatus(booking.status))
    }
}