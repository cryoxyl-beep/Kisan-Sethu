package com.kisansethu.app.ui.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.FirebaseRegistrationRepository
import com.kisansethu.app.data.LocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationFormState(
    val currentStep: Int = 1, // 1: Personal, 2: Location, 3: Bank, 4: Review & PIN, 5: Success
    
    // Step 1: Personal Details
    val fullName: String = "",
    val phoneNumber: String = "",
    val dob: String = "",
    val aadhaarNumber: String = "",

    // Step 2: Location Details
    val selectedState: String = "",
    val selectedDistrict: String = "",
    val mandalOrCity: String = "",
    val villageOrLocality: String = "",

    // Step 3: Bank Details
    val bankAccountNumber: String = "",
    val confirmAccountNumber: String = "",
    val ifscCode: String = "",

    // Step 4: PIN Security
    val pin: String = "",
    val confirmPin: String = "",

    // Output Generated Details
    val generatedFarmerId: String = "",

    // Errors
    val fullNameError: String? = null,
    val phoneNumberError: String? = null,
    val dobError: String? = null,
    val aadhaarNumberError: String? = null,
    val stateError: String? = null,
    val districtError: String? = null,
    val mandalError: String? = null,
    val villageError: String? = null,
    val bankAccountError: String? = null,
    val confirmAccountError: String? = null,
    val ifscError: String? = null,
    val pinError: String? = null,
    val confirmPinError: String? = null,
    val registrationError: String? = null,

    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val firebaseRegistrationRepository = FirebaseRegistrationRepository()

    private val _uiState = MutableStateFlow(RegistrationFormState())
    val uiState: StateFlow<RegistrationFormState> = _uiState.asStateFlow()

    val availableStates: List<String> by lazy {
        locationRepository.getStates()
    }

    fun getDistrictsForState(state: String): List<String> {
        return locationRepository.getDistrictsForState(state)
    }

    // Step 1 Updates
    fun updateFullName(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null) }
    }

    fun updatePhoneNumber(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phoneNumber = filtered, phoneNumberError = null, registrationError = null) }
    }

    fun updateDob(value: String) {
        _uiState.update { it.copy(dob = value, dobError = null) }
    }

    fun updateAadhaarNumber(value: String) {
        val filtered = value.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(aadhaarNumber = filtered, aadhaarNumberError = null) }
    }

    // Step 2 Updates
    fun updateState(value: String) {
        _uiState.update { 
            it.copy(
                selectedState = value, 
                selectedDistrict = "", // Reset district when state changes
                stateError = null,
                districtError = null
            ) 
        }
    }

    fun updateDistrict(value: String) {
        _uiState.update { it.copy(selectedDistrict = value, districtError = null) }
    }

    fun updateMandalOrCity(value: String) {
        _uiState.update { it.copy(mandalOrCity = value, mandalError = null) }
    }

    fun updateVillageOrLocality(value: String) {
        _uiState.update { it.copy(villageOrLocality = value, villageError = null) }
    }

    // Step 3 Updates
    fun updateBankAccountNumber(value: String) {
        val filtered = value.filter { it.isDigit() }.take(18)
        _uiState.update { it.copy(bankAccountNumber = filtered, bankAccountError = null) }
    }

    fun updateConfirmAccountNumber(value: String) {
        val filtered = value.filter { it.isDigit() }.take(18)
        _uiState.update { it.copy(confirmAccountNumber = filtered, confirmAccountError = null) }
    }

    fun updateIfscCode(value: String) {
        val filtered = value.uppercase().take(11)
        _uiState.update { it.copy(ifscCode = filtered, ifscError = null) }
    }

    // Step 4 Updates (PIN)
    fun updatePin(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(pin = filtered, pinError = null, registrationError = null) }
    }

    fun updateConfirmPin(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(confirmPin = filtered, confirmPinError = null, registrationError = null) }
    }

    fun validateAndProceedNext(): Boolean {
        val current = _uiState.value
        if (current.isSubmitting) return false

        when (current.currentStep) {
            1 -> {
                val fullNameErr = if (current.fullName.trim().isEmpty()) "Full name is required" else null
                val phoneErr = when {
                    current.phoneNumber.isEmpty() -> "Phone number is required"
                    current.phoneNumber.length != 10 -> "Phone number must be 10 digits"
                    else -> null
                }
                val dobErr = if (current.dob.trim().isEmpty()) "Date of birth is required" else null
                val aadhaarErr = when {
                    current.aadhaarNumber.isEmpty() -> "Aadhaar number is required"
                    current.aadhaarNumber.length != 12 -> "Aadhaar number must be 12 digits"
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        fullNameError = fullNameErr,
                        phoneNumberError = phoneErr,
                        dobError = dobErr,
                        aadhaarNumberError = aadhaarErr
                    )
                }

                if (fullNameErr == null && phoneErr == null && dobErr == null && aadhaarErr == null) {
                    _uiState.update { it.copy(currentStep = 2) }
                    return true
                }
            }

            2 -> {
                val stateErr = if (current.selectedState.isEmpty()) "State is required" else null
                val districtErr = if (current.selectedDistrict.isEmpty()) "District is required" else null
                val mandalErr = if (current.mandalOrCity.trim().isEmpty()) "Mandal / City is required" else null
                val villageErr = if (current.villageOrLocality.trim().isEmpty()) "Village / Locality is required" else null

                _uiState.update {
                    it.copy(
                        stateError = stateErr,
                        districtError = districtErr,
                        mandalError = mandalErr,
                        villageError = villageErr
                    )
                }

                if (stateErr == null && districtErr == null && mandalErr == null && villageErr == null) {
                    _uiState.update { it.copy(currentStep = 3) }
                    return true
                }
            }

            3 -> {
                val bankAccErr = when {
                    current.bankAccountNumber.isEmpty() -> "Bank account number is required"
                    current.bankAccountNumber.length < 9 -> "Account number must be at least 9 digits"
                    else -> null
                }
                val confirmAccErr = when {
                    current.confirmAccountNumber.isEmpty() -> "Please confirm account number"
                    current.confirmAccountNumber != current.bankAccountNumber -> "Account numbers do not match"
                    else -> null
                }
                val ifscErr = when {
                    current.ifscCode.isEmpty() -> "IFSC Code is required"
                    current.ifscCode.length != 11 -> "IFSC Code must be 11 characters"
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        bankAccountError = bankAccErr,
                        confirmAccountError = confirmAccErr,
                        ifscError = ifscErr
                    )
                }

                if (bankAccErr == null && confirmAccErr == null && ifscErr == null) {
                    _uiState.update { it.copy(currentStep = 4) }
                    return true
                }
            }

            4 -> {
                val pErr = when {
                    current.pin.isEmpty() -> "Enter your 6-digit PIN."
                    current.pin.length != 6 -> "PIN must contain 6 digits."
                    else -> null
                }

                val cpErr = when {
                    current.confirmPin.isEmpty() -> "Enter your 6-digit PIN."
                    current.confirmPin.length != 6 -> "PIN must contain 6 digits."
                    current.confirmPin != current.pin -> "PINs do not match."
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        pinError = pErr,
                        confirmPinError = cpErr
                    )
                }

                if (pErr == null && cpErr == null) {
                    submitRegistration()
                    return true
                }
            }
        }
        return false
    }

    fun previousStep() {
        if (_uiState.value.currentStep in 2..4 && !_uiState.value.isSubmitting) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, registrationError = null) }
        }
    }

    private fun submitRegistration() {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    registrationError = null
                )
            }

            val startTime = System.currentTimeMillis()
            val result = firebaseRegistrationRepository.registerFarmer(
                formState = _uiState.value,
                preferredLanguageCode = "en"
            )

            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 2500) {
                delay(2500 - elapsedTime)
            }

            result.fold(
                onSuccess = { farmerId ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            generatedFarmerId = farmerId,
                            currentStep = 5
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Something went wrong while creating your account. Please try again."
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            registrationError = errorMsg
                        )
                    }
                }
            )
        }
    }
}
