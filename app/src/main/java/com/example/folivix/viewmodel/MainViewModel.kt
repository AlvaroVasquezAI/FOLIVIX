package com.example.folivix.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.folivix.data.preferences.AppPreferences
import com.example.folivix.data.repository.UserRepository
import com.example.folivix.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val users: List<User> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val TAG = "MainViewModel"

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadUsers()

            appPreferences.lastUserId.collect { lastUserId ->
                if (lastUserId != null && _uiState.value.users.isNotEmpty()) {
                    val lastUser = _uiState.value.users.find { it.id == lastUserId }
                    if (lastUser != null) {
                        setCurrentUser(lastUser)
                    }
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.getAllUsers().collect { users ->
                    _uiState.update {
                        it.copy(users = users, isLoading = false)
                    }
                    Log.d(TAG, "Loaded ${users.size} users")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error loading users: ${e.message}", isLoading = false)
                }
                Log.e(TAG, "Error loading users", e)
            }
        }
    }

    fun createUser(name: String, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val newUser = userRepository.createUser(name, imageUri)
                Log.d(TAG, "User created: ${newUser.id}")

                setCurrentUser(newUser)

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error creating user: ${e.message}", isLoading = false)
                }
                Log.e(TAG, "Error creating user", e)
            }
        }
    }

    fun setCurrentUser(user: User) {
        _uiState.update { it.copy(currentUser = user) }
        userRepository.setCurrentUser(user.id)

        viewModelScope.launch {
            appPreferences.saveLastUserId(user.id)
        }
    }

    fun clearLastUser() {
        viewModelScope.launch {
            appPreferences.clearLastUserId()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.deleteUser(user.id)
                _uiState.update { it.copy(isLoading = false) }
                Log.d(TAG, "User deleted: ${user.id}")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error deleting user: ${e.message}", isLoading = false)
                }
                Log.e(TAG, "Error deleting user", e)
            }
        }
    }
}