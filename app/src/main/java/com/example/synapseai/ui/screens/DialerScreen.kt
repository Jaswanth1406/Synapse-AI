package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.CallState
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.DialerViewModel

@Composable
fun DialerScreen(
    viewModel: DialerViewModel = viewModel(),
    initialPhoneNumber: String? = null
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val leadName by viewModel.leadName.collectAsState()
    val language by viewModel.language.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val error by viewModel.error.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val lastCallResponse by viewModel.lastCallResponse.collectAsState()

    LaunchedEffect(initialPhoneNumber) {
        if (!initialPhoneNumber.isNullOrBlank()) {
            viewModel.updatePhoneNumber(initialPhoneNumber)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI Dialer",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Trigger AI-powered outbound calls",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Call State Indicator ──
        AnimatedVisibility(
            visible = callState != CallState.IDLE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CallStateCard(callState = callState, response = lastCallResponse, onEnd = { viewModel.endCall() })
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Input Fields ──
        AnimatedVisibility(
            visible = callState == CallState.IDLE || callState == CallState.CONCLUDED || callState == CallState.FAILED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.updatePhoneNumber(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone Number", color = TextTertiary) },
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
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = leadName,
                        onValueChange = { viewModel.updateLeadName(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Lead Name", color = TextTertiary) },
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
                }


                Spacer(modifier = Modifier.height(24.dp))

                // ── Dial Button ──
                DialButton(
                    callState = callState,
                    onDial = { viewModel.dialNow() },
                    onReset = { viewModel.resetCall() }
                )

                // Error message
                error?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Call History ──
        SectionHeader(
            title = "Recent Calls",
            action = {
                TextButton(onClick = { viewModel.refreshHistory() }) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (callHistory.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No call history yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }
            }
            items(callHistory) { entry ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricIndigo.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PhoneForwarded, "Call",
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = entry.phoneNumber ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                entry.summary?.let {
                                    Text(
                                        text = it.take(50) + if (it.length > 50) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val intentColor = when (entry.intent?.uppercase()) {
                                "INTERESTED" -> InterestedColor
                                "NOT_INTERESTED", "NOT INTERESTED" -> NotInterestedColor
                                "CALLBACK" -> CallbackColor
                                else -> TextTertiary
                            }
                            StatusChip(
                                label = entry.intent ?: entry.status ?: "—",
                                color = intentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallStateCard(
    callState: CallState,
    response: String?,
    onEnd: () -> Unit
) {
    val stateColor = when (callState) {
        CallState.INITIATING -> WarmAmber
        CallState.CONNECTED -> CyanAccent
        CallState.SPEAKING -> SuccessGreen
        CallState.CONCLUDED -> ElectricIndigo
        CallState.FAILED -> ErrorRed
        else -> TextTertiary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(stateColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (callState) {
                            CallState.INITIATING -> Icons.Filled.PhoneInTalk
                            CallState.SPEAKING -> Icons.Filled.RecordVoiceOver
                            CallState.CONCLUDED -> Icons.Filled.CheckCircle
                            CallState.FAILED -> Icons.Filled.Error
                            else -> Icons.Filled.Phone
                        },
                        "State",
                        tint = stateColor.copy(alpha = if (callState == CallState.INITIATING) pulseAlpha else 1f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = callState.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = stateColor,
                        fontWeight = FontWeight.Bold
                    )
                    response?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (callState == CallState.CONNECTED || callState == CallState.SPEAKING) {
                IconButton(
                    onClick = onEnd,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ErrorRed)
                ) {
                    Icon(Icons.Filled.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun DialButton(
    callState: CallState,
    onDial: () -> Unit,
    onReset: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dialPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    when {
        callState == CallState.CONCLUDED || callState == CallState.FAILED -> {
            GradientButton(
                text = "Reset & Dial Again",
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = listOf(ElectricIndigo, CyanAccent)
            )
        }
        callState == CallState.IDLE -> {
            Button(
                onClick = onDial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(pulseScale),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SuccessGreen, Color(0xFF2E7D32)),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, "Dial", tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DIAL NOW",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
        else -> { /* Call in progress — button hidden */ }
    }
}
