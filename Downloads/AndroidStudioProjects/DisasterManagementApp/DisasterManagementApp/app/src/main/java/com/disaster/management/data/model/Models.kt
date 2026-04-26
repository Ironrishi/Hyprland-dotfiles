package com.disaster.management.data.model

import com.google.gson.annotations.SerializedName

// ── Request ──────────────────────────────────────────────────────────────────

data class AnalyzeRequest(
    @SerializedName("text") val text: String,
    @SerializedName("location") val location: String? = null
)

// ── Responses ─────────────────────────────────────────────────────────────────

data class AnalyzeResponse(
    @SerializedName("disaster_type") val disasterType: String,
    @SerializedName("risk_level") val riskLevel: String,   // "Low" | "Medium" | "High"
    @SerializedName("confidence") val confidence: String,
    @SerializedName("summary") val summary: String
)

data class Report(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("location") val location: String?,
    @SerializedName("disaster_type") val disasterType: String,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("confidence") val confidence: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("timestamp") val timestamp: String
)

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

enum class RiskLevel(val label: String) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High"), UNKNOWN("Unknown");

    companion object {
        fun from(value: String): RiskLevel =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
