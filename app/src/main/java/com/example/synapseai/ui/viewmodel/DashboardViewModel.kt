package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.AnalyticsResponse
import com.example.synapseai.data.model.CallHistoryEntry
import com.example.synapseai.data.repository.AnalyticsRepository
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val callRepository = CallRepository()
    private val analyticsRepository = AnalyticsRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isHealthy = MutableStateFlow<Boolean?>(null)
    val isHealthy: StateFlow<Boolean?> = _isHealthy

    private val _analytics = MutableStateFlow(AnalyticsResponse())
    val analytics: StateFlow<AnalyticsResponse> = _analytics

    private val _recentCalls = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val recentCalls: StateFlow<List<CallHistoryEntry>> = _recentCalls

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var pollingJob: Job? = null

    init {
        refreshDashboard()
        startPolling()
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

            // Fetch live analytics
            analyticsRepository.fetchAnalytics().fold(
                onSuccess = { _analytics.value = it },
                onFailure = { e -> _error.value = "Analytics: ${e.message}" }
            )

            // Fetch live call history
            callRepository.fetchCallHistory().fold(
                onSuccess = { _recentCalls.value = it.take(10) },
                onFailure = { /* Call history may be empty */ }
            )

            _isLoading.value = false
        }
    }

    /** Auto-refresh dashboard data every 20 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000)
                refreshDashboard()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
