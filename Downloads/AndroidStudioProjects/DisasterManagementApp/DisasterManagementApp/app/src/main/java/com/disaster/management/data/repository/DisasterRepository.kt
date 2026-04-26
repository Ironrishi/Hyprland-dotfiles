package com.disaster.management.data.repository

import com.disaster.management.data.api.DisasterApiService
import com.disaster.management.data.model.AnalyzeRequest
import com.disaster.management.data.model.AnalyzeResponse
import com.disaster.management.data.model.Report

class DisasterRepository(private val api: DisasterApiService) {

    suspend fun analyzeReport(text: String, location: String?): Result<AnalyzeResponse> {
        return try {
            val response = api.analyzeReport(AnalyzeRequest(text = text, location = location))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Server error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReports(): Result<List<Report>> {
        return try {
            val response = api.getReports()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
