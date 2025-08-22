package com.example.folivix.model

import android.net.Uri

data class User(
    val id: String,
    val name: String,
    val imageUri: Uri? = null
)