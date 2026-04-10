package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.ScheduledCallEntry
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _leadName = MutableStateFlow("")
    val leadName: StateFlow<String> = _leadName

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date

    private val _time = MutableStateFlow("")
    val time: StateFlow<String> = _time

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    private val _retryCount = MutableStateFlow(3)
    val retryCount: StateFlow<Int> = _retryCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    // ── Scheduled Calls List ──
    private val _scheduledCalls = MutableStateFlow<List<ScheduledCallEntry>>(emptyList())
    val scheduledCalls: StateFlow<List<ScheduledCallEntry>> = _scheduledCalls

    private val _isLoadingScheduled = MutableStateFlow(false)
    val isLoadingScheduled: StateFlow<Boolean> = _isLoadingScheduled

    private var pollingJob: Job? = null

    init {
        loadScheduledCalls()
        startPolling()
    }

    fun updatePhoneNumber(value: String) { _phoneNumber.value = value }
    fun updateLeadName(value: String) { _leadName.value = value }
    fun updateDate(value: String) { _date.value = value }
    fun updateTime(value: String) { _time.value = value }
    fun updateLanguage(value: String) { _language.value = value }
    fun updateRetryCount(value: Int) { _retryCount.value = value.coerceIn(0, 5) }

    fun scheduleCall() {
        if (_phoneNumber.value.isBlank() || _date.value.isBlank() || _time.value.isBlank()) {
            _toastMessage.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val scheduledTime = "${_date.value}T${_time.value}:00Z"

            callRepository.scheduleCall(
                phoneNumber = _phoneNumber.value,
                scheduledTime = scheduledTime,
                leadName = _leadName.value.ifBlank { "Unknown" },
                language = _language.value,
                retryCount = _retryCount.value
            ).fold(
                onSuccess = {
                    _toastMessage.value = "Call scheduled successfully!"
                    resetForm()
                    loadScheduledCalls() // Refresh list after scheduling
                },
                onFailure = { e ->
                    _toastMessage.value = "Schedule failed: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    fun loadScheduledCalls() {
        viewModelScope.launch {
            _isLoadingScheduled.value = true
            callRepository.fetchScheduledCalls().fold(
                onSuccess = { _scheduledCalls.value = it },
                onFailure = { /* Keep current state on failure */ }
            )
            _isLoadingScheduled.value = false
        }
    }

    fun cancelScheduledCall(id: String) {
        viewModelScope.launch {
            callRepository.cancelScheduledCall(id).fold(
                onSuccess = {
                    _toastMessage.value = "Schedule cancelled"
                    loadScheduledCalls() // Refresh list
                },
                onFailure = { e ->
                    _toastMessage.value = "Cancel failed: ${e.message}"
                }
            )
        }
    }

    private fun resetForm() {
        _phoneNumber.value = ""
        _leadName.value = ""
        _date.value = ""
        _time.value = ""
        _retryCount.value = 3
    }

    fun clearToast() { _toastMessage.value = null }

    /** Auto-refresh scheduled calls list every 20 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000)
                callRepository.fetchScheduledCalls().fold(
                    onSuccess = { _scheduledCalls.value = it },
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
