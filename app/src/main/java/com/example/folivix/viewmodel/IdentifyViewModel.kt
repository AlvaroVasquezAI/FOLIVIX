package com.example.folivix.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.folivix.data.repository.AnalysisRepository
import com.example.folivix.model.AnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import android.util.Log
import com.example.folivix.FolivixApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class IdentifyUiState(
    val selectedImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val result: AnalysisResult? = null,
    val tempImageUri: Uri? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val cropImageUri: Uri? = null,
    val isImageManuallyEdited: Boolean = false
)

@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val TAG = "IdentifyViewModel"

    private val _uiState = MutableStateFlow(IdentifyUiState())
    val uiState: StateFlow<IdentifyUiState> = _uiState.asStateFlow()

    private val diseaseNameMapping = mapOf(
        "Common Rust" to "Roya común",
        "Gray Leaf Spot" to "Mancha gris",
        "Northern Leaf Blight" to "Tizón foliar del norte",
        "Phaeosphaeria Leaf Spot" to "Mancha foliar Phaeosphaeria",
        "Southern Rust" to "Roya del sur",
        "Healthy" to "Saludable"
    )

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(
            selectedImageUri = uri,
            result = null,
            error = null,
            saveSuccess = false,
            isImageManuallyEdited = false
        )}
    }

    private suspend fun ensureSquareImage(context: Context, sourceUri: Uri): Uri = withContext(
        Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val size = minOf(originalBitmap.width, originalBitmap.height)

            val x = (originalBitmap.width - size) / 2
            val y = (originalBitmap.height - size) / 2

            val squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "FOLIVIX_SQUARE_${timeStamp}_"
            val storageDir = context.cacheDir
            val squareImageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            FileOutputStream(squareImageFile).use { out ->
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            originalBitmap.recycle()
            squareBitmap.recycle()

            return@withContext Uri.fromFile(squareImageFile)
        } catch (e: Exception) {
            Log.e("IdentifyViewModel", "Error creating square image", e)
            return@withContext sourceUri
        }
    }

    fun prepareImageCapture(context: Context, onUriReady: (Uri) -> Unit) {
        try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val imageFileName = "FOLIVIX_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                image
            )
            _uiState.update { it.copy(tempImageUri = uri) }
            onUriReady(uri)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al preparar la cámara: ${e.message}") }
        }
    }

    fun onImageCaptured() {
        _uiState.update { it.copy(
            selectedImageUri = it.tempImageUri,
            result = null,
            error = null,
            saveSuccess = false,
            isImageManuallyEdited = false
        )}
    }

    fun prepareCropImage(context: Context): Uri? {
        try {
            val sourceUri = _uiState.value.selectedImageUri ?: return null

            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val imageFileName = "FOLIVIX_CROP_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
            val destinationUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                image
            )

            _uiState.update { it.copy(cropImageUri = destinationUri) }
            return destinationUri
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al preparar el recorte: ${e.message}") }
            return null
        }
    }

    fun onImageCropped(uri: Uri) {
        _uiState.update { it.copy(
            selectedImageUri = uri,
            result = null,
            error = null,
            saveSuccess = false,
            cropImageUri = null,
            isImageManuallyEdited = true
        )}
    }

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.5
        const val NOT_LEAF_MESSAGE = "La imagen no se ve como una hoja"
    }

    fun analyzeImage() {
        val currentUri = _uiState.value.selectedImageUri ?: return

        _uiState.update { it.copy(isAnalyzing = true, error = null) }

        viewModelScope.launch {
            try {
                val finalUri = if (!_uiState.value.isImageManuallyEdited) {
                    val context = FolivixApplication.appContext
                    ensureSquareImage(context, currentUri)
                } else {
                    currentUri
                }

                _uiState.update { it.copy(selectedImageUri = finalUri) }

                val result = analysisRepository.analyzeImage(finalUri)

                if (result.confidence <= CONFIDENCE_THRESHOLD) {
                    val notLeafResult = result.copy(
                        diseaseType = NOT_LEAF_MESSAGE,
                        confidence = result.confidence
                    )

                    _uiState.update { it.copy(isAnalyzing = false, result = notLeafResult) }
                } else {
                    val translatedDiseaseType = translateDiseaseType(result.diseaseType)

                    val translatedResult = result.copy(diseaseType = translatedDiseaseType)

                    Log.d(TAG, "Original disease type: ${result.diseaseType}, Translated: $translatedDiseaseType")

                    _uiState.update { it.copy(isAnalyzing = false, result = translatedResult) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Error al analizar la imagen: ${e.message}"
                    )
                }
            }
        }
    }

    private fun translateDiseaseType(diseaseType: String): String {
        return diseaseNameMapping[diseaseType] ?: diseaseType
    }

    fun saveResult() {
        val result = _uiState.value.result ?: return

        viewModelScope.launch {
            try {
                analysisRepository.saveAnalysisResult(result)
                _uiState.update { it.copy(saveSuccess = true) }

                launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(saveSuccess = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar el resultado: ${e.message}") }
            }
        }
    }

    fun resetState() {
        _uiState.update {
            IdentifyUiState(
                tempImageUri = it.tempImageUri
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
}