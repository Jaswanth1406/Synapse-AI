package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.repository.AnalyticsMetrics
import com.example.synapseai.data.repository.AnalyticsRepository
import com.example.synapseai.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {

    private val contactRepository = ContactRepository()
    private val analyticsRepository = AnalyticsRepository()

    private val _metrics = MutableStateFlow(
        AnalyticsMetrics(
            totalCalls = 156,
            answeredCalls = 124,
            answerRate = 79.5f,
            interestedCount = 43,
            notInterestedCount = 61,
            callbackCount = 20,
            conversionRate = 27.6f,
            activeLeads = 63
        )
    )
    val metrics: StateFlow<AnalyticsMetrics> = _metrics

    private val _weeklyData = MutableStateFlow(
        listOf(12, 18, 24, 15, 30, 22, 28)
    )
    val weeklyData: StateFlow<List<Int>> = _weeklyData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun refreshAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            contactRepository.fetchCallLogs().fold(
                onSuccess = { logs ->
                    _metrics.value = analyticsRepository.computeMetrics(logs)
                },
                onFailure = { /* Keep demo data */ }
            )
            _isLoading.value = false
        }
    }
}
