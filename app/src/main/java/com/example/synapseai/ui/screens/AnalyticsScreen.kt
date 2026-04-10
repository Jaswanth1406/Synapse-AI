package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.ChartDataItem
import com.example.synapseai.data.model.DurationDataItem
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live performance insights",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                IconButton(onClick = { viewModel.refreshAnalytics() }) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = ElectricIndigo)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Error Banner ──
            error?.let { msg ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, "Error", tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricIndigo)
                }
            } else {
                // ── KPI Summary Row ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Calls",
                        value = analytics.totalRuns.toString(),
                        accentColor = ElectricIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Interested",
                        value = analytics.interestedCount.toString(),
                        accentColor = InterestedColor,
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
                        value = "${analytics.conversionRate.toInt()}%",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Engagement",
                        value = String.format("%.1f", analytics.avgEngagement),
                        accentColor = WarmAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Intent Distribution Donut ──
                if (analytics.intentBreakdown.isNotEmpty()) {
                    SectionHeader(title = "Intent Distribution")
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(data = analytics.intentBreakdown)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${analytics.totalRuns}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                analytics.intentBreakdown.forEach { item ->
                                    LegendItem(item.name, item.value, getIntentColor(item.name))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── Sentiment Distribution ──
                if (analytics.sentimentDist.isNotEmpty()) {
                    SectionHeader(title = "Sentiment Distribution")
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        analytics.sentimentDist.forEach { item ->
                            PerformanceRow(item.name.replaceFirstChar { it.uppercase() }, item.value.toString(), getSentimentColor(item.name))
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── Duration Stats ──
                if (analytics.durationStats.isNotEmpty()) {
                    SectionHeader(title = "Call Duration Histogram")
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        DurationBarChart(data = analytics.durationStats)
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── Dispositions ──
                if (analytics.dispositions.isNotEmpty()) {
                    SectionHeader(title = "Call Dispositions")
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        analytics.dispositions.forEach { item ->
                            PerformanceRow(item.name.replaceFirstChar { it.uppercase() }, item.value.toString(), getDispositionColor(item.name))
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── Action Buttons ──
                SectionHeader(title = "Data Operations")
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.exportCsv() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Download, "CSV", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { viewModel.triggerBackfill() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmAmber),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Sync, "Backfill", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backfill", style = MaterialTheme.typography.labelSmall)
                    }
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
    }
}

private fun getIntentColor(name: String): Color {
    return when (name.uppercase()) {
        "INTERESTED" -> InterestedColor
        "NOT_INTERESTED", "NOT INTERESTED" -> NotInterestedColor
        "CALLBACK" -> CallbackColor
        "NO_ANSWER", "NO ANSWER" -> WarningOrange
        else -> ElectricIndigo
    }
}

private fun getSentimentColor(name: String): Color {
    return when (name.lowercase()) {
        "positive" -> SuccessGreen
        "negative" -> ErrorRed
        "neutral" -> WarmAmber
        else -> TextTertiary
    }
}

private fun getDispositionColor(name: String): Color {
    return when (name.lowercase()) {
        "completed" -> SuccessGreen
        "failed" -> ErrorRed
        "no_answer", "no answer" -> WarningOrange
        else -> CyanAccent
    }
}

@Composable
private fun DonutChart(data: List<ChartDataItem>) {
    val total = data.sumOf { it.value }.toFloat().coerceAtLeast(1f)

    var animProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(data) {
        animate(0f, 1f, animationSpec = tween(1200, easing = FastOutSlowInEasing)) { v, _ ->
            animProgress = v
        }
    }

    Canvas(modifier = Modifier.size(130.dp)) {
        val strokeWidth = 24f
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - 2 * radius) / 2,
            (size.height - 2 * radius) / 2
        )
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f * animProgress
            drawArc(
                color = getIntentColor(item.name),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle + 3f
        }
    }
}

@Composable
private fun DurationBarChart(data: List<DurationDataItem>) {
    val maxVal = data.maxOfOrNull { it.count }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    var animProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(data) {
        animate(0f, 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing)) { v, _ ->
            animProgress = v
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = (item.count * animProgress).toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height((100 * (item.count / maxVal) * animProgress).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(ElectricIndigo.copy(alpha = 0.4f + (0.6f * item.count / maxVal)))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.range,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PerformanceRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
