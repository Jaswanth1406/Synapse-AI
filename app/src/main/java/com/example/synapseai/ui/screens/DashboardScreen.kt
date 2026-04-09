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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToDialer: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {}
) {
    val metrics by viewModel.metrics.collectAsState()
    val isHealthy by viewModel.isHealthy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recentCalls by viewModel.recentCalls.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Synapse AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        brush = Brush.horizontalGradient(
                            colors = listOf(ElectricIndigo, CyanAccent)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Tele-Calling Dashboard",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PulseIndicator(isActive = isHealthy == true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHealthy == true) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHealthy == true) SuccessGreen else ErrorRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Metric Cards Grid ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Calls",
                value = metrics.totalCalls.toString(),
                subtitle = "All time",
                accentColor = ElectricIndigo,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Answer Rate",
                value = "${metrics.answerRate.toInt()}%",
                subtitle = "${metrics.answeredCalls} answered",
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Conversion",
                value = "${metrics.conversionRate.toInt()}%",
                subtitle = "${metrics.interestedCount} interested",
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Active Leads",
                value = metrics.activeLeads.toString(),
                subtitle = "Following up",
                accentColor = WarmAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Quick Actions ──
        SectionHeader(title = "Quick Actions")
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Filled.Phone,
                label = "New Call",
                color = SuccessGreen,
                onClick = onNavigateToDialer,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Filled.PersonAdd,
                label = "Add Contact",
                color = CyanAccent,
                onClick = onNavigateToContacts,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Filled.Schedule,
                label = "Schedule",
                color = WarmAmber,
                onClick = onNavigateToSchedule,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Intent Distribution ──
        SectionHeader(title = "Lead Intent Distribution")
        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IntentBar("Interested", metrics.interestedCount, InterestedColor, metrics.totalCalls)
                IntentBar("Not Interest.", metrics.notInterestedCount, NotInterestedColor, metrics.totalCalls)
                IntentBar("Callback", metrics.callbackCount, CallbackColor, metrics.totalCalls)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Recent Activity ──
        SectionHeader(
            title = "Recent Activity",
            action = {
                TextButton(onClick = { viewModel.refreshDashboard() }) {
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

        if (recentCalls.isEmpty()) {
            // Demo recent activity
            listOf(
                Triple("Rahul Sharma", "2m 34s", "Interested"),
                Triple("Priya Patel", "No Answer", "Pending"),
                Triple("Arjun Menon", "4m 12s", "Not Interested"),
                Triple("Sneha Reddy", "1m 58s", "Callback")
            ).forEach { (name, duration, status) ->
                RecentCallRow(name = name, duration = duration, status = status)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            recentCalls.forEach { call ->
                RecentCallRow(
                    name = call.parentName ?: call.name ?: "Unknown",
                    duration = "${(call.duration ?: 0) / 60}m ${(call.duration ?: 0) % 60}s",
                    status = call.status ?: "Unknown"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun IntentBar(
    label: String,
    count: Int,
    color: Color,
    total: Int
) {
    val pct = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct / 100f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RecentCallRow(
    name: String,
    duration: String,
    status: String
) {
    val statusColor = when (status.lowercase()) {
        "interested", "held" -> InterestedColor
        "not interested" -> NotInterestedColor
        "callback" -> CallbackColor
        else -> TextTertiary
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            StatusChip(label = status, color = statusColor)
        }
    }
}
