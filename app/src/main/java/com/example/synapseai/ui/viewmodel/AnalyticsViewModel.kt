package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.model.AnalyticsResponse
import com.example.synapseai.data.model.BackfillResponse
import com.example.synapseai.data.repository.AnalyticsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {

    private val analyticsRepository = AnalyticsRepository()

    private val _analytics = MutableStateFlow(AnalyticsResponse())
    val analytics: StateFlow<AnalyticsResponse> = _analytics

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var pollingJob: Job? = null

    init {
        refreshAnalytics()
        startPolling()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            analyticsRepository.fetchAnalytics().fold(
                onSuccess = { _analytics.value = it },
                onFailure = { e -> _error.value = "Failed to load analytics: ${e.message}" }
            )
            _isLoading.value = false
        }
    }

    fun triggerBackfill() {
        viewModelScope.launch {
            _toastMessage.value = "Running backfill..."
            analyticsRepository.triggerBackfill().fold(
                onSuccess = { result ->
                    _toastMessage.value = "Backfill complete: ${result.updated}/${result.totalCandidates} updated"
                    refreshAnalytics()
                },
                onFailure = { e ->
                    _toastMessage.value = "Backfill failed: ${e.message}"
                }
            )
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _toastMessage.value = "Downloading CSV..."
            analyticsRepository.downloadCsv().fold(
                onSuccess = { _toastMessage.value = "CSV ready for export" },
                onFailure = { e -> _toastMessage.value = "CSV export failed: ${e.message}" }
            )
        }
    }

    fun clearToast() { _toastMessage.value = null }

    /** Auto-refresh analytics every 30 seconds */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                analyticsRepository.fetchAnalytics().fold(
                    onSuccess = { _analytics.value = it },
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
