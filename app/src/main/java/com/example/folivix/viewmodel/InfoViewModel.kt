package com.example.folivix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.folivix.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InfoUiState(
    val serverIp: String = "",
    val showIpDialog: Boolean = false,
    val ipInputError: String? = null
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        loadServerIp()
    }

    private fun loadServerIp() {
        viewModelScope.launch {
            appPreferences.serverIp.collect { ip ->
                _uiState.update { it.copy(serverIp = ip) }
            }
        }
    }

    fun showIpDialog() {
        _uiState.update { it.copy(showIpDialog = true, ipInputError = null) }
    }

    fun hideIpDialog() {
        _uiState.update { it.copy(showIpDialog = false) }
    }

    fun updateServerIp(ip: String) {
        // Validar la IP
        if (!isValidIp(ip)) {
            _uiState.update { it.copy(ipInputError = "IP inválida. Formato: xxx.xxx.xxx.xxx") }
            return
        }

        viewModelScope.launch {
            appPreferences.saveServerIp(ip)
            _uiState.update { it.copy(showIpDialog = false, ipInputError = null) }
        }
    }

    private fun isValidIp(ip: String): Boolean {
        val ipPattern = """^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
        return ipPattern.matches(ip)
    }
}