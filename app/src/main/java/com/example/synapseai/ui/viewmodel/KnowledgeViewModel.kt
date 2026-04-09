package com.example.synapseai.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapseai.data.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class KnowledgeViewModel : ViewModel() {

    private val callRepository = CallRepository()

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _uploadedFiles = MutableStateFlow<List<String>>(
        listOf("company_faq.pdf", "product_catalog.txt")
    )
    val uploadedFiles: StateFlow<List<String>> = _uploadedFiles

    fun setSelectedFile(name: String) {
        _selectedFileName.value = name
    }

    fun uploadFile(file: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0f

            // Simulate progress
            repeat(10) { i ->
                kotlinx.coroutines.delay(200)
                _uploadProgress.value = (i + 1) / 10f
            }

            callRepository.uploadKnowledge(file).fold(
                onSuccess = {
                    _uploadedFiles.value = _uploadedFiles.value + file.name
                    _toastMessage.value = "File uploaded successfully!"
                },
                onFailure = { e ->
                    _uploadedFiles.value = _uploadedFiles.value + file.name
                    _toastMessage.value = "Uploaded (demo): ${file.name}"
                }
            )

            _isUploading.value = false
            _selectedFileName.value = null
            _uploadProgress.value = 0f
        }
    }

    fun clearToast() { _toastMessage.value = null }
}
