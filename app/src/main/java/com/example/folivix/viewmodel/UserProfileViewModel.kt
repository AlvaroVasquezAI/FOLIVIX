package com.example.folivix.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.folivix.data.preferences.AppPreferences
import com.example.folivix.data.repository.AnalysisRepository
import com.example.folivix.data.repository.UserRepository
import com.example.folivix.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val user: User? = null,
    val totalAnalyses: Int = 0,
    val averageAccuracy: Int = 0,
    val showEditNameDialog: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val operationSuccess: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val analysisRepository: AnalysisRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val tag = "UserProfileViewModel"

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadUserStatistics()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                userRepository.getCurrentUser().collect { user ->
                    Log.d(tag, "Current user loaded: ${user?.id}")
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading current user", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar el usuario: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadUserStatistics() {
        viewModelScope.launch {
            try {
                analysisRepository.getAnalysisStatistics().collect { stats ->
                    _uiState.update {
                        it.copy(
                            totalAnalyses = stats.totalLeaves,
                            averageAccuracy = stats.averageAccuracy
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al cargar estadísticas: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateUserImage(uri: Uri) {
        val userId = _uiState.value.user?.id ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                userRepository.updateUserImage(userId, uri)

                _uiState.update {
                    it.copy(
                        user = it.user?.copy(imageUri = uri),
                        isLoading = false,
                        operationSuccess = true
                    )
                }

                launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(operationSuccess = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al actualizar la imagen: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateUserName(name: String) {
        if (name.isBlank()) return

        val userId = _uiState.value.user?.id ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                userRepository.updateUserName(userId, name)

                _uiState.update {
                    it.copy(
                        user = it.user?.copy(name = name),
                        isLoading = false,
                        operationSuccess = true,
                        showEditNameDialog = false
                    )
                }

                launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(operationSuccess = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al actualizar el nombre: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val userId = _uiState.value.user?.id

                if (userId != null) {
                    try {
                        val lastUserId = appPreferences.lastUserId.first()
                        if (lastUserId == userId) {
                            appPreferences.clearLastUserId()
                            Log.d(tag, "Cleared last user ID preference for user: $userId")
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error checking or clearing last user preference", e)
                    }

                    userRepository.deleteUser(userId)
                    _uiState.update { it.copy(isLoading = false) }
                    Log.d(tag, "User deleted: $userId")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo eliminar el usuario: ID no disponible"
                        )
                    }
                    Log.e(tag, "Cannot delete user: No user ID available")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar el usuario: ${e.message}"
                    )
                }
                Log.e(tag, "Error deleting user", e)
            }
        }
    }

    fun showEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = true) }
    }

    fun hideEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = false) }
    }

    fun showDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
    }

    fun hideDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                appPreferences.clearLastUserId()
                Log.d(tag, "User logged out, last user preference cleared")
            } catch (e: Exception) {
                Log.e(tag, "Error during logout", e)
            }
        }
    }
}