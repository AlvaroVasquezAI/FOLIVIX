package com.example.folivix.data.repository

import android.net.Uri
import com.example.folivix.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun createUser(name: String, imageUri: Uri?): User
    suspend fun updateUserName(userId: String, name: String)
    suspend fun updateUserImage(userId: String, imageUri: Uri)
    suspend fun deleteUser(userId: String)
    fun getAllUsers(): Flow<List<User>>

    fun setCurrentUser(userId: String)
}