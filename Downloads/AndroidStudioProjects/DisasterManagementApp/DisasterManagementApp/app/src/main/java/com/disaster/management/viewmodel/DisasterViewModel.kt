package com.disaster.management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disaster.management.data.api.RetrofitClient
import com.disaster.management.data.model.AnalyzeResponse
import com.disaster.management.data.model.Report
import com.disaster.management.data.model.UiState
import com.disaster.management.data.repository.DisasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DisasterViewModel : ViewModel() {

    private val repository = DisasterRepository(RetrofitClient.apiService)

    // ── Analyze ───────────────────────────────────────────────────────────────
    private val _analyzeState = MutableStateFlow<UiState<AnalyzeResponse>>(UiState.Idle)
    val analyzeState: StateFlow<UiState<AnalyzeResponse>> = _analyzeState.asStateFlow()

    // ── Reports ───────────────────────────────────────────────────────────────
    private val _reportsState = MutableStateFlow<UiState<List<Report>>>(UiState.Idle)
    val reportsState: StateFlow<UiState<List<Report>>> = _reportsState.asStateFlow()

    // ── Form fields ───────────────────────────────────────────────────────────
    private val _reportText = MutableStateFlow("")
    val reportText: StateFlow<String> = _reportText.asStateFlow()

    private val _locationText = MutableStateFlow("")
    val locationText: StateFlow<String> = _locationText.asStateFlow()

    fun onReportTextChange(value: String) { _reportText.value = value }
    fun onLocationTextChange(value: String) { _locationText.value = value }

    fun setLocationFromGps(latLng: String) { _locationText.value = latLng }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun submitReport() {
        val text = _reportText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _analyzeState.value = UiState.Loading
            val location = _locationText.value.takeIf { it.isNotBlank() }
            val result = repository.analyzeReport(text, location)
            _analyzeState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _reportsState.value = UiState.Loading
            val result = repository.getReports()
            _reportsState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetAnalyzeState() {
        _analyzeState.value = UiState.Idle
        _reportText.value = ""
        _locationText.value = ""
    }
}
