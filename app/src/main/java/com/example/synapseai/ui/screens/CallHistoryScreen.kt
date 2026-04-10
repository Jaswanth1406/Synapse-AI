package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.CallHistoryEntry
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.CallHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel = viewModel()
) {
    val callHistory by viewModel.callHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredHistory = viewModel.getFilteredHistory()

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Call History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${callHistory.size} records from database",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = ElectricIndigo)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by phone, intent, status...", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = TextTertiary) },
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

            // Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricIndigo)
                }
            } else if (error != null && callHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CloudOff, "Error", tint = ErrorRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Failed to load history", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                        Text(error ?: "", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        GradientButton(text = "Retry", onClick = { viewModel.refresh() })
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (filteredHistory.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.HistoryToggleOff, "Empty", tint = TextTertiary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "No call records found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    }
                    items(filteredHistory, key = { it.callId ?: it.hashCode().toString() }) { entry ->
                        CallHistoryCard(
                            entry = entry,
                            onClick = { viewModel.selectEntry(entry) }
                        )
                    }
                }
            }
        }

        // ── Call Summary Bottom Sheet ──
        selectedEntry?.let { entry ->
            CallSummarySheet(
                entry = entry,
                onDismiss = { viewModel.selectEntry(null) }
            )
        }
    }
}

// ── Full-Screen Call Summary Bottom Sheet ──
@Composable
private fun CallSummarySheet(
    entry: CallHistoryEntry,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false, onClick = {}), // prevent propagation
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Drag handle
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GlassBorder)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Call Summary",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            entry.phoneNumber ?: "Unknown Number",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                    ) {
                        Icon(Icons.Filled.Close, "Close", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    entry.status?.let { StatusChip(label = it, color = getStatusColor(it)) }
                    entry.intent?.let { StatusChip(label = it, color = getIntentColor(it)) }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── AI Summary Card ──
                entry.summary?.let { summary ->
                    SummarySection(
                        icon = Icons.Filled.AutoAwesome,
                        title = "AI Summary",
                        iconColor = WarmAmber
                    ) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Call Metadata ──
                SummarySection(
                    icon = Icons.Filled.Info,
                    title = "Call Details",
                    iconColor = CyanAccent
                ) {
                    DetailRow("Call ID", entry.callId ?: "N/A")
                    DetailRow("Phone", entry.phoneNumber ?: "N/A")
                    DetailRow("Status", entry.status ?: "N/A")
                    DetailRow("Intent", entry.intent ?: "N/A")
                    entry.createdAt?.let {
                        DetailRow("Date", it.take(19).replace("T", "  "))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Recording Link ──
                entry.recordingUrl?.let { url ->
                    SummarySection(
                        icon = Icons.Filled.Headphones,
                        title = "Call Recording",
                        iconColor = SuccessGreen
                    ) {
                        OutlinedButton(
                            onClick = {
                                try { uriHandler.openUri(url) } catch (_: Exception) {}
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                        ) {
                            Icon(Icons.Filled.PlayCircle, "Play", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Listen to Recording", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Full Transcript ──
                entry.transcript?.let { transcript ->
                    if (transcript.isNotBlank()) {
                        SummarySection(
                            icon = Icons.Filled.Chat,
                            title = "Full Transcript",
                            iconColor = ElectricIndigo
                        ) {
                            // Parse transcript lines
                            val lines = transcript.split("\n").filter { it.isNotBlank() }
                            lines.forEach { line ->
                                val isAssistant = line.contains("assistant:")
                                val isUser = line.contains("user:")
                                val cleanLine = line
                                    .replace(Regex("\\[.*?\\]\\s*"), "")
                                    .replace("assistant:", "")
                                    .replace("user:", "")
                                    .trim()

                                if (cleanLine.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        // Role indicator
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isAssistant) ElectricIndigo.copy(alpha = 0.15f)
                                                    else CyanAccent.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isAssistant) Icons.Filled.SmartToy else Icons.Filled.Person,
                                                contentDescription = if (isAssistant) "AI" else "User",
                                                tint = if (isAssistant) ElectricIndigo else CyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isAssistant) "AI Agent" else "Lead",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isAssistant) ElectricIndigo else CyanAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = cleanLine,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ── Section Card with icon + title ──
@Composable
private fun SummarySection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, title, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

// ── Detail row (label: value) ──
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CallHistoryCard(
    entry: CallHistoryEntry,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getIntentColor(entry.intent ?: "").copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (entry.intent?.uppercase()) {
                        "INTERESTED" -> Icons.Filled.ThumbUp
                        "NOT_INTERESTED", "NOT INTERESTED" -> Icons.Filled.ThumbDown
                        "CALLBACK" -> Icons.Filled.PhoneCallback
                        "INFO_SEEKING" -> Icons.Filled.Info
                        else -> Icons.Filled.Phone
                    },
                    "Intent",
                    tint = getIntentColor(entry.intent ?: ""),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.phoneNumber ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                entry.summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                entry.createdAt?.let {
                    Text(
                        text = it.take(16).replace("T", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                entry.intent?.let {
                    StatusChip(label = it, color = getIntentColor(it))
                }
                Spacer(modifier = Modifier.height(4.dp))
                entry.status?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(it)
                    )
                }
            }
        }
    }
}

private fun getIntentColor(name: String): androidx.compose.ui.graphics.Color {
    return when (name.uppercase()) {
        "INTERESTED" -> InterestedColor
        "NOT_INTERESTED", "NOT INTERESTED" -> NotInterestedColor
        "CALLBACK" -> CallbackColor
        "INFO_SEEKING" -> ElectricIndigo
        "NO_ANSWER", "NO ANSWER" -> WarningOrange
        else -> ElectricIndigo
    }
}

private fun getStatusColor(name: String): androidx.compose.ui.graphics.Color {
    return when (name.lowercase()) {
        "completed" -> SuccessGreen
        "failed" -> ErrorRed
        "pending", "scheduled" -> WarmAmber
        "initiated" -> CyanAccent
        else -> TextTertiary
    }
}
