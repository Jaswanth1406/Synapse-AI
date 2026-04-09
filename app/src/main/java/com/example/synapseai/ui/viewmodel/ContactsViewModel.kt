package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.Contact
import com.example.synapseai.data.model.LeadStatus
import com.example.synapseai.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val repository = ContactRepository()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        loadDemoContacts()
    }

    private fun loadDemoContacts() {
        val demoContacts = listOf(
            Contact("1", "Rahul Sharma", "+91 98765 43210", "TechCorp India", LeadStatus.INTERESTED),
            Contact("2", "Priya Patel", "+91 87654 32109", "DataVision Ltd", LeadStatus.CALLBACK),
            Contact("3", "Arjun Menon", "+91 76543 21098", "CloudNine Solutions", LeadStatus.NOT_INTERESTED),
            Contact("4", "Sneha Reddy", "+91 65432 10987", "InnovateTech", LeadStatus.PENDING),
            Contact("5", "Vikram Singh", "+91 54321 09876", "GrowthHub", LeadStatus.INTERESTED),
            Contact("6", "Ananya Gupta", "+91 43210 98765", "FutureStack", LeadStatus.PENDING),
            Contact("7", "Karthik Nair", "+91 32109 87654", "ByteForce", LeadStatus.CALLBACK),
            Contact("8", "Meera Joshi", "+91 21098 76543", "QuantumLeap AI", LeadStatus.NO_ANSWER)
        )
        repository.addLocalContacts(demoContacts)
        _contacts.value = repository.getLocalContacts()
    }

    fun syncFromCrm() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.fetchLeadsFromCrm().fold(
                onSuccess = { leads ->
                    val converted = leads.map { lead ->
                        Contact(
                            id = lead.id,
                            name = "${lead.firstName ?: ""} ${lead.lastName ?: ""}".trim().ifEmpty { lead.name ?: "Unknown" },
                            phoneNumber = lead.phoneNumber ?: "",
                            company = lead.company ?: "",
                            leadStatus = when (lead.status?.lowercase()) {
                                "converted" -> LeadStatus.INTERESTED
                                "dead" -> LeadStatus.NOT_INTERESTED
                                else -> LeadStatus.PENDING
                            }
                        )
                    }
                    repository.addLocalContacts(converted)
                    _contacts.value = repository.getLocalContacts()
                    _toastMessage.value = "Synced ${leads.size} leads from CRM"
                },
                onFailure = { e ->
                    _toastMessage.value = "CRM sync failed: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    fun addContact(name: String, phone: String, company: String) {
        val contact = Contact(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            phoneNumber = phone,
            company = company
        )
        repository.addLocalContact(contact)
        _contacts.value = repository.getLocalContacts()
        _showAddDialog.value = false
        _toastMessage.value = "Contact added"
    }

    fun importCsv(csvContent: String) {
        val parsed = repository.parseCsvContacts(csvContent)
        if (parsed.isNotEmpty()) {
            repository.addLocalContacts(parsed)
            _contacts.value = repository.getLocalContacts()
            _toastMessage.value = "Imported ${parsed.size} contacts"
        } else {
            _toastMessage.value = "No valid contacts found in CSV"
        }
    }

    fun deleteContact(id: String) {
        repository.removeLocalContact(id)
        _contacts.value = repository.getLocalContacts()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getFilteredContacts(): List<Contact> {
        val query = _searchQuery.value.lowercase()
        return if (query.isBlank()) _contacts.value
        else _contacts.value.filter {
            it.name.lowercase().contains(query) ||
            it.company.lowercase().contains(query) ||
            it.phoneNumber.contains(query)
        }
    }
}
