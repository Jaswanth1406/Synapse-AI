package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.Contact
import com.example.synapseai.data.model.LeadStatus
import com.example.synapseai.data.repository.CallRepository
import com.example.synapseai.data.repository.ContactRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val repository = ContactRepository()
    private val callRepository = CallRepository()

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

    private var pollingJob: Job? = null

    init {
        // Fetch CRM leads from the backend on first load
        fetchCrmLeads()
        startPolling()
    }

    /**
     * Pull leads from EspoCRM via GET /api/leads and merge into local store.
     */
    fun fetchCrmLeads() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.fetchCrmLeads().fold(
                onSuccess = { crmContacts ->
                    // Add fetched CRM leads that aren't already in local store
                    val existingIds = repository.getLocalContacts().map { it.id }.toSet()
                    val newLeads = crmContacts.filter { it.id !in existingIds }
                    if (newLeads.isNotEmpty()) {
                        repository.addLocalContacts(newLeads)
                    }
                    _contacts.value = repository.getLocalContacts()
                    if (crmContacts.isNotEmpty()) {
                        _toastMessage.value = "Loaded ${crmContacts.size} leads from CRM"
                    }
                },
                onFailure = {
                    // CRM fetch failed — just show local contacts
                    _contacts.value = repository.getLocalContacts()
                }
            )
            _isLoading.value = false
        }
    }

    fun triggerCallForContact(contact: Contact) {
        viewModelScope.launch {
            _isLoading.value = true
            callRepository.triggerCall(
                phoneNumber = contact.phoneNumber,
                leadName = contact.name
            ).fold(
                onSuccess = { response ->
                    _toastMessage.value = "Call triggered for ${contact.name}: ${response.message ?: response.status ?: "OK"}"
                },
                onFailure = { e ->
                    _toastMessage.value = "Call failed: ${e.message}"
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

    fun pushLeadsToCRM() {
        val currentContacts = _contacts.value
        if (currentContacts.isEmpty()) {
            _toastMessage.value = "No contacts to push"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.pushLeadsBatch(currentContacts).fold(
                onSuccess = { response ->
                    _toastMessage.value = "Pushed ${response.created} leads to CRM: ${response.message ?: "OK"}"
                },
                onFailure = { e ->
                    _toastMessage.value = "CRM push failed: ${e.message}"
                }
            )
            _isLoading.value = false
        }
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

    /** Auto-refresh CRM leads every 30 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                repository.fetchCrmLeads().fold(
                    onSuccess = { crmContacts ->
                        val existingIds = repository.getLocalContacts().map { it.id }.toSet()
                        val newLeads = crmContacts.filter { it.id !in existingIds }
                        if (newLeads.isNotEmpty()) {
                            repository.addLocalContacts(newLeads)
                            _contacts.value = repository.getLocalContacts()
                        }
                    },
                    onFailure = { /* Silently ignore background poll failures */ }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
