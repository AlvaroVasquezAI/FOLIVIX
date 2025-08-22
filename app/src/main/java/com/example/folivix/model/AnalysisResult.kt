package com.example.folivix.model

import android.net.Uri

data class AnalysisResult(
    val id: String,
    val imageUri: Uri?,
    val timestamp: Long,
    val diseaseType: String,
    val confidence: Double,
    val processingTime: String
)