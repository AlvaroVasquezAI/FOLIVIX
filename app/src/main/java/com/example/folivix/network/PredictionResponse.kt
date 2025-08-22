package com.example.folivix.network

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("className")
    val className: String,

    @SerializedName("confidence")
    val confidence: Float,

    @SerializedName("processingTime")
    val processingTime: Float
)