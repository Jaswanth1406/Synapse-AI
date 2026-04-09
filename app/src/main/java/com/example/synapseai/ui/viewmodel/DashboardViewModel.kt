package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.EspoCallLog
import com.example.synapseai.data.repository.AnalyticsMetrics
import com.example.synapseai.data.repository.AnalyticsRepository
import com.example.synapseai.data.repository.CallRepository
import com.example.synapseai.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val callRepository = CallRepository()
    private val contactRepository = ContactRepository()
    private val analyticsRepository = AnalyticsRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isHealthy = MutableStateFlow<Boolean?>(null)
    val isHealthy: StateFlow<Boolean?> = _isHealthy

    private val _metrics = MutableStateFlow(AnalyticsMetrics())
    val metrics: StateFlow<AnalyticsMetrics> = _metrics

    private val _recentCalls = MutableStateFlow<List<EspoCallLog>>(emptyList())
    val recentCalls: StateFlow<List<EspoCallLog>> = _recentCalls

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Health check
            callRepository.healthCheck().fold(
                onSuccess = { _isHealthy.value = true },
                onFailure = { _isHealthy.value = false }
            )

            // Fetch call logs for metrics
            contactRepository.fetchCallLogs().fold(
                onSuccess = { logs ->
                    _recentCalls.value = logs.take(10)
                    _metrics.value = analyticsRepository.computeMetrics(logs)
                },
                onFailure = { e ->
                    _error.value = e.message
                    // Use demo data when API is unavailable
                    _metrics.value = AnalyticsMetrics(
                        totalCalls = 156,
                        answeredCalls = 124,
                        answerRate = 79.5f,
                        interestedCount = 43,
                        notInterestedCount = 61,
                        callbackCount = 20,
                        conversionRate = 27.6f,
                        activeLeads = 63
                    )
                }
            )

            _isLoading.value = false
        }
    }
}
