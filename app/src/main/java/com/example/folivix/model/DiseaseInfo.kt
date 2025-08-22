package com.example.folivix.model

data class DiseaseInfo(
    val name: String,
    val description: String,
    val imageResId: Int,
    val detailImageResIds: List<Int> = emptyList(),
    val detailedDescription: String = description
)
