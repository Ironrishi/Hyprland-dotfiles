package com.disaster.management.data.api

import com.disaster.management.data.model.AnalyzeRequest
import com.disaster.management.data.model.AnalyzeResponse
import com.disaster.management.data.model.Report
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DisasterApiService {

    @POST("api/analyze")
    suspend fun analyzeReport(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @GET("api/reports")
    suspend fun getReports(): Response<List<Report>>
}
