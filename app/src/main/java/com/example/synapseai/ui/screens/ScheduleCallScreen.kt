package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.ScheduledCallEntry
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCallScreen(
    viewModel: ScheduleViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val leadName by viewModel.leadName.collectAsState()
    val date by viewModel.date.collectAsState()
    val time by viewModel.time.collectAsState()
    val retryCount by viewModel.retryCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val scheduledCalls by viewModel.scheduledCalls.collectAsState()
    val isLoadingScheduled by viewModel.isLoadingScheduled.collectAsState()

    // Get current date/time from device
    val calendar = remember {
        Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
    }

    // Pre-fill ViewModel with defaults on first launch
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val defaultDate = sdf.format(calendar.time)
        val defaultTime = "%02d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        if (date.isBlank()) viewModel.updateDate(defaultDate)
        if (time.isBlank()) viewModel.updateTime(defaultTime)
    }

    // Date Picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    // Time Picker state
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Column {
                    Text(
                        text = "Schedule Call",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set up AI call for a future time",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Phone Number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number *", color = TextTertiary) },
                placeholder = { Text("+91 XXXXX XXXXX", color = TextTertiary.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Filled.Phone, "Phone", tint = ElectricIndigo) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    cursorColor = ElectricIndigo,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Lead Name
            OutlinedTextField(
                value = leadName,
                onValueChange = { viewModel.updateLeadName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lead Name", color = TextTertiary) },
                placeholder = { Text("e.g. John Doe", color = TextTertiary.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Filled.Person, "Name", tint = CyanAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    cursorColor = ElectricIndigo,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Date Picker Button
            SectionHeader(title = "Date & Time")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkCard,
                        contentColor = if (date.isNotBlank()) TextPrimary else TextTertiary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Icon(
                        Icons.Filled.CalendarToday, "Date",
                        tint = WarmAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (date.isNotBlank()) date else "Pick Date",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Time Button
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkCard,
                        contentColor = if (time.isNotBlank()) TextPrimary else TextTertiary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Icon(
                        Icons.Filled.AccessTime, "Time",
                        tint = WarmAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (time.isNotBlank()) time else "Pick Time",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Retry Count
            SectionHeader(title = "Retry Attempts")
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Auto-retry if no answer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Respects 9 AM – 6 PM local time",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.updateRetryCount(retryCount - 1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(Icons.Filled.Remove, "Minus", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = retryCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricIndigo,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = { viewModel.updateRetryCount(retryCount + 1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricIndigo.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Filled.Add, "Plus", tint = ElectricIndigo, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Submit Button
            GradientButton(
                text = if (isLoading) "Scheduling..." else "Schedule Call",
                onClick = { viewModel.scheduleCall() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = listOf(WarmAmber, WarmAmberDark)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Scheduled Calls List ──
            SectionHeader(
                title = "Scheduled Calls",
                action = {
                    TextButton(onClick = { viewModel.loadScheduledCalls() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = ElectricIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", color = ElectricIndigo, style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (isLoadingScheduled) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ElectricIndigo, modifier = Modifier.size(28.dp))
                }
            } else if (scheduledCalls.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EventBusy,
                            "No schedules",
                            tint = TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "No scheduled calls yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                scheduledCalls.forEach { entry ->
                    ScheduledCallCard(
                        entry = entry,
                        onCancel = { entry.id?.let { viewModel.cancelScheduledCall(it) } }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Toast
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkCardElevated,
                tonalElevation = 8.dp
            ) {
                Text(
                    text = toastMessage ?: "",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // ── Date Picker Dialog ──
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                viewModel.updateDate(sdf.format(Date(millis)))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", color = ElectricIndigo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = DarkSurface
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary,
                        headlineContentColor = TextPrimary,
                        weekdayContentColor = TextTertiary,
                        subheadContentColor = TextSecondary,
                        yearContentColor = TextSecondary,
                        currentYearContentColor = ElectricIndigo,
                        selectedYearContentColor = TextPrimary,
                        selectedYearContainerColor = ElectricIndigo,
                        dayContentColor = TextPrimary,
                        selectedDayContentColor = TextPrimary,
                        selectedDayContainerColor = ElectricIndigo,
                        todayContentColor = ElectricIndigo,
                        todayDateBorderColor = ElectricIndigo,
                        navigationContentColor = TextPrimary
                    )
                )
            }
        }

        // ── Time Picker Dialog ──
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text("Select Time", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = DarkCard,
                                clockDialSelectedContentColor = TextPrimary,
                                clockDialUnselectedContentColor = TextSecondary,
                                selectorColor = ElectricIndigo,
                                containerColor = DarkSurface,
                                periodSelectorBorderColor = GlassBorder,
                                periodSelectorSelectedContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                periodSelectorSelectedContentColor = ElectricIndigo,
                                periodSelectorUnselectedContainerColor = DarkCard,
                                periodSelectorUnselectedContentColor = TextSecondary,
                                timeSelectorSelectedContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                timeSelectorSelectedContentColor = ElectricIndigo,
                                timeSelectorUnselectedContainerColor = DarkCard,
                                timeSelectorUnselectedContentColor = TextSecondary
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val h = timePickerState.hour
                            val m = timePickerState.minute
                            viewModel.updateTime("%02d:%02d".format(h, m))
                            showTimePicker = false
                        }
                    ) {
                        Text("OK", color = ElectricIndigo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ScheduledCallCard(
    entry: ScheduledCallEntry,
    onCancel: () -> Unit
) {
    val statusColor = when (entry.status?.lowercase()) {
        "completed" -> SuccessGreen
        "pending" -> WarmAmber
        "cancelled", "canceled" -> ErrorRed
        "failed" -> ErrorRed
        else -> TextTertiary
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendar icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    "Scheduled",
                    tint = WarmAmber,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.leadName ?: entry.phoneNumber ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.leadName != null && entry.phoneNumber != null) {
                    Text(
                        text = entry.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                entry.scheduledTime?.let {
                    Text(
                        text = it.take(16).replace("T", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusChip(
                    label = entry.status ?: "Unknown",
                    color = statusColor
                )
                // Show cancel button only for pending items
                if (entry.status?.lowercase() == "pending") {
                    Spacer(modifier = Modifier.height(6.dp))
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Cancel,
                            "Cancel",
                            tint = ErrorRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
