package com.example.folivix.ui.screens.main

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.folivix.R
import com.example.folivix.model.AnalysisResult
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.graphics.Brush
import java.util.Calendar

@Composable
fun BackgroundScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var resultToDelete by remember { mutableStateOf<AnalysisResult?>(null) }

    var showFiltersDialog by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    var previousFirstVisibleItemIndex by remember { mutableStateOf(0) }
    var previousFirstVisibleItemScrollOffset by remember { mutableStateOf(0) }

    val isScrollingUp = remember {
        derivedStateOf {
            val currentIndex = lazyListState.firstVisibleItemIndex
            val currentOffset = lazyListState.firstVisibleItemScrollOffset

            val goingUp = if (currentIndex < previousFirstVisibleItemIndex) {
                true
            } else if (currentIndex > previousFirstVisibleItemIndex) {
                false
            } else {
                currentOffset <= previousFirstVisibleItemScrollOffset
            }

            previousFirstVisibleItemIndex = currentIndex
            previousFirstVisibleItemScrollOffset = currentOffset

            goingUp
        }
    }

    val shouldShowFilters by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset == 0 ||
                    isScrollingUp.value
        }
    }

    val filtersHeight by animateDpAsState(
        targetValue = if (shouldShowFilters)
            if (uiState.activeFilters.isEmpty()) 80.dp else 132.dp
        else 0.dp,
        label = "filters height"
    )

    val filteredResults = if (uiState.activeFilters.isEmpty()) {
        uiState.analysisResults
    } else {
        val availableDiseases = uiState.analysisResults.map { it.diseaseType }.distinct()

        val diseaseFilters = uiState.activeFilters.filter { filter ->
            availableDiseases.contains(filter)
        }

        val dateFilters = uiState.activeFilters.filter { filter ->
            listOf("Este día", "Este mes", "Este año").contains(filter)
        }

        val precisionFilters = uiState.activeFilters.filter { filter ->
            listOf(
                "Alta (>90%)",
                "Media (70%-90%)",
                "Baja (<70%)"
            ).contains(filter)
        }

        uiState.analysisResults.filter { result ->
            val passesDisease = diseaseFilters.isEmpty() ||
                    diseaseFilters.contains(result.diseaseType)

            val passesDate = dateFilters.isEmpty() || dateFilters.any { filter ->
                when (filter) {
                    "Este día" -> {
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        result.timestamp >= today
                    }
                    "Este mes" -> {
                        val firstDayOfMonth = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        result.timestamp >= firstDayOfMonth
                    }
                    "Este año" -> {
                        val firstDayOfYear = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        result.timestamp >= firstDayOfYear
                    }
                    else -> false
                }
            }

            val passesPrecision = precisionFilters.isEmpty() || precisionFilters.any { filter ->
                when (filter) {
                    "Alta (>90%)" -> result.confidence > 0.9
                    "Media (70%-90%)" -> result.confidence in 0.7..0.9
                    "Baja (<70%)" -> result.confidence < 0.7
                    else -> false
                }
            }

            passesDisease && passesDate && passesPrecision
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = filtersHeight + 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredResults) { result ->
                InstagramStyleResultCard(
                    result = result,
                    onOptionsClick = {
                        selectedResult = result
                        showOptionsDialog = true
                    },
                    onSaveImage = {
                        if (result.imageUri != null) {
                            coroutineScope.launch {
                                try {
                                    saveImageToGallery(context, result.imageUri, result.diseaseType)
                                    Toast.makeText(context, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al guardar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "No hay imagen para guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (!uiState.isLoading && uiState.error == null && filteredResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.activeFilters.isEmpty())
                                "No hay análisis guardados"
                            else
                                "No hay resultados que coincidan con los filtros seleccionados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = shouldShowFilters,
            enter = slideInVertically() + expandVertically(),
            exit = slideOutVertically() + shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.001f),
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = if (uiState.activeFilters.isEmpty()) 8.dp else 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { showFiltersDialog = true },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Historial de hojas analizadas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FolivixBlack
                            )

                            Image(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Leaf icon",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (uiState.activeFilters.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = true,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.activeFilters) { filter ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = FolivixGreen),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.clickable { viewModel.removeFilter(filter) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = filter,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar filtro",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FolivixGreen)
            }
        }
    }

    if (showOptionsDialog && selectedResult != null) {
        Dialog(
            onDismissRequest = {
                showOptionsDialog = false
                selectedResult = null
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = FolivixGreen.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedResult?.diseaseType ?: "Enfermedad desconocida",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FolivixGreen
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Precisión
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_acc_result),
                                            contentDescription = "Precisión",
                                            tint = FolivixGreen,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${selectedResult?.confidence?.times(100)?.toInt() ?: 0}%",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(50.dp)
                                            .width(1.dp)
                                            .background(Color.LightGray)
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_history),
                                            contentDescription = "Fecha",
                                            tint = FolivixGreen,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = selectedResult?.let { formatDate(it.timestamp) } ?: "Desconocida",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar análisis",
                        tint = Color.Red,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                resultToDelete = selectedResult
                                showDeleteConfirmation = true
                                showOptionsDialog = false
                                selectedResult = null
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .align(Alignment.CenterHorizontally)
                            .size(30.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = {
                                showOptionsDialog = false
                                selectedResult = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = FolivixGreen
                            )
                        ) {
                            Text("Volver", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showFiltersDialog) {
        FiltersDialog(
            onDismiss = { showFiltersDialog = false },
            onApplyFilters = { filters ->
                viewModel.setFilters(filters)
                showFiltersDialog = false
            },
            activeFilters = uiState.activeFilters,
            availableDiseases = uiState.analysisResults.map { it.diseaseType }.distinct()
        )
    }

    if (showDeleteConfirmation && resultToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                resultToDelete = null
            },
            title = { Text("Eliminar registro") },
            text = { Text("¿Estás seguro de que deseas eliminar este análisis? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultToDelete?.let { result ->
                            viewModel.deleteAnalysisResult(result)
                        }
                        showDeleteConfirmation = false
                        resultToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    resultToDelete = null
                }) {
                    Text("Cancelar", color = FolivixBlack)
                }
            }
        )
    }
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    val firstVisibleItemIndex = this.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = this.firstVisibleItemScrollOffset

    val recentIndex = remember { mutableStateOf(firstVisibleItemIndex) }
    val recentOffset = remember { mutableStateOf(firstVisibleItemScrollOffset) }

    val isScrollingUp = if (firstVisibleItemIndex < recentIndex.value) {
        true
    } else if (firstVisibleItemIndex > recentIndex.value) {
        false
    } else {
        firstVisibleItemScrollOffset <= recentOffset.value
    }

    recentIndex.value = firstVisibleItemIndex
    recentOffset.value = firstVisibleItemScrollOffset

    return isScrollingUp
}

@Composable
fun FiltersDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (List<String>) -> Unit,
    activeFilters: List<String>,
    availableDiseases: List<String>
) {
    val selectedFilters = remember { mutableStateListOf<String>().apply { addAll(activeFilters) } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tipo de enfermedad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    availableDiseases.forEach { disease ->
                        FilterOption(
                            text = disease,
                            isSelected = selectedFilters.contains(disease),
                            onToggle = {
                                if (selectedFilters.contains(disease)) {
                                    selectedFilters.remove(disease)
                                } else {
                                    selectedFilters.add(disease)
                                }
                            }
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val dateFilters = listOf("Este día", "Este mes", "Este año")
                    dateFilters.forEach { dateFilter ->
                        FilterOption(
                            text = dateFilter,
                            isSelected = selectedFilters.contains(dateFilter),
                            onToggle = {
                                if (selectedFilters.contains(dateFilter)) {
                                    selectedFilters.remove(dateFilter)
                                } else {
                                    selectedFilters.removeAll { it in dateFilters }
                                    selectedFilters.add(dateFilter)
                                }
                            }
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    Text(
                        text = "Precisión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val precisionFilters = listOf(
                        "Alta (>90%)",
                        "Media (70%-90%)",
                        "Baja (<70%)"
                    )

                    precisionFilters.forEach { precisionFilter ->
                        FilterOption(
                            text = precisionFilter,
                            isSelected = selectedFilters.contains(precisionFilter),
                            onToggle = {
                                if (selectedFilters.contains(precisionFilter)) {
                                    selectedFilters.remove(precisionFilter)
                                } else {
                                    selectedFilters.removeAll { it in precisionFilters }
                                    selectedFilters.add(precisionFilter)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            selectedFilters.clear()
                            onApplyFilters(emptyList())
                        }
                    ) {
                        Text("Limpiar filtros")
                    }

                    Button(
                        onClick = { onApplyFilters(selectedFilters.toList()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixGreen
                        )
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = FolivixGreen,
                uncheckedColor = FolivixBlack
            )
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}


private suspend fun saveImageToGallery(context: Context, imageUri: Uri, diseaseName: String) {
    withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(imageUri)
                .build()

            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    ?: throw Exception("No se pudo cargar la imagen")

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "FOLIVIX_${diseaseName.replace(" ", "_")}_$timestamp.jpg"

                var outputStream: OutputStream? = null
                var imageUri: Uri? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FOLIVIX")
                    }

                    context.contentResolver.also { resolver ->
                        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        outputStream = imageUri?.let { resolver.openOutputStream(it) }
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/FOLIVIX"
                    val dir = File(imagesDir)
                    if (!dir.exists()) dir.mkdirs()
                    val image = File(dir, fileName)
                    outputStream = FileOutputStream(image)
                    imageUri = Uri.fromFile(image)
                }

                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                } ?: throw Exception("No se pudo crear el archivo de salida")
            } else {
                throw Exception("No se pudo cargar la imagen")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

@Composable
fun InstagramStyleResultCard(
    result: AnalysisResult,
    onOptionsClick: () -> Unit,
    onSaveImage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = FolivixBlack
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(FolivixGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.leaf_black),
                            contentDescription = "Leaf",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = result.diseaseType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FolivixGreen
                        )

                        Text(
                            text = formatDate(result.timestamp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(onClick = onOptionsClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = FolivixBlack
                    )
                }
            }

            result.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen del análisis",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(FolivixGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.leaf),
                    contentDescription = "No hay imagen",
                    tint = FolivixGreen,
                    modifier = Modifier.size(64.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_acc_result),
                            contentDescription = "Precisión",
                            tint = FolivixGreen,
                            modifier = Modifier.size(25.dp)
                        )

                        Text(
                            text = "${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_time_result),
                            contentDescription = "Tiempo",
                            tint = FolivixGreen,
                            modifier = Modifier.size(23.dp)
                        )

                        Text(
                            text = formatProcessingTime(result.processingTime),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "Hora",
                            tint = FolivixGreen,
                            modifier = Modifier.size(25.dp)
                        )

                        Text(
                            text = formatTime(result.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSaveImage,
                    modifier = Modifier
                        .background(
                            color = Color.White
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = "Guardar imagen",
                        tint = FolivixGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatDateWithTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatProcessingTime(time: String): String {
    return try {
        val timeValue = time.toDouble()
        String.format(Locale.US, "%.4fs", timeValue)
    } catch (e: Exception) {
        "${time}s"
    }
}