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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.ScheduleViewModel

@Composable
fun ScheduleCallScreen(
    viewModel: ScheduleViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val date by viewModel.date.collectAsState()
    val time by viewModel.time.collectAsState()
    val language by viewModel.language.collectAsState()
    val retryCount by viewModel.retryCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

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

            Spacer(modifier = Modifier.height(16.dp))

            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { viewModel.updateDate(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Date *", color = TextTertiary) },
                    placeholder = { Text("2026-04-10", color = TextTertiary.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, "Date", tint = WarmAmber, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmAmber,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        cursorColor = WarmAmber,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { viewModel.updateTime(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Time *", color = TextTertiary) },
                    placeholder = { Text("09:00", color = TextTertiary.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.AccessTime, "Time", tint = WarmAmber, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmAmber,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        cursorColor = WarmAmber,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language Selection
            SectionHeader(title = "Language")
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("english" to "🇺🇸 English", "hindi" to "🇮🇳 Hindi", "tamil" to "🇮🇳 Tamil").forEach { (code, label) ->
                        FilterChip(
                            selected = language == code,
                            onClick = { viewModel.updateLanguage(code) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricIndigo,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
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
    }
}
