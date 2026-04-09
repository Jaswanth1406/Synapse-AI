package com.example.synapseai.ui.screens

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
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val metrics by viewModel.metrics.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                    text = "Call performance insights",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            IconButton(onClick = { viewModel.refreshAnalytics() }) {
                Icon(Icons.Filled.Refresh, "Refresh", tint = ElectricIndigo)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── KPI Summary Row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Calls",
                value = metrics.totalCalls.toString(),
                accentColor = ElectricIndigo,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Answered",
                value = metrics.answeredCalls.toString(),
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
                title = "Answer Rate",
                value = "${metrics.answerRate.toInt()}%",
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Conversion",
                value = "${metrics.conversionRate.toInt()}%",
                accentColor = WarmAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Donut Chart — Intent Distribution ──
        SectionHeader(title = "Intent Distribution")
        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        interested = metrics.interestedCount,
                        notInterested = metrics.notInterestedCount,
                        callback = metrics.callbackCount
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.totalCalls}",
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

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem("Interested", metrics.interestedCount, InterestedColor)
                    LegendItem("Not Interested", metrics.notInterestedCount, NotInterestedColor)
                    LegendItem("Callback", metrics.callbackCount, CallbackColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Weekly Bar Chart ──
        SectionHeader(title = "Calls This Week")
        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            WeeklyBarChart(data = weeklyData)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Performance Summary ──
        SectionHeader(title = "Performance Summary")
        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            PerformanceRow("Active Leads", metrics.activeLeads.toString(), SuccessGreen)
            Spacer(modifier = Modifier.height(14.dp))
            PerformanceRow("Interested Leads", metrics.interestedCount.toString(), InterestedColor)
            Spacer(modifier = Modifier.height(14.dp))
            PerformanceRow("Callbacks Pending", metrics.callbackCount.toString(), CallbackColor)
            Spacer(modifier = Modifier.height(14.dp))
            PerformanceRow("Lost Leads", metrics.notInterestedCount.toString(), NotInterestedColor)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun DonutChart(
    interested: Int,
    notInterested: Int,
    callback: Int
) {
    val total = (interested + notInterested + callback).toFloat().coerceAtLeast(1f)

    var animProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
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

        val intAngle = (interested / total) * 360f * animProgress
        val notIntAngle = (notInterested / total) * 360f * animProgress
        val callAngle = (callback / total) * 360f * animProgress

        var startAngle = -90f

        // Interested
        drawArc(
            color = InterestedColor,
            startAngle = startAngle,
            sweepAngle = intAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += intAngle + 3f

        // Not Interested
        drawArc(
            color = NotInterestedColor,
            startAngle = startAngle,
            sweepAngle = notIntAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += notIntAngle + 3f

        // Callback
        drawArc(
            color = CallbackColor,
            startAngle = startAngle,
            sweepAngle = callAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
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
private fun WeeklyBarChart(data: List<Int>) {
    val maxVal = data.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    var animProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
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
        data.forEachIndexed { index, value ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = (value * animProgress).toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height((100 * (value / maxVal) * animProgress).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (index == data.lastIndex) ElectricIndigo
                            else ElectricIndigo.copy(alpha = 0.4f + (0.6f * value / maxVal))
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = days.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
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
