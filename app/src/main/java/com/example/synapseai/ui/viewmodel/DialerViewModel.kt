package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.CallHistoryEntry
import com.example.synapseai.data.model.CallState
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DialerViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _leadName = MutableStateFlow("")
    val leadName: StateFlow<String> = _leadName

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _callHistory = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val callHistory: StateFlow<List<CallHistoryEntry>> = _callHistory

    private val _lastCallResponse = MutableStateFlow<String?>(null)
    val lastCallResponse: StateFlow<String?> = _lastCallResponse

    private var pollingJob: Job? = null

    init {
        loadCallHistory()
        startPolling()
    }

    fun updatePhoneNumber(number: String) { _phoneNumber.value = number }
    fun updateLeadName(name: String) { _leadName.value = name }
    fun updateLanguage(lang: String) { _language.value = lang }

    fun dialNow() {
        if (_phoneNumber.value.isBlank()) {
            _error.value = "Please enter a phone number"
            return
        }

        viewModelScope.launch {
            _callState.value = CallState.INITIATING
            _error.value = null

            callRepository.triggerCall(
                phoneNumber = _phoneNumber.value,
                leadName = _leadName.value.ifBlank { "Unknown" },
                language = _language.value
            ).fold(
                onSuccess = { response ->
                    _callState.value = CallState.CONNECTED
                    val runName = response.callData?.workflowRunName ?: "N/A"
                    _lastCallResponse.value = "Run: $runName | ${response.message ?: response.status ?: "Triggered"}"
                    // Refresh history after triggering
                    loadCallHistory()
                },
                onFailure = { e ->
                    _callState.value = CallState.FAILED
                    _error.value = "Call failed: ${e.message}"
                }
            )
        }
    }

    fun endCall() {
        _callState.value = CallState.CONCLUDED
    }

    fun resetCall() {
        _callState.value = CallState.IDLE
        _phoneNumber.value = ""
        _leadName.value = ""
        _language.value = "en"
        _error.value = null
        _lastCallResponse.value = null
    }

    private fun loadCallHistory() {
        viewModelScope.launch {
            callRepository.fetchCallHistory().fold(
                onSuccess = { _callHistory.value = it.take(20) },
                onFailure = { /* Keep current state */ }
            )
        }
    }

    fun refreshHistory() = loadCallHistory()

    /** Auto-refresh call history every 15 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(15_000)
                loadCallHistory()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
