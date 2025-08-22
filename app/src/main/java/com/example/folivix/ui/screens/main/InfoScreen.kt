package com.example.folivix.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.folivix.R
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.ui.theme.FolivixWhite
import com.example.folivix.viewmodel.InfoViewModel

@Composable
fun InfoScreen(
    onNavigateToManual: () -> Unit,
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit,
    viewModel: InfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FolivixGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

                Spacer(modifier = Modifier.size(48.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FolivixWhite.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, FolivixWhite.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¿FOLIVIX?",
                        style = MaterialTheme.typography.titleMedium,
                        color = FolivixWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "FOLIVIX es una aplicación para identificar enfermedades foliares en plantas de maíz (futuramente en otras plantas también), la cual tiene la capacidad de identificar hasta 5 tipos de enfermedades, y además identificar si la hoja es sana. Los tipos de enfermedades son: Roya común, Mancha gris, Tizón foliar del norte, Mancha foliar Phaeosphaeria y Roya del sur.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FolivixWhite,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FolivixWhite.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, FolivixWhite.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Impacto de la aplicación",
                        style = MaterialTheme.typography.titleMedium,
                        color = FolivixWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "FOLIVIX es un cambio en la forma en la que se tiene un control en los cultivos de maíz y además en la forma en la que se previenen enfermedades con un bajo costo, un tiempo récord y además menos personal requerido.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FolivixWhite,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FolivixWhite.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, FolivixWhite.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Cómo usar la aplicación?",
                        style = MaterialTheme.typography.titleMedium,
                        color = FolivixWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onNavigateToManual,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixWhite,
                            contentColor = FolivixGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Manual de usuario",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FolivixWhite.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, FolivixWhite.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configuración del servidor",
                        style = MaterialTheme.typography.titleMedium,
                        color = FolivixWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "IP actual: ${uiState.serverIp}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FolivixWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { viewModel.showIpDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FolivixWhite,
                            contentColor = FolivixGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cambiar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (uiState.showIpDialog) {
        var ipInput by remember { mutableStateOf(uiState.serverIp) }

        AlertDialog(
            onDismissRequest = { viewModel.hideIpDialog() },
            title = { Text("Cambiar IP del servidor") },
            text = {
                Column {
                    Text(
                        "Ingresa la nueva dirección IP del servidor. La URL completa será: http://[IP]:5000/",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("Dirección IP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.ipInputError != null
                    )

                    uiState.ipInputError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateServerIp(ipInput) }) {
                    Text("Guardar", color = FolivixGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideIpDialog() }) {
                    Text("Cancelar", color = FolivixBlack)
                }
            }
        )
    }
}