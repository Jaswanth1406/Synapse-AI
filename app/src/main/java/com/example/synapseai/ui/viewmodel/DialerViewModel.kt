package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.CallState
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DialerViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _leadName = MutableStateFlow("")
    val leadName: StateFlow<String> = _leadName

    private val _company = MutableStateFlow("")
    val company: StateFlow<String> = _company

    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _callHistory = MutableStateFlow<List<CallHistoryItem>>(
        listOf(
            CallHistoryItem("+91 98765 43210", "Rahul Sharma", "Connected", "2m 34s"),
            CallHistoryItem("+91 87654 32109", "Priya Patel", "No Answer", "0s"),
            CallHistoryItem("+91 76543 21098", "Arjun Menon", "Concluded", "4m 12s")
        )
    )
    val callHistory: StateFlow<List<CallHistoryItem>> = _callHistory

    fun updatePhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    fun updateLeadName(name: String) {
        _leadName.value = name
    }

    fun updateCompany(company: String) {
        _company.value = company
    }

    fun dialNow() {
        if (_phoneNumber.value.isBlank()) {
            _error.value = "Please enter a phone number"
            return
        }

        viewModelScope.launch {
            _callState.value = CallState.INITIATING
            _error.value = null

            val context = mutableMapOf<String, String>()
            if (_leadName.value.isNotBlank()) context["lead_name"] = _leadName.value
            if (_company.value.isNotBlank()) context["company"] = _company.value

            callRepository.triggerCall(_phoneNumber.value, context).fold(
                onSuccess = { response ->
                    _callState.value = CallState.CONNECTED
                    simulateCallProgress()
                },
                onFailure = { e ->
                    // Simulate call progression for demo
                    _callState.value = CallState.CONNECTED
                    simulateCallProgress()
                }
            )
        }
    }

    private fun simulateCallProgress() {
        viewModelScope.launch {
            delay(2000)
            _callState.value = CallState.SPEAKING
            _callDuration.value = 0

            // Count duration
            repeat(30) {
                delay(1000)
                _callDuration.value = it + 1
                if (_callState.value != CallState.SPEAKING) return@launch
            }

            _callState.value = CallState.CONCLUDED
            addToHistory()
        }
    }

    fun endCall() {
        _callState.value = CallState.CONCLUDED
        addToHistory()
    }

    fun resetCall() {
        _callState.value = CallState.IDLE
        _callDuration.value = 0
        _phoneNumber.value = ""
        _leadName.value = ""
        _company.value = ""
    }

    private fun addToHistory() {
        val duration = _callDuration.value
        val durationStr = "${duration / 60}m ${duration % 60}s"
        val item = CallHistoryItem(
            _phoneNumber.value,
            _leadName.value.ifBlank { "Unknown" },
            "Concluded",
            durationStr
        )
        _callHistory.value = listOf(item) + _callHistory.value
    }

    fun clearError() {
        _error.value = null
    }
}

data class CallHistoryItem(
    val phoneNumber: String,
    val name: String,
    val status: String,
    val duration: String
)
