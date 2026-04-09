package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.*
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.CampaignViewModel

@Composable
fun CampaignScreen(
    viewModel: CampaignViewModel = viewModel(),
    onUploadKnowledge: () -> Unit = {}
) {
    val config by viewModel.config.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
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

            Text(
                text = "Campaign Config",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure your AI calling agent",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Objective Selection ──
            SectionHeader(title = "Campaign Objective")
            Spacer(modifier = Modifier.height(12.dp))

            CampaignObjective.entries.forEach { objective ->
                ObjectiveCard(
                    objective = objective,
                    isSelected = config.objective == objective,
                    onClick = { viewModel.setObjective(objective) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Tone / Persona ──
            SectionHeader(title = "AI Tone & Persona")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgentTone.entries.forEach { tone ->
                    ToneChip(
                        tone = tone,
                        isSelected = config.tone == tone,
                        onClick = { viewModel.setTone(tone) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Language Selection ──
            SectionHeader(title = "Spoken Language")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgentLanguage.entries.forEach { lang ->
                    LanguageCard(
                        language = lang,
                        isSelected = config.language == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Knowledge Base ──
            SectionHeader(title = "RAG Knowledge Base")
            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CloudUpload, "Upload", tint = CyanAccent)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Upload Business Documents",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "PDF, TXT files for AI context",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    IconButton(onClick = onUploadKnowledge) {
                        Icon(Icons.Filled.ArrowForward, "Go", tint = CyanAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Save Button ──
            GradientButton(
                text = if (isSaved) "✓ Configuration Saved" else "Save Configuration",
                onClick = { viewModel.saveConfig() },
                modifier = Modifier.fillMaxWidth(),
                colors = if (isSaved) listOf(SuccessGreen, SuccessGreenLight)
                         else listOf(ElectricIndigo, CyanAccent)
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

@Composable
private fun ObjectiveCard(
    objective: CampaignObjective,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (objective) {
        CampaignObjective.BOOK_MEETING -> Icons.Filled.EventAvailable
        CampaignObjective.GAUGE_INTEREST -> Icons.Filled.TrendingUp
        CampaignObjective.SEND_LINK -> Icons.Filled.Link
    }
    val borderColor = if (isSelected) ElectricIndigo else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) ElectricIndigo.copy(alpha = 0.1f) else DarkCard)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) ElectricIndigo.copy(alpha = 0.2f)
                    else GlassBackground
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, objective.label,
                tint = if (isSelected) ElectricIndigo else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = objective.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = objective.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        if (isSelected) {
            Icon(
                Icons.Filled.CheckCircle, "Selected",
                tint = ElectricIndigo,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ToneChip(
    tone: AgentTone,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricIndigo.copy(alpha = 0.15f) else DarkCard)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) ElectricIndigo else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tone.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) ElectricIndigo else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun LanguageCard(
    language: AgentLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flag = when (language) {
        AgentLanguage.ENGLISH -> "🇺🇸"
        AgentLanguage.HINDI -> "🇮🇳"
        AgentLanguage.TAMIL -> "🇮🇳"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyanAccent.copy(alpha = 0.1f) else DarkCard)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) CyanAccent else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = flag, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = language.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) CyanAccent else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
