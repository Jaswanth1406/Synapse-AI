package com.example.synapseai.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.synapseai.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CampaignViewModel : ViewModel() {

    private val _config = MutableStateFlow(CampaignConfig())
    val config: StateFlow<CampaignConfig> = _config

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun setObjective(objective: CampaignObjective) {
        _config.value = _config.value.copy(objective = objective)
        _isSaved.value = false
    }

    fun setTone(tone: AgentTone) {
        _config.value = _config.value.copy(tone = tone)
        _isSaved.value = false
    }

    fun setLanguage(language: AgentLanguage) {
        _config.value = _config.value.copy(language = language)
        _isSaved.value = false
    }

    fun saveConfig() {
        _isSaved.value = true
        _toastMessage.value = "Campaign configuration saved!"
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
