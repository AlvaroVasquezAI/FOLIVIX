package com.example.folivix.ui.screens.main

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.folivix.R
import com.example.folivix.ui.theme.FolivixBackground
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixDarkGreen
import com.example.folivix.ui.theme.FolivixDelete
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.ui.theme.FolivixSelectImage
import com.example.folivix.ui.theme.FolivixWhite
import com.example.folivix.viewmodel.IdentifyViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Locale

@Composable
fun IdentifyScreen(
    viewModel: IdentifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                viewModel.onImageCropped(resultUri)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR && result.data != null) {
            val error = UCrop.getError(result.data!!)
            error?.let {
                viewModel.setError("Error al recortar la imagen: ${it.message}")
            }
        }
    }

    val startCrop = remember {
        { sourceUri: Uri ->
            try {
                val destinationUri = Uri.fromFile(
                    File(
                        context.cacheDir,
                        "cropped_${System.currentTimeMillis()}.jpg"
                    )
                )

                val uCrop = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(1080, 1080)

                val options = UCrop.Options().apply {
                    setToolbarColor(FolivixGreen.hashCode())
                    setStatusBarColor(FolivixGreen.hashCode())
                    setToolbarWidgetColor(FolivixWhite.hashCode())
                    setActiveControlsWidgetColor(FolivixGreen.hashCode())
                }

                cropLauncher.launch(uCrop.withOptions(options).getIntent(context))
            } catch (e: Exception) {
                viewModel.setError("Error al iniciar el recorte: ${e.message}")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onImageCaptured()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.prepareImageCapture(context) { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            viewModel.setError("Se requiere permiso de cámara para tomar fotos")
        }
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = if (uiState.selectedImageUri == null)
                    CardDefaults.cardColors(containerColor = FolivixDarkGreen)
                else
                    CardDefaults.cardColors()
            ) {
                if (uiState.selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Selected leaf image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(
                                color = FolivixGreen,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.leaf_white),
                                contentDescription = "Leaf icon",
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No hay ninguna imagen aún",
                                color = FolivixWhite,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (uiState.result == null) {
                if (uiState.selectedImageUri != null && !uiState.isAnalyzing) {
                    Button(
                        onClick = {
                            uiState.selectedImageUri?.let { startCrop(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3C597),
                            contentColor = FolivixBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Editar imagen")

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit icon"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixSelectImage,
                            contentColor = FolivixBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Foto",
                            color = FolivixWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixSelectImage,
                            contentColor = FolivixBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Galería",
                            color = FolivixWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Button(
                    onClick = { viewModel.analyzeImage() },
                    enabled = uiState.selectedImageUri != null && !uiState.isAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FolivixDarkGreen,
                        contentColor = FolivixWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analizar")

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.saveSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = FolivixGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resultado guardado correctamente",
                            color = FolivixGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (uiState.result != null) {
                val result = uiState.result!!
                val isNotLeaf = result.diseaseType == "La imagen no se ve como una hoja"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNotLeaf) FolivixBlack else FolivixBackground
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isNotLeaf) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {


                                Text(
                                    text = result.diseaseType,
                                    color = FolivixWhite,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.warning),
                                    contentDescription = "Advertencia",
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )

                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .weight(0.65f)
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_leaf_result),
                                    contentDescription = "Tipo de enfermedad",
                                    tint = FolivixWhite,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = result.diseaseType,
                                    color = FolivixWhite,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.15f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Icono de precisión
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_acc_result),
                                    contentDescription = "Precisión",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_time_result),
                                    contentDescription = "Tiempo de inferencia",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.20f),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${(result.confidence * 100).toInt()}%",
                                    color = FolivixWhite,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = String.format(Locale.US, "%.2f s", result.processingTime.toDouble()),
                                    color = FolivixWhite,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveResult() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isNotLeaf,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixBackground,
                            contentColor = FolivixBlack,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Guardar resultado",
                            color = if (isNotLeaf) Color.DarkGray else FolivixWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixDelete,
                            contentColor = FolivixBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Resetear",
                            color = FolivixWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

