package com.example.folivix.viewmodel

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
import javax.inject.Inject

data class HistoryUiState(
    val analysisResults: List<AnalysisResult> = emptyList(),
    val activeFilters: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisResults()
    }

    private fun loadAnalysisResults() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                analysisRepository.getAnalysisResults().collect { results ->
                    _uiState.update {
                        it.copy(
                            analysisResults = results,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar los resultados: ${e.message}"
                    )
                }
            }
        }
    }

    fun applyFilter(filter: String) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.activeFilters.contains(filter)) {
                currentState.activeFilters - filter
            } else {
                currentState.activeFilters + filter
            }

            currentState.copy(activeFilters = newFilters)
        }
    }

    fun deleteAnalysisResult(result: AnalysisResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                analysisRepository.deleteAnalysisResult(result.id)

                _uiState.update { currentState ->
                    val updatedResults = currentState.analysisResults.filter { it.id != result.id }
                    currentState.copy(
                        analysisResults = updatedResults,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar el análisis: ${e.message}"
                    )
                }
            }
        }
    }

    fun setFilters(filters: List<String>) {
        _uiState.update { it.copy(activeFilters = filters) }
    }

    fun addFilter(filter: String) {
        _uiState.update {
            val currentFilters = it.activeFilters.toMutableList()
            if (!currentFilters.contains(filter)) {
                currentFilters.add(filter)
            }
            it.copy(activeFilters = currentFilters)
        }
    }

    fun removeFilter(filter: String) {
        _uiState.update {
            val currentFilters = it.activeFilters.toMutableList()
            currentFilters.remove(filter)
            it.copy(activeFilters = currentFilters)
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(activeFilters = emptyList()) }
    }

}