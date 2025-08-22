package com.example.folivix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.folivix.data.repository.AnalysisRepository
import com.example.folivix.data.repository.DiseaseInfoRepository
import com.example.folivix.model.DiseaseInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.example.folivix.model.DiseaseTip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

data class HomeUiState(
    val totalLeaves: Int = 0,
    val averageAccuracy: Int = 0,
    val diseaseStats: Map<String, String> = emptyMap(),
    val diseaseInfoList: List<DiseaseInfo> = emptyList(),
    val selectedDisease: DiseaseInfo? = null,
    val showDetailDialog: Boolean = false,
    val currentImageIndex: Int = 0,
    val currentDiseaseTip: DiseaseTip? = null
)

private val diseaseTips = listOf(
    DiseaseTip("Roya común", "Aplica fungicidas a base de triazoles durante las primeras etapas de desarrollo para prevenir la infección."),
    DiseaseTip("Roya común", "Realiza rotación de cultivos con especies no hospederas por al menos 2 años para reducir el inóculo inicial."),
    DiseaseTip("Roya común", "Utiliza híbridos con genes de resistencia Rp específicos para tu región agrícola."),

    DiseaseTip("Mancha gris", "Implementa rotación de cultivos con especies no gramíneas por 1-2 años para reducir la presión de la enfermedad."),
    DiseaseTip("Mancha gris", "Aplica fungicidas que contengan estrobilurinas cuando aparezcan las primeras lesiones."),
    DiseaseTip("Mancha gris", "Retira y destruye los residuos de cultivos infectados para reducir la fuente de inóculo."),

    DiseaseTip("Tizón foliar del norte", "Utiliza híbridos con genes de resistencia Ht que sean efectivos contra las razas de patógenos locales."),
    DiseaseTip("Tizón foliar del norte", "Aplica fungicidas protectores al inicio de la temporada y sistémicos al detectar los primeros síntomas."),
    DiseaseTip("Tizón foliar del norte", "Implementa prácticas de labranza que aceleren la descomposición de residuos para reducir la fuente de inóculo."),

    DiseaseTip("Mancha foliar Phaeosphaeria", "Utiliza semillas tratadas con fungicidas para proteger las plántulas durante las etapas iniciales."),
    DiseaseTip("Mancha foliar Phaeosphaeria", "Mantén un programa de fertilización balanceada, ya que las deficiencias de potasio aumentan la susceptibilidad."),
    DiseaseTip("Mancha foliar Phaeosphaeria", "Aplica fungicidas a base de estrobilurinas + triazoles al detectar los primeros síntomas."),

    DiseaseTip("Roya del sur", "Realiza siembras tempranas para evitar que el cultivo coincida con las condiciones óptimas para el desarrollo de la enfermedad."),
    DiseaseTip("Roya del sur", "Utiliza fungicidas preventivos y monitorea constantemente, ya que esta enfermedad puede desarrollarse rápidamente."),
    DiseaseTip("Roya del sur", "Selecciona híbridos con resistencia genética específica para esta enfermedad, especialmente en zonas de alta presión.")
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository,
    private val diseaseInfoRepository: DiseaseInfoRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _randomTip = MutableStateFlow("")
    val randomTip: StateFlow<String> = _randomTip.asStateFlow()

    private val _currentDiseaseTip = MutableStateFlow<DiseaseTip?>(null)
    val currentDiseaseTip: StateFlow<DiseaseTip?> = _currentDiseaseTip.asStateFlow()

    private var tipRefreshJob: Job? = null
    private var diseaseTipRefreshJob: Job? = null


    init {
        loadHomeData()
        startTipRefreshTimer()
        startDiseaseTipRefreshTimer()
    }

    private fun startTipRefreshTimer() {
        tipRefreshJob?.cancel()

        tipRefreshJob = viewModelScope.launch {
            while(true) {
                loadRandomTip()
                delay(5000)
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            Log.d(TAG, "Loading home data...")

            analysisRepository.getAnalysisStatistics().collect { stats ->
                Log.d(TAG, "Received stats: total=${stats.totalLeaves}, avg=${stats.averageAccuracy}")
                Log.d(TAG, "Disease stats: ${stats.diseaseStats}")

                _uiState.update { currentState ->
                    currentState.copy(
                        totalLeaves = stats.totalLeaves,
                        averageAccuracy = stats.averageAccuracy,
                        diseaseStats = stats.diseaseStats
                    )
                }
            }
        }

        viewModelScope.launch {
            diseaseInfoRepository.getAllDiseaseInfo().collect { diseaseList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        diseaseInfoList = diseaseList
                    )
                }
            }
        }
    }

    private fun loadRandomTip() {
        viewModelScope.launch {
            diseaseInfoRepository.getRandomTip().collect { tip ->
                _randomTip.value = tip
            }
        }
    }

    fun refreshRandomTip() {
        loadRandomTip()
    }

    fun refreshData() {
        Log.d(TAG, "Refreshing data...")
        loadHomeData()
        loadRandomTip()
    }

    fun showDiseaseDetail(disease: DiseaseInfo) {
        _uiState.update { it.copy(
            selectedDisease = disease,
            showDetailDialog = true,
            currentImageIndex = 0
        )}
    }

    fun hideDetailDialog() {
        _uiState.update { it.copy(showDetailDialog = false) }
    }

    fun cycleToNextImage() {
        val disease = _uiState.value.selectedDisease ?: return
        val imageCount = disease.detailImageResIds.size.coerceAtLeast(1)
        val nextIndex = (_uiState.value.currentImageIndex + 1) % imageCount

        _uiState.update { it.copy(currentImageIndex = nextIndex) }
    }

    private fun startDiseaseTipRefreshTimer() {
        diseaseTipRefreshJob?.cancel()

        diseaseTipRefreshJob = viewModelScope.launch {
            while(true) {
                loadRandomDiseaseTip()
                delay(5000)
            }
        }
    }

    private fun loadRandomDiseaseTip() {
        val filteredTips = diseaseTips.filter { it.diseaseName != "Hoja saludable" }
        if (filteredTips.isNotEmpty()) {
            _currentDiseaseTip.value = filteredTips.random()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tipRefreshJob?.cancel()
        diseaseTipRefreshJob?.cancel()
    }
}