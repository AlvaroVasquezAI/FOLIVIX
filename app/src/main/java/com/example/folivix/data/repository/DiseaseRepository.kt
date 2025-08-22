package com.example.folivix.data.repository

import com.example.folivix.model.DiseaseInfo
import kotlinx.coroutines.flow.Flow

interface DiseaseInfoRepository {
    fun getAllDiseaseInfo(): Flow<List<DiseaseInfo>>
    fun getDiseaseInfo(name: String): Flow<DiseaseInfo?>
    fun getRandomTip(): Flow<String>
}