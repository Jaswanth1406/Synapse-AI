package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date

    private val _time = MutableStateFlow("")
    val time: StateFlow<String> = _time

    private val _language = MutableStateFlow("english")
    val language: StateFlow<String> = _language

    private val _retryCount = MutableStateFlow(0)
    val retryCount: StateFlow<Int> = _retryCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun updatePhoneNumber(value: String) { _phoneNumber.value = value }
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
                language = _language.value,
                retryCount = _retryCount.value
            ).fold(
                onSuccess = {
                    _toastMessage.value = "Call scheduled successfully!"
                    resetForm()
                },
                onFailure = { e ->
                    _toastMessage.value = "Scheduled (offline): ${e.message}"
                    resetForm()
                }
            )
            _isLoading.value = false
        }
    }

    private fun resetForm() {
        _phoneNumber.value = ""
        _date.value = ""
        _time.value = ""
        _retryCount.value = 0
    }

    fun clearToast() { _toastMessage.value = null }
}
