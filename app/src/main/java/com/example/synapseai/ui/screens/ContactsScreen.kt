package com.example.synapseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synapseai.data.model.Contact
import com.example.synapseai.data.model.LeadStatus
import com.example.synapseai.ui.components.*
import com.example.synapseai.ui.theme.*
import com.example.synapseai.ui.viewmodel.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = viewModel(),
    onCallContact: (String) -> Unit = {}
) {
    val contacts by viewModel.contacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val filteredContacts = viewModel.getFilteredContacts()

    // Toast
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
                        text = "Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${contacts.size} leads in pipeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                IconButton(onClick = { viewModel.fetchCrmLeads() }) {
                    Icon(Icons.Filled.Sync, "Sync CRM", tint = ElectricIndigo)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search contacts...", color = TextTertiary) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Demo CSV import
                        viewModel.importCsv("Name,Phone,Company\nDemo User,+91 99999 00001,TestCorp")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmAmber),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.UploadFile, "CSV", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import CSV", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { viewModel.pushLeadsToCRM() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Filled.CloudUpload, "CRM", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push to CRM", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredContacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onCall = { onCallContact(contact.phoneNumber) },
                        onTriggerAI = { viewModel.triggerCallForContact(contact) },
                        onDelete = { viewModel.deleteContact(contact.id) }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.toggleAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp),
            containerColor = ElectricIndigo,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PersonAdd, "Add Contact")
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

        // Add Contact Dialog
        if (showAddDialog) {
            AddContactDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                onAdd = { name, phone, company -> viewModel.addContact(name, phone, company) }
            )
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    onCall: () -> Unit,
    onTriggerAI: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (contact.leadStatus) {
        LeadStatus.INTERESTED -> InterestedColor
        LeadStatus.NOT_INTERESTED -> NotInterestedColor
        LeadStatus.CALLBACK -> CallbackColor
        LeadStatus.PENDING -> TextTertiary
        LeadStatus.NO_ANSWER -> WarningOrange
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (contact.company.isNotBlank()) {
                    Text(
                        text = contact.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusChip(
                    label = contact.leadStatus.label,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    IconButton(
                        onClick = onTriggerAI,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.SmartToy,
                            "AI Call",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Phone,
                            "Call",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            "Delete",
                            tint = ErrorRed.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Add Contact",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = TextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = ElectricIndigo,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = TextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = ElectricIndigo,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company", color = TextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = ElectricIndigo,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GradientButton(
                text = "Add Contact",
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(name, phone, company) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
