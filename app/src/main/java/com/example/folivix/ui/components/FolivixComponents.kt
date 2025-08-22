package com.example.folivix.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixGray
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.ui.theme.FolivixWhite

@Suppress("FunctionName")
@Composable
fun FolivixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSecondary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) FolivixWhite else FolivixGreen,
            contentColor = if (isSecondary) FolivixGreen else FolivixWhite,
            disabledContainerColor = FolivixGray,
            disabledContentColor = FolivixWhite
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSecondary) BorderStroke(1.dp, FolivixGreen) else null
    ) {
        Text(text = text)
    }
}

@Suppress("FunctionName")
@Composable
fun FolivixCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FolivixWhite
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FolivixBlack),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        content()
    }
}

@Suppress("FunctionName")
@Composable
fun FolivixUserAvatar(
    imageUri: Uri?,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(FolivixGreen),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "User avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default avatar",
                tint = FolivixWhite,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}