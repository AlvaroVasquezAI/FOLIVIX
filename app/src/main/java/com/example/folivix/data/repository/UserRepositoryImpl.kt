package com.example.folivix.data.repository

import android.net.Uri
import android.util.Log
import com.example.folivix.data.storage.FileStorageManager
import com.example.folivix.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val fileStorageManager: FileStorageManager
) : UserRepository {

    private val tag = "UserRepositoryImpl"

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val currentUserId = MutableStateFlow<String?>(null)
    private val users = MutableStateFlow<List<User>>(emptyList())

    init {
        repositoryScope.launch {
            try {
                val loadedUsers = fileStorageManager.getAllUsers()
                users.value = loadedUsers
                Log.d(tag, "Loaded ${loadedUsers.size} users")
            } catch (e: Exception) {
                Log.e(tag, "Error loading users", e)
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return users.combine(currentUserId) { userList, userId ->
            if (userId != null) {
                val user = userList.find { it.id == userId }
                Log.d(tag, "Getting current user: ${user?.id}")
                user
            } else {
                Log.d(tag, "No current user set")
                null
            }
        }
    }

    override suspend fun createUser(name: String, imageUri: Uri?): User {
        try {
            Log.d(tag, "Creating new user: $name")
            val newUser = fileStorageManager.createUser(name, imageUri)

            val updatedUsers = users.value.toMutableList()
            updatedUsers.add(newUser)
            users.value = updatedUsers

            currentUserId.value = newUser.id
            Log.d(tag, "New user created and set as current: ${newUser.id}")

            return newUser
        } catch (e: Exception) {
            Log.e(tag, "Error creating user", e)
            throw e
        }
    }

    override suspend fun updateUserName(userId: String, name: String) {
        try {
            Log.d(tag, "Updating name for user $userId to $name")
            fileStorageManager.updateUserName(userId, name)

            val updatedUsers = users.value.map { user ->
                if (user.id == userId) {
                    user.copy(name = name)
                } else {
                    user
                }
            }
            users.value = updatedUsers
            Log.d(tag, "User name updated successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error updating user name", e)
            throw e
        }
    }

    override suspend fun updateUserImage(userId: String, imageUri: Uri) {
        try {
            Log.d(tag, "Updating image for user $userId")
            fileStorageManager.updateUserImage(userId, imageUri)

            val updatedUsers = users.value.map { user ->
                if (user.id == userId) {
                    user.copy(imageUri = imageUri)
                } else {
                    user
                }
            }
            users.value = updatedUsers
            Log.d(tag, "User image updated successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error updating user image", e)
            throw e
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            Log.d(tag, "Deleting user $userId")
            fileStorageManager.deleteUser(userId)

            val updatedUsers = users.value.filter { it.id != userId }
            users.value = updatedUsers

            if (currentUserId.value == userId) {
                currentUserId.value = updatedUsers.firstOrNull()?.id
                Log.d(tag, "Current user was deleted, new current user: ${currentUserId.value}")
            }
            Log.d(tag, "User deleted successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error deleting user", e)
            throw e
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return users
    }

    override fun setCurrentUser(userId: String) {
        currentUserId.value = userId
        Log.d(tag, "Current user set to: $userId")
    }
}