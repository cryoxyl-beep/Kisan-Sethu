package com.kisansethu.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.ui.components.GlassButton
import com.kisansethu.app.ui.components.GlassOutlinedButton
import com.kisansethu.app.ui.registration.RegistrationFormState
import com.kisansethu.app.ui.registration.RegistrationViewModel
import com.kisansethu.app.ui.theme.KisanSethuTheme
import androidx.compose.ui.graphics.toArgb
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onRegistrationSuccess: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null,
    viewModel: RegistrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep in 2..4) {
                                viewModel.previousStep()
                            } else {
                                onNavigateBack()
                            }
                        },
                        enabled = !uiState.isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "Farmer Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (onToggleTheme != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = onToggleTheme,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                    contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                }

                if (uiState.currentStep <= 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepProgressBar(currentStep = uiState.currentStep, totalSteps = 4)
                }
            }
        },
        bottomBar = {
            if (uiState.currentStep <= 4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    GlassButton(
                        onClick = { viewModel.validateAndProceedNext() },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSubmitting) {
                                Text(
                                    text = "Creating your account...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                        } else {
                            Text(
                                text = if (uiState.currentStep == 4) "Create Account" else "Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            when (uiState.currentStep) {
                1 -> StepPersonalDetails(
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenDatePicker = { showDatePicker = true }
                )
                2 -> StepLocationDetails(
                    uiState = uiState,
                    viewModel = viewModel
                )
                3 -> StepBankDetails(
                    uiState = uiState,
                    viewModel = viewModel
                )
                4 -> StepReviewAndPin(
                    uiState = uiState,
                    viewModel = viewModel
                )
                5 -> StepSuccess(
                    farmerId = uiState.generatedFarmerId,
                    onDone = {
                        onRegistrationSuccess()
                    }
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        RegistrationDatePickerDialog(
            onDateSelected = { selectedDateStr ->
                viewModel.updateDob(selectedDateStr)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Expressive Loading Overlay
    if (uiState.isSubmitting) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            com.google.android.material.loadingindicator.LoadingIndicator(context)
                        },
                        update = { view ->
                            view.setIndicatorColor(primaryColor.toArgb())
                        },
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Creating account...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please wait while we set up your profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stepTitle = when (currentStep) {
                1 -> "1. Personal Details"
                2 -> "2. Location Details"
                3 -> "3. Bank Details"
                4 -> "4. Review & PIN Security"
                else -> ""
            }
            Text(
                text = stepTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

// ======================== STEP 1: PERSONAL DETAILS ========================
@Composable
private fun StepPersonalDetails(
    uiState: RegistrationFormState,
    viewModel: RegistrationViewModel,
    onOpenDatePicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Please enter your personal details as per official government records.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        RegistrationTextField(
            value = uiState.fullName,
            onValueChange = { viewModel.updateFullName(it) },
            label = "Full Name",
            placeholder = "Enter your full name",
            icon = Icons.Rounded.Person,
            errorMessage = uiState.fullNameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            )
        )

        RegistrationTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.updatePhoneNumber(it) },
            label = "Phone Number",
            placeholder = "10 digit mobile number",
            icon = Icons.Rounded.Phone,
            errorMessage = uiState.phoneNumberError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // DOB Field with Calendar trigger
        Box(modifier = Modifier.fillMaxWidth()) {
            RegistrationTextField(
                value = uiState.dob,
                onValueChange = { viewModel.updateDob(it) },
                label = "Date of Birth (DOB)",
                placeholder = "DD/MM/YYYY",
                icon = Icons.Rounded.CalendarToday,
                errorMessage = uiState.dobError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        RegistrationTextField(
            value = uiState.aadhaarNumber,
            onValueChange = { viewModel.updateAadhaarNumber(it) },
            label = "Aadhaar Number",
            placeholder = "12 digit Aadhaar number",
            icon = Icons.Rounded.Badge,
            errorMessage = uiState.aadhaarNumberError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ======================== STEP 2: LOCATION DETAILS ========================
@Composable
private fun StepLocationDetails(
    uiState: RegistrationFormState,
    viewModel: RegistrationViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Location Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Select your location hierarchy from dropdowns.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // State Dropdown
        DropdownSelectionField(
            value = uiState.selectedState,
            onValueChange = { viewModel.updateState(it) },
            label = "State",
            placeholder = "Select State",
            icon = Icons.Rounded.Map,
            items = viewModel.availableStates,
            errorMessage = uiState.stateError
        )

        // District Dropdown
        val districts = if (uiState.selectedState.isNotEmpty()) {
            viewModel.getDistrictsForState(uiState.selectedState)
        } else {
            emptyList()
        }

        DropdownSelectionField(
            value = uiState.selectedDistrict,
            onValueChange = { viewModel.updateDistrict(it) },
            label = "District",
            placeholder = if (uiState.selectedState.isEmpty()) "Select State first" else "Select District",
            icon = Icons.Rounded.LocationOn,
            items = districts,
            errorMessage = uiState.districtError,
            enabled = uiState.selectedState.isNotEmpty()
        )

        // Mandal / City Manual Entry
        RegistrationTextField(
            value = uiState.mandalOrCity,
            onValueChange = { viewModel.updateMandalOrCity(it) },
            label = "Mandal / City",
            placeholder = "Enter your Mandal or City name",
            icon = Icons.Rounded.LocationCity,
            errorMessage = uiState.mandalError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            )
        )

        // Village / Locality Manual Entry
        RegistrationTextField(
            value = uiState.villageOrLocality,
            onValueChange = { viewModel.updateVillageOrLocality(it) },
            label = "Village / Locality",
            placeholder = "Enter your Village or Locality name",
            icon = Icons.Rounded.Home,
            errorMessage = uiState.villageError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ======================== STEP 3: BANK DETAILS ========================
@Composable
private fun StepBankDetails(
    uiState: RegistrationFormState,
    viewModel: RegistrationViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Bank Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Provide your bank account details for direct procurement payments.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        RegistrationTextField(
            value = uiState.bankAccountNumber,
            onValueChange = { viewModel.updateBankAccountNumber(it) },
            label = "Bank Account Number",
            placeholder = "Enter bank account number",
            icon = Icons.Rounded.AccountBalance,
            errorMessage = uiState.bankAccountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        RegistrationTextField(
            value = uiState.confirmAccountNumber,
            onValueChange = { viewModel.updateConfirmAccountNumber(it) },
            label = "Confirm Account Number",
            placeholder = "Re-enter bank account number",
            icon = Icons.Rounded.AccountBalance,
            errorMessage = uiState.confirmAccountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        RegistrationTextField(
            value = uiState.ifscCode,
            onValueChange = { viewModel.updateIfscCode(it) },
            label = "IFSC Code",
            placeholder = "e.g. SBIN0001234",
            icon = Icons.Rounded.AccountBalanceWallet,
            errorMessage = uiState.ifscError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ======================== STEP 4: REVIEW DETAILS & PIN SECURITY ========================
@Composable
private fun StepReviewAndPin(
    uiState: RegistrationFormState,
    viewModel: RegistrationViewModel
) {
    var isPinVisible by remember { mutableStateOf(false) }
    var isConfirmPinVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Review & Security PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Review your registration details and set up a 6-digit security PIN.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Registration Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SummaryRow(label = "Full Name", value = uiState.fullName)
                SummaryRow(label = "Mobile", value = uiState.phoneNumber)
                SummaryRow(label = "DOB", value = uiState.dob)
                SummaryRow(
                    label = "Aadhaar",
                    value = if (uiState.aadhaarNumber.length == 12) "XXXX-XXXX-${uiState.aadhaarNumber.takeLast(4)}" else uiState.aadhaarNumber
                )
                SummaryRow(label = "State", value = uiState.selectedState)
                SummaryRow(label = "District", value = uiState.selectedDistrict)
                SummaryRow(label = "Mandal/City", value = uiState.mandalOrCity)
                SummaryRow(label = "Village/Locality", value = uiState.villageOrLocality)
                SummaryRow(
                    label = "Bank Account",
                    value = if (uiState.bankAccountNumber.length >= 4) "XXXX${uiState.bankAccountNumber.takeLast(4)}" else uiState.bankAccountNumber
                )
                SummaryRow(label = "IFSC Code", value = uiState.ifscCode)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Create 6-Digit PIN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // PIN Input Field
        RegistrationTextField(
            value = uiState.pin,
            onValueChange = { viewModel.updatePin(it) },
            label = "Enter 6-Digit PIN",
            placeholder = "6 digits PIN",
            icon = Icons.Rounded.Lock,
            errorMessage = uiState.pinError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPinVisible = !isPinVisible }) {
                    Icon(
                        imageVector = if (isPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Toggle PIN visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        // Confirm PIN Input Field
        RegistrationTextField(
            value = uiState.confirmPin,
            onValueChange = { viewModel.updateConfirmPin(it) },
            label = "Confirm 6-Digit PIN",
            placeholder = "Re-enter 6 digits PIN",
            icon = Icons.Rounded.Lock,
            errorMessage = uiState.confirmPinError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = if (isConfirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPinVisible = !isConfirmPinVisible }) {
                    Icon(
                        imageVector = if (isConfirmPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Toggle Confirm PIN visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        // Display Firebase registration error banner if write failed
        AnimatedVisibility(visible = uiState.registrationError != null) {
            val errorMsg = uiState.registrationError ?: ""
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================== STEP 5: REGISTRATION SUCCESS ========================
@Composable
private fun StepSuccess(
    farmerId: String,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(90.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Registration Complete",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Kissaan Sync farmer account has been created.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Prominent Farmer ID Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR FARMER ID",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = farmerId,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Keep your Farmer ID safe. You will use it to sign in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                GlassOutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(farmerId))
                        Toast.makeText(context, "Farmer ID copied.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Copy Farmer ID",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        GlassButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue to Login",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ======================== REUSABLE FORM COMPONENTS ========================
@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = trailingIcon,
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = errorMessage != null) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelectionField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    items: List<String>,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
                },
                isError = errorMessage != null,
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
            )

            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onValueChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }

        AnimatedVisibility(visible = errorMessage != null) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

// ======================== DATE PICKER DIALOG ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val formattedDate = formatter.format(Date(selectedMillis))
                        onDateSelected(formattedDate)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationScreenPreview() {
    KisanSethuTheme(dynamicColor = false) {
        RegistrationScreen()
    }
}
