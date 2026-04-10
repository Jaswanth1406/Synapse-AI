package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.CallHistoryEntry
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CallHistoryViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _callHistory = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val callHistory: StateFlow<List<CallHistoryEntry>> = _callHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedEntry = MutableStateFlow<CallHistoryEntry?>(null)
    val selectedEntry: StateFlow<CallHistoryEntry?> = _selectedEntry

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var pollingJob: Job? = null

    init {
        refresh()
        startPolling()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            callRepository.fetchCallHistory().fold(
                onSuccess = { _callHistory.value = it },
                onFailure = { e -> _error.value = e.message }
            )
            _isLoading.value = false
        }
    }

    fun selectEntry(entry: CallHistoryEntry?) {
        _selectedEntry.value = entry
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredHistory(): List<CallHistoryEntry> {
        val query = _searchQuery.value.lowercase()
        return if (query.isBlank()) _callHistory.value
        else _callHistory.value.filter {
            it.phoneNumber?.lowercase()?.contains(query) == true ||
            it.intent?.lowercase()?.contains(query) == true ||
            it.summary?.lowercase()?.contains(query) == true ||
            it.status?.lowercase()?.contains(query) == true
        }
    }

    /** Auto-refresh call history every 15 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(15_000)
                callRepository.fetchCallHistory().fold(
                    onSuccess = { _callHistory.value = it },
                    onFailure = { /* Keep current state on background poll failure */ }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
