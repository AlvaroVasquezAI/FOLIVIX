package com.example.folivix.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.folivix.R
import com.example.folivix.model.User
import com.example.folivix.ui.components.FolivixButton
import com.example.folivix.ui.components.FolivixUserAvatar
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixDelete

@Composable
fun ManageProfilesScreen(
    profiles: List<User>,
    onDeleteProfile: (User) -> Unit,
    onEditProfile: (User) -> Unit,
    onBack: () -> Unit
) {
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.folivix_logo),
            contentDescription = "FOLIVIX Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .width(150.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 120.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(profiles) { user ->
                        ProfileItem(
                            user = user,
                            onEditClick = { onEditProfile(user) },
                            onDeleteClick = { userToDelete = user }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp)
        ) {
            FolivixButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar perfil") },
            text = { Text("¿Estás seguro de que deseas eliminar el perfil de ${user.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(user)
                        userToDelete = null
                    }
                ) {
                    Text("Eliminar", color = FolivixDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar", color = FolivixBlack)
                }
            }
        )
    }
}

@Composable
fun ProfileItem(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FolivixUserAvatar(
                imageUri = user.imageUri,
                size = 60.dp
            )

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar perfil",
                        tint = FolivixBlack
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar perfil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}