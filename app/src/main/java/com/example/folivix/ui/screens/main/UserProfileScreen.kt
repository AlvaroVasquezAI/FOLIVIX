package com.example.folivix.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import com.example.folivix.R
import com.example.folivix.ui.components.FolivixUserAvatar
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixDelete
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.ui.theme.FolivixWhite
import com.example.folivix.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onNavigateToProfiles: () -> Unit,
    onDeleteUser: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateUserImage(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FolivixGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver atrás",
                        tint = FolivixWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.folivix_logo),
                    contentDescription = "FOLIVIX Logo",
                    modifier = Modifier.width(150.dp),
                    colorFilter = ColorFilter.tint(FolivixWhite)
                )

                Spacer(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.size(52.dp))
            }

            FolivixUserAvatar(
                imageUri = uiState.user?.imageUri,
                size = 200.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextButton(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = FolivixWhite
                )
            ) {
                Text("Editar imagen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.user?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FolivixWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = { viewModel.showEditNameDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = FolivixWhite
                    )
                ) {
                    Text("Editar nombre")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CircularStat(
                    icon = R.drawable.leaf,
                    value = "${uiState.totalAnalyses}",
                    modifier = Modifier.weight(1f)
                )

                CircularStat(
                    icon = R.drawable.ic_acc_result,
                    value = "${uiState.averageAccuracy}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.showDeleteConfirmationDialog() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FolivixDelete,
                    contentColor = FolivixWhite
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Eliminar usuario")
            }

            Button(
                onClick = {
                    viewModel.logout()
                    onNavigateToProfiles()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FolivixWhite,
                    contentColor = FolivixBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }

    if (uiState.showEditNameDialog) {
        var newName by remember { mutableStateOf(uiState.user?.name ?: "") }

        AlertDialog(
            onDismissRequest = { viewModel.hideEditNameDialog() },
            title = { Text("Editar nombre") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserName(newName)
                        viewModel.hideEditNameDialog()
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideEditNameDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmationDialog() },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Estás seguro de que deseas eliminar este perfil? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser()
                        viewModel.hideDeleteConfirmationDialog()
                        onDeleteUser()
                    }
                ) {
                    Text("Eliminar", color = FolivixDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmationDialog() }) {
                    Text("Cancelar", color = FolivixBlack)
                }
            }
        )
    }
}

@Composable
fun CircularStat(
    icon: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = FolivixWhite,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Image",
                    colorFilter = ColorFilter.tint(FolivixGreen),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FolivixGreen,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}