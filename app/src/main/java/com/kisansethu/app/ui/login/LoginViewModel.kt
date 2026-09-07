package com.kisansethu.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.FarmerLoginResult
import com.kisansethu.app.data.FirebaseLoginRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "", // Mobile Number or Farmer ID
    val pin: String = "",
    val identifierError: String? = null,
    val pinError: String? = null,
    val loginError: String? = null,
    val isLoading: Boolean = false,
    val loggedInFarmer: FarmerLoginResult? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseLoginRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateIdentifier(value: String) {
        _uiState.update { 
            it.copy(
                identifier = value, 
                identifierError = null, 
                loginError = null 
            ) 
        }
    }

    fun updatePin(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { 
            it.copy(
                pin = filtered, 
                pinError = null, 
                loginError = null 
            ) 
        }
    }

    fun performLogin(onSuccess: (FarmerLoginResult) -> Unit) {
        val current = _uiState.value
        if (current.isLoading) return

        val idTrimmed = current.identifier.trim()
        val pinTrimmed = current.pin.trim()

        val idErr = when {
            idTrimmed.isEmpty() -> "Mobile Number or Farmer ID is required"
            else -> null
        }

        val pinErr = when {
            pinTrimmed.isEmpty() -> "6-digit PIN is required"
            pinTrimmed.length != 6 -> "PIN must contain 6 digits"
            else -> null
        }

        _uiState.update { 
            it.copy(
                identifierError = idErr,
                pinError = pinErr,
                loginError = null
            ) 
        }

        if (idErr != null || pinErr != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }

            val startTime = System.currentTimeMillis()
            val result = repository.login(idTrimmed, pinTrimmed)

            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 2500) {
                delay(2500 - elapsedTime)
            }

            result.fold(
                onSuccess = { farmer ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loggedInFarmer = farmer
                        ) 
                    }
                    onSuccess(farmer)
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Login failed. Please check your credentials."
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loginError = errorMsg
                        ) 
                    }
                }
            )
        }
    }
}
