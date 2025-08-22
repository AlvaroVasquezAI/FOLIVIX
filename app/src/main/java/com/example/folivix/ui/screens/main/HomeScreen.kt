package com.example.folivix.ui.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.folivix.R
import com.example.folivix.viewmodel.HomeViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.folivix.model.DiseaseInfo
import com.example.folivix.ui.theme.FolivixGreen
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val randomTip by viewModel.randomTip.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        FolivixCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Sabías que?",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_light),
                        contentDescription = "NewThing",
                        tint = FolivixGreen,
                        modifier = Modifier.size(40.dp)
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = randomTip,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { tip ->
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        FolivixCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Estadísticas",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                val pieChartData = preparePieChartData(uiState.diseaseStats)

                if (pieChartData.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(
                            data = pieChartData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "${uiState.averageAccuracy}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        pieChartData.forEach { entry ->
                            PieChartSimpleLegendItem(
                                color = entry.color,
                                name = entry.name
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No hay datos de análisis disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }

            }
        }

        val rows = uiState.diseaseInfoList.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { disease ->
                    Box(modifier = Modifier.weight(1f)) {
                        DiseaseCard(
                            disease = disease,
                            onClick = { viewModel.showDiseaseDetail(disease) }
                        )
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (uiState.showDetailDialog && uiState.selectedDisease != null) {
            DiseaseDetailDialog(
                disease = uiState.selectedDisease!!,
                currentImageIndex = uiState.currentImageIndex,
                onDismiss = { viewModel.hideDetailDialog() },
                onNextImage = { viewModel.cycleToNextImage() }
            )
        }

        FolivixCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentTip = viewModel.currentDiseaseTip.collectAsState().value

                    if (currentTip != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_advice),
                            contentDescription = "Consejo",
                            tint = FolivixGreen,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentTip.diseaseName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AnimatedContent(
                            targetState = currentTip,
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            }
                        ) { tip ->
                            Text(
                                text = tip.tip,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Text(
                            text = "Cargando consejos...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun preparePieChartData(diseaseStats: Map<String, String>): List<PieChartEntry> {
    val colors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFB67616),
        Color(0xFFBA68C8),
        Color(0xFF4DB6AC)
    )

    val result = mutableListOf<PieChartEntry>()

    diseaseStats.entries.forEachIndexed { index, entry ->
        val diseaseName = entry.key

        val parts = entry.value.split("-")
        val count = parts[0].toIntOrNull() ?: 0
        val accuracy = parts[1].removeSuffix("%").toIntOrNull() ?: 0

        if (count > 0) {
            result.add(
                PieChartEntry(
                    name = diseaseName,
                    count = count,
                    accuracy = accuracy,
                    color = colors[index % colors.size]
                )
            )
        }
    }

    return result
}

data class PieChartEntry(
    val name: String,
    val count: Int,
    val accuracy: Int,
    val color: Color
)

@Composable
fun PieChart(
    data: List<PieChartEntry>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.count }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2 * 0.8f
        val center = Offset(canvasWidth / 2, canvasHeight / 2)

        var startAngle = -90f

        data.forEach { entry ->
            val sweepAngle = 360f * entry.count / total
            val middleAngle = startAngle + sweepAngle / 2

            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 2f)
            )

            val labelRadius = radius * 0.65f
            val radians = middleAngle * PI.toFloat() / 180f
            val labelX = center.x + cos(radians) * labelRadius
            val labelY = center.y + sin(radians) * labelRadius

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }

                drawText(
                    "${entry.count}",
                    labelX,
                    labelY,
                    paint
                )

                drawText(
                    "${entry.accuracy}%",
                    labelX,
                    labelY + 35f,
                    paint
                )
            }

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = radius * 0.3f,
            center = center
        )

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            drawText(
                "$total",
                center.x,
                center.y + 15f,
                paint
            )
        }
    }
}

@Composable
fun PieChartSimpleLegendItem(
    color: Color,
    name: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FolivixCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        content()
    }
}

@Composable
fun DiseaseCard(
    disease: DiseaseInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = disease.imageResId),
                    contentDescription = disease.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = disease.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DiseaseDetailDialog(
    disease: DiseaseInfo,
    currentImageIndex: Int,
    onDismiss: () -> Unit,
    onNextImage: () -> Unit
) {
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
                .padding(16.dp)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = disease.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    val imageResId = if (disease.detailImageResIds.isNotEmpty()) {
                        disease.detailImageResIds[currentImageIndex]
                    } else {
                        disease.imageResId
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNextImage),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = disease.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (disease.detailImageResIds.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "(${currentImageIndex + 1}/${disease.detailImageResIds.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = FolivixGreen
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    val sections = parseDetailedDescription(disease.detailedDescription)

                    sections.forEach { (title, content) ->
                        if (title.isNotEmpty()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FolivixGreen,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        } else {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entendido")
                    }
                }
            }
        }
    }
}

private fun parseDetailedDescription(description: String): List<Pair<String, String>> {
    val sectionTitles = listOf(
        "Patógeno",
        "Síntomas",
        "Características",
        "Apariencia",
        "Impacto",
        "Control",
        "Prevención",
        "Importancia"
    )

    if (!sectionTitles.any { description.contains("$it:") }) {
        return listOf("" to description)
    }

    val sections = mutableListOf<Pair<String, String>>()
    var currentText = description
    var introText = ""

    val firstSectionIndex = sectionTitles.mapNotNull {
        val index = description.indexOf("$it:")
        if (index >= 0) index else null
    }.minOrNull()

    if (firstSectionIndex != null && firstSectionIndex > 0) {
        introText = description.substring(0, firstSectionIndex).trim()
        currentText = description.substring(firstSectionIndex)
    }

    if (introText.isNotEmpty()) {
        sections.add("" to introText)
    }

    sectionTitles.forEach { sectionTitle ->
        val startIndex = currentText.indexOf("$sectionTitle:")
        if (startIndex >= 0) {
            var endIndex = currentText.length

            for (nextTitle in sectionTitles) {
                val nextIndex = currentText.indexOf("$nextTitle:", startIndex + sectionTitle.length + 1)
                if (nextIndex > startIndex && nextIndex < endIndex) {
                    endIndex = nextIndex
                }
            }

            val sectionContent = currentText.substring(
                startIndex + sectionTitle.length + 1,
                endIndex
            ).trim()

            sections.add(sectionTitle to sectionContent)
        }
    }

    return sections
}