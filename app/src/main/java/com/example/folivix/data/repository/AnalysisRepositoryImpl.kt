package com.example.folivix.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.folivix.data.storage.FileStorageManager
import com.example.folivix.model.AnalysisResult
import com.example.folivix.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisRepositoryImpl @Inject constructor(
    private val fileStorageManager: FileStorageManager,
    private val userRepository: UserRepository
) : AnalysisRepository {

    private val TAG = "AnalysisRepositoryImpl"

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val analysisResults = MutableStateFlow<List<AnalysisResult>>(emptyList())
    private val currentUserId = MutableStateFlow<String?>(null)

    init {
        repositoryScope.launch {
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    currentUserId.value = user.id
                    loadResultsForCurrentUser()
                } else {
                    currentUserId.value = null
                    analysisResults.value = emptyList()
                }
            }
        }
    }

    private suspend fun loadResultsForCurrentUser() {
        val userId = currentUserId.value ?: return
        try {
            val results = fileStorageManager.getAnalysisResults(userId)
            Log.d(TAG, "Loaded ${results.size} results for user $userId")
            analysisResults.value = results
        } catch (e: Exception) {
            Log.e(TAG, "Error loading results for user $userId", e)
            analysisResults.value = emptyList()
        }
    }

    override suspend fun analyzeImage(imageUri: Uri): AnalysisResult {
        try {
            val context = com.example.folivix.FolivixApplication.appContext
            val file = uriToFile(context, imageUri)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = RetrofitClient.apiService.predictDisease(imagePart)

            if (response.isSuccessful) {
                val predictionResponse = response.body()!!

                Log.d(TAG, "Analysis successful: ${predictionResponse.className} with confidence ${predictionResponse.confidence}")

                return AnalysisResult(
                    id = System.currentTimeMillis().toString(),
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    diseaseType = predictionResponse.className,
                    confidence = predictionResponse.confidence.toDouble(),
                    processingTime = predictionResponse.processingTime.toString()
                )
            } else {
                Log.e(TAG, "Server error: ${response.code()}")
                throw Exception("Error en la respuesta del servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            throw Exception("Error al analizar la imagen: ${e.message}")
        }
    }

    override suspend fun saveAnalysisResult(result: AnalysisResult) {
        val userId = currentUserId.value
        if (userId != null) {
            try {
                Log.d(TAG, "Saving analysis result for user $userId")
                fileStorageManager.saveAnalysisResult(userId, result)

                val currentList = analysisResults.value.toMutableList()
                currentList.add(0, result)
                analysisResults.value = currentList

                Log.d(TAG, "Analysis result saved successfully, new count: ${currentList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving analysis result", e)
            }
        } else {
            Log.e(TAG, "Cannot save result: No current user")
        }
    }

    override fun getAnalysisResults(): Flow<List<AnalysisResult>> {
        return analysisResults
    }

    private val diseaseNameMapping = mapOf(
        "Common Rust" to "Roya común",
        "Gray Leaf Spot" to "Mancha gris",
        "Northern Leaf Blight" to "Tizón foliar del norte",
        "Phaeosphaeria Leaf Spot" to "Mancha foliar Phaeosphaeria",
        "Southern Rust" to "Roya del sur",
        "Healthy" to "Saludable"
    )

    private fun translateDiseaseType(diseaseType: String): String {
        return diseaseNameMapping[diseaseType] ?: diseaseType
    }

    override fun getAnalysisStatistics(): Flow<AnalysisStatistics> {
        return analysisResults.map { results ->
            val totalLeaves = results.size

            val averageAccuracy = if (totalLeaves > 0) {
                (results.sumOf { it.confidence } / totalLeaves * 100).toInt()
            } else {
                0
            }

            val diseaseGroups = results.groupBy { result ->
                translateDiseaseType(result.diseaseType)
            }

            val diseaseStats = mutableMapOf<String, String>()
            val knownDiseases = listOf(
                "Roya común",
                "Mancha gris",
                "Tizón foliar del norte",
                "Mancha foliar Phaeosphaeria",
                "Roya del sur",
                "Saludable"
            )

            knownDiseases.forEach { disease ->
                diseaseStats[disease] = "0-0%"
            }

            diseaseGroups.forEach { (diseaseType, resultsForDisease) ->
                val count = resultsForDisease.size
                val avgConfidence = if (count > 0) {
                    (resultsForDisease.sumOf { it.confidence } / count * 100).toInt()
                } else {
                    0
                }
                diseaseStats[diseaseType] = "$count-$avgConfidence%"
            }

            Log.d(TAG, "Statistics calculated: total=$totalLeaves, avg=$averageAccuracy")
            Log.d(TAG, "Disease stats: $diseaseStats")
            Log.d(TAG, "Original disease types: ${results.map { it.diseaseType }.distinct()}")

            AnalysisStatistics(
                totalLeaves = totalLeaves,
                averageAccuracy = averageAccuracy,
                diseaseStats = diseaseStats
            )
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("image", ".jpg", context.cacheDir)
        tempFile.deleteOnExit()

        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }

        return tempFile
    }

    override suspend fun deleteAnalysisResult(resultId: String) {
        val userId = currentUserId.value
        if (userId != null) {
            try {
                Log.d(TAG, "Deleting analysis result $resultId for user $userId")

                fileStorageManager.deleteAnalysisResult(userId, resultId)

                val currentList = analysisResults.value.toMutableList()
                val updatedList = currentList.filter { it.id != resultId }
                analysisResults.value = updatedList

                Log.d(TAG, "Analysis result deleted successfully, new count: ${updatedList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting analysis result", e)
                throw e
            }
        } else {
            Log.e(TAG, "Cannot delete result: No current user")
            throw Exception("No hay usuario actual")
        }
    }
}