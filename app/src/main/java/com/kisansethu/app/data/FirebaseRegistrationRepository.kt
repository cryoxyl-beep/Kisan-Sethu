package com.kisansethu.app.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.kisansethu.app.ui.registration.RegistrationFormState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseRegistrationRepository {

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            try {
                FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.firebaseio.com")
            } catch (e2: Exception) {
                FirebaseDatabase.getInstance()
            }
        }
    }

    suspend fun isPhoneNumberRegistered(phoneNumber: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val phoneRef = database.getReference("phoneNumbers").child(phoneNumber)
            phoneRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (continuation.isActive) {
                        continuation.resume(snapshot.exists())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            })
        }

    private fun generateCandidateFarmerId(): String {
        val charPool = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Exclude confusing chars 0, O, 1, I
        return (1..6)
            .map { charPool.random() }
            .joinToString("")
    }

    private suspend fun tryReserveFarmerId(candidateId: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val idRef = database.getReference("farmerIds").child(candidateId)
            idRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    if (mutableData.value != null) {
                        return Transaction.abort()
                    }
                    mutableData.value = true
                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (continuation.isActive) {
                        continuation.resume(error == null && committed)
                    }
                }
            })
        }

    suspend fun registerFarmer(
        formState: RegistrationFormState,
        preferredLanguageCode: String = "en"
    ): Result<String> {
        return try {
            kotlinx.coroutines.withTimeout(12000L) {
                // Step 1: Check duplicate phone number
                if (isPhoneNumberRegistered(formState.phoneNumber.trim())) {
                    return@withTimeout Result.failure(Exception("This mobile number is already registered."))
                }

                // Step 2: Reserve a unique Farmer ID
                var reservedFarmerId: String? = null
                var attempts = 0
                while (reservedFarmerId == null && attempts < 5) {
                    attempts++
                    val candidateId = generateCandidateFarmerId()
                    if (tryReserveFarmerId(candidateId)) {
                        reservedFarmerId = candidateId
                    }
                }

                val farmerId = reservedFarmerId ?: return@withTimeout Result.failure(
                    Exception("Something went wrong while generating your Farmer ID. Please try again.")
                )

                // Step 3: Hash PIN securely
                val pinHash = PinHasher.hashPin(formState.pin)

                // Step 4: Multi-path update in Firebase
                val updates = hashMapOf<String, Any>(
                    "farmerIds/$farmerId" to true,
                    "phoneNumbers/${formState.phoneNumber.trim()}" to farmerId,
                    "farmers/$farmerId" to mapOf(
                        "farmerId" to farmerId,
                        "fullName" to formState.fullName.trim(),
                        "phoneNumber" to formState.phoneNumber.trim(),
                        "dateOfBirth" to formState.dob.trim(),
                        "aadhaarNumber" to formState.aadhaarNumber.trim(),
                        "location" to mapOf(
                            "state" to formState.selectedState.trim(),
                            "district" to formState.selectedDistrict.trim(),
                            "mandalOrCity" to formState.mandalOrCity.trim(),
                            "locality" to formState.villageOrLocality.trim()
                        ),
                        "bank" to mapOf(
                            "accountNumber" to formState.bankAccountNumber.trim(),
                            "ifscCode" to formState.ifscCode.trim()
                        ),
                        "language" to preferredLanguageCode,
                        "verificationStatus" to "PENDING",
                        "accountStatus" to "ACTIVE",
                        "createdAt" to ServerValue.TIMESTAMP,
                        "updatedAt" to ServerValue.TIMESTAMP
                    ),
                    "authCredentials/$farmerId" to mapOf(
                        "pinHash" to pinHash,
                        "createdAt" to ServerValue.TIMESTAMP
                    )
                )

                suspendCancellableCoroutine { continuation ->
                    database.reference.updateChildren(updates)
                        .addOnSuccessListener {
                            if (continuation.isActive) {
                                continuation.resume(Result.success(farmerId))
                            }
                        }
                        .addOnFailureListener { exception ->
                            database.getReference("farmerIds").child(farmerId).removeValue()
                            if (continuation.isActive) {
                                continuation.resume(
                                    Result.failure(
                                        exception ?: Exception("Failed to write to Firebase database.")
                                    )
                                )
                            }
                        }
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Connection timeout. Please check your internet connection or Firebase Database Security Rules (set '.read': true, '.write': true in Firebase Console)."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
