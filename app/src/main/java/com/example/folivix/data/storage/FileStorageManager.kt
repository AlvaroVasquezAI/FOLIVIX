package com.example.folivix.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.folivix.model.AnalysisResult
import com.example.folivix.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "FileStorageManager"
        private const val MAX_USERS = 3
        private const val USER_FOLDER_PREFIX = "User"
        private const val USER_DATA_FOLDER = "UserData"
        private const val PREDICTIONS_FOLDER = "Predictions"
    }

    private val appDir: File by lazy {
        File(context.filesDir, "FolivixData").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun createUser(name: String, imageUri: Uri?): User = withContext(Dispatchers.IO) {
        val userFolder = findEmptyUserFolder() ?: createNewUserFolder()

        val userDataFolder = File(userFolder, USER_DATA_FOLDER).apply { mkdirs() }
        val userInfoFile = File(userDataFolder, "info.txt")
        userInfoFile.writeText(name)

        var savedImageUri: Uri? = null
        if (imageUri != null) {
            val profileImageFile = File(userDataFolder, "profile_image.jpg")
            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(profileImageFile).use { output ->
                        input.copyTo(output)
                    }
                }
                savedImageUri = profileImageFile.toUri()
                Log.d(TAG, "Saved profile image to ${profileImageFile.absolutePath}")
            } catch (e: IOException) {
                Log.e(TAG, "Error saving profile image", e)
            }
        }

        val predictionsFolder = File(userFolder, PREDICTIONS_FOLDER).apply { mkdirs() }

        Log.d(TAG, "Created user: ${userFolder.name}, name: $name, image: ${savedImageUri != null}")

        return@withContext User(
            id = userFolder.name,
            name = name,
            imageUri = savedImageUri
        )
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()

        val userFolders = appDir.listFiles { file ->
            file.isDirectory && file.name.startsWith(USER_FOLDER_PREFIX)
        } ?: emptyArray()

        Log.d(TAG, "Found ${userFolders.size} user folders")

        userFolders.forEach { userFolder ->
            val userDataFolder = File(userFolder, USER_DATA_FOLDER)
            if (userDataFolder.exists()) {
                val userInfoFile = File(userDataFolder, "info.txt")
                val profileImageFile = File(userDataFolder, "profile_image.jpg")

                if (userInfoFile.exists()) {
                    val name = userInfoFile.readText()
                    val imageUri = if (profileImageFile.exists()) {
                        profileImageFile.toUri()
                    } else {
                        null
                    }

                    users.add(User(
                        id = userFolder.name,
                        name = name,
                        imageUri = imageUri
                    ))

                    Log.d(TAG, "Loaded user: ${userFolder.name}, name: $name, has image: ${imageUri != null}")
                } else {
                    Log.w(TAG, "User info file not found for folder: ${userFolder.name}")
                }
            } else {
                Log.w(TAG, "User data folder not found for folder: ${userFolder.name}")
            }
        }

        Log.d(TAG, "Loaded ${users.size} users")
        return@withContext users
    }

    suspend fun updateUserName(userId: String, name: String) = withContext(Dispatchers.IO) {
        val userFolder = File(appDir, userId)
        if (userFolder.exists()) {
            val userDataFolder = File(userFolder, USER_DATA_FOLDER)
            val userInfoFile = File(userDataFolder, "info.txt")
            userInfoFile.writeText(name)
            Log.d(TAG, "Updated name for user $userId to $name")
        } else {
            Log.e(TAG, "User folder not found for ID: $userId")
            throw IOException("User not found")
        }
    }

    suspend fun updateUserImage(userId: String, imageUri: Uri) = withContext(Dispatchers.IO) {
        val userFolder = File(appDir, userId)
        if (userFolder.exists()) {
            val userDataFolder = File(userFolder, USER_DATA_FOLDER)
            val profileImageFile = File(userDataFolder, "profile_image.jpg")

            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(profileImageFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Updated image for user $userId")
            } catch (e: IOException) {
                Log.e(TAG, "Error updating profile image", e)
                throw e
            }
        } else {
            Log.e(TAG, "User folder not found for ID: $userId")
            throw IOException("User not found")
        }
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        val userFolder = File(appDir, userId)
        if (userFolder.exists()) {
            val success = userFolder.deleteRecursively()
            Log.d(TAG, "Deleted user $userId: $success")
            if (!success) {
                throw IOException("Failed to delete user folder")
            }
        } else {
            Log.e(TAG, "User folder not found for ID: $userId")
            throw IOException("User not found")
        }
    }

    suspend fun saveAnalysisResult(userId: String, result: AnalysisResult) = withContext(Dispatchers.IO) {
        val userFolder = File(appDir, userId)
        if (!userFolder.exists()) {
            Log.e(TAG, "User folder does not exist: $userId")
            throw IOException("User not found")
        }

        val predictionsFolder = File(userFolder, PREDICTIONS_FOLDER)
        if (!predictionsFolder.exists()) {
            predictionsFolder.mkdirs()
        }

        val classFolder = File(predictionsFolder, normalizeClassName(result.diseaseType))
        if (!classFolder.exists()) {
            classFolder.mkdirs()
        }

        val predictionFolder = File(classFolder, "Prediction_${result.id}")
        predictionFolder.mkdirs()

        val metadataFile = File(predictionFolder, "metadata.txt")
        val metadata = """
        ClassPredicted: ${result.diseaseType}
        Confidence: ${result.confidence}
        Date: ${formatDate(result.timestamp)}
        TimeInference: ${result.processingTime}
    """.trimIndent()
        metadataFile.writeText(metadata)

        result.imageUri?.let { uri ->
            val imageFile = File(predictionFolder, "image.jpg")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Saved analysis result image to ${imageFile.absolutePath}")
            } catch (e: IOException) {
                Log.e(TAG, "Error saving prediction image", e)
            }
        }

        Log.d(TAG, "Saved analysis result for user $userId, disease: ${result.diseaseType}")
    }

    suspend fun getAnalysisResults(userId: String): List<AnalysisResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AnalysisResult>()
        val userFolder = File(appDir, userId)
        if (!userFolder.exists()) {
            Log.e(TAG, "User folder does not exist when getting results: $userId")
            return@withContext results
        }

        val predictionsFolder = File(userFolder, PREDICTIONS_FOLDER)
        if (!predictionsFolder.exists()) {
            Log.e(TAG, "Predictions folder does not exist for user: $userId")
            return@withContext results
        }

        predictionsFolder.listFiles { file -> file.isDirectory }?.forEach { classFolder ->
            classFolder.listFiles { file -> file.isDirectory && file.name.startsWith("Prediction_") }?.forEach { predictionFolder ->
                val metadataFile = File(predictionFolder, "metadata.txt")
                val imageFile = File(predictionFolder, "image.jpg")

                if (metadataFile.exists()) {
                    val metadata = metadataFile.readText().lines().associate { line ->
                        val parts = line.split(":", limit = 2)
                        if (parts.size == 2) {
                            parts[0].trim() to parts[1].trim()
                        } else {
                            "" to ""
                        }
                    }

                    val id = predictionFolder.name.removePrefix("Prediction_")
                    val diseaseType = metadata["ClassPredicted"] ?: ""
                    val confidence = metadata["Confidence"]?.toDoubleOrNull() ?: 0.0
                    val timestamp = parseDate(metadata["Date"] ?: "")
                    val processingTime = metadata["TimeInference"] ?: "0.0"
                    val imageUri = if (imageFile.exists()) imageFile.toUri() else null

                    results.add(AnalysisResult(
                        id = id,
                        imageUri = imageUri,
                        timestamp = timestamp,
                        diseaseType = diseaseType,
                        confidence = confidence,
                        processingTime = processingTime
                    ))
                }
            }
        }

        val sortedResults = results.sortedByDescending { it.timestamp }
        Log.d(TAG, "Loaded ${sortedResults.size} analysis results for user $userId")
        return@withContext sortedResults
    }

    private fun findEmptyUserFolder(): File? {
        for (i in 1..MAX_USERS) {
            val folder = File(appDir, "$USER_FOLDER_PREFIX$i")
            if (!folder.exists()) {
                folder.mkdirs()
                Log.d(TAG, "Created new user folder: ${folder.name}")
                return folder
            } else if (folder.listFiles()?.isEmpty() == true) {
                Log.d(TAG, "Found empty user folder: ${folder.name}")
                return folder
            }
        }
        Log.d(TAG, "No empty user folder found")
        return null
    }

    private fun createNewUserFolder(): File {
        val existingFolders = appDir.listFiles { file ->
            file.isDirectory && file.name.startsWith(USER_FOLDER_PREFIX)
        } ?: emptyArray()

        if (existingFolders.size >= MAX_USERS) {
            Log.e(TAG, "Maximum number of users reached")
            throw IOException("Maximum number of users reached")
        }

        val usedNumbers = existingFolders.mapNotNull { folder ->
            folder.name.removePrefix(USER_FOLDER_PREFIX).toIntOrNull()
        }.toSet()

        for (i in 1..MAX_USERS) {
            if (i !in usedNumbers) {
                val newFolder = File(appDir, "$USER_FOLDER_PREFIX$i")
                newFolder.mkdirs()
                Log.d(TAG, "Created new user folder with available number: ${newFolder.name}")
                return newFolder
            }
        }

        Log.e(TAG, "Could not create user folder")
        throw IOException("Could not create user folder")
    }

    private fun normalizeClassName(className: String): String {
        return when (className) {
            "Roya común" -> "Roya_comun"
            "Mancha gris" -> "Mancha_gris"
            "Tizón foliar del norte" -> "Tizon_foliar_del_norte"
            "Mancha foliar Phaeosphaeria" -> "Mancha_foliar_Phaeosphaeria"
            "Roya del sur" -> "Roya_del_sur"
            "Saludable" -> "Saludable"
            else -> className.replace(" ", "_")
        }
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    suspend fun deleteAnalysisResult(userId: String, resultId: String) = withContext(Dispatchers.IO) {
        val userFolder = File(appDir, userId)
        if (!userFolder.exists()) {
            Log.e(TAG, "User folder does not exist: $userId")
            throw IOException("User not found")
        }

        val predictionsFolder = File(userFolder, PREDICTIONS_FOLDER)
        if (!predictionsFolder.exists()) {
            Log.e(TAG, "Predictions folder does not exist for user: $userId")
            throw IOException("Predictions folder not found")
        }

        var found = false
        predictionsFolder.listFiles { file -> file.isDirectory }?.forEach { classFolder ->
            val predictionFolder = File(classFolder, "Prediction_$resultId")
            if (predictionFolder.exists()) {
                val success = predictionFolder.deleteRecursively()
                if (success) {
                    Log.d(TAG, "Deleted analysis result $resultId successfully")
                    found = true
                } else {
                    Log.e(TAG, "Failed to delete analysis result $resultId")
                    throw IOException("Failed to delete analysis result")
                }
            }
        }

        if (!found) {
            Log.e(TAG, "Analysis result $resultId not found for user $userId")
            throw IOException("Analysis result not found")
        }
    }
}