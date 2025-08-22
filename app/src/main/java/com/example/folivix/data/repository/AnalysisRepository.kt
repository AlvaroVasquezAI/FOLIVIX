package com.example.folivix.data.repository

import android.net.Uri
import com.example.folivix.model.AnalysisResult
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    suspend fun analyzeImage(imageUri: Uri): AnalysisResult
    suspend fun saveAnalysisResult(result: AnalysisResult)
    suspend fun deleteAnalysisResult(resultId: String)
    fun getAnalysisResults(): Flow<List<AnalysisResult>>
    fun getAnalysisStatistics(): Flow<AnalysisStatistics>
}

data class AnalysisStatistics(
    val totalLeaves: Int = 0,
    val averageAccuracy: Int = 0,
    val diseaseStats: Map<String, String> = emptyMap()
)