package com.example.folivix.ui.screens.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.folivix.R
import com.example.folivix.ui.theme.FolivixGreen

@Composable
fun UserManualScreen(
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver atrás",
                    tint = FolivixGreen
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.folivix_logo),
                contentDescription = "FOLIVIX Logo",
                modifier = Modifier.width(150.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Manual de usuario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )

            ManualCard(title = "Introducción y perfiles") {
                Text(
                    text = "FOLIVIX es una aplicación dedicada a la identificación de enfermedades foliares en cultivos de maíz. Te permite analizar hojas, guardar resultados y acceder a información detallada sobre las enfermedades.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Gestión de perfiles:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Puedes crear hasta 3 perfiles diferentes para que distintos usuarios utilicen la aplicación.")
                BulletPoint(text = "Al iniciar la app por primera vez, deberás crear un perfil con tu nombre y opcional una foto.")
                BulletPoint(text = "Para cambiar entre perfiles, accede desde el icono de perfil en la esquina superior derecha.")
                BulletPoint(text = "Cada perfil mantiene su propio historial de análisis y estadísticas.")
            }

            ManualCard(title = "Navegación principal") {
                Text(
                    text = "La aplicación se divide en tres secciones principales accesibles desde la barra de navegación inferior:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "1. Historial",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Aquí encontrarás todos tus análisis anteriores ordenados cronológicamente. Puedes:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Filtrar resultados por tipo de enfermedad, fecha o nivel de precisión.")
                BulletPoint(text = "Tocar en cualquier análisis para ver detalles completos.")
                BulletPoint(text = "Guardar imágenes de los análisis en tu galería.")
                BulletPoint(text = "Eliminar análisis que ya no necesites.")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "2. Inicio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Esta pantalla muestra información útil y estadísticas:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Consejos cambiantes sobre cultivo y prevención de enfermedades.")
                BulletPoint(text = "Gráfico estadístico de tus análisis agrupados por enfermedades.")
                BulletPoint(text = "Información detallada sobre cada enfermedad - toca en cualquier tarjeta para ver más detalles, incluyendo múltiples imágenes de referencia.")
                BulletPoint(text = "Consejos específicos para el control de cada enfermedad.")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "3. Identificar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Sección principal para analizar hojas de maíz:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Selecciona una imagen de la galería o toma una foto directamente.")
                BulletPoint(text = "Opcional: Edita/recorta la imagen para enfocarte en el área afectada.")
                BulletPoint(text = "Presiona 'Analizar' para enviar la imagen al servidor de análisis.")
                BulletPoint(text = "Revisa los resultados que muestran el tipo de enfermedad, porcentaje de confianza y tiempo de procesamiento.")
                BulletPoint(text = "Guarda el resultado en tu historial para referencias futuras.")
            }

            ManualCard(title = "Análisis de enfermedades") {
                Text(
                    text = "FOLIVIX puede identificar 5 enfermedades comunes en el maíz, además de reconocer hojas saludables:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Enfermedades detectables:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Roya común - Causada por el hongo Puccinia sorghi.")
                BulletPoint(text = "Mancha gris - Causada por el hongo Cercospora zeae-maydis.")
                BulletPoint(text = "Tizón foliar del norte - Causado por el hongo Exserohilum turcicum.")
                BulletPoint(text = "Mancha foliar Phaeosphaeria - Causada por el hongo Phaeosphaeria maydis.")
                BulletPoint(text = "Roya del sur - Causada por el hongo Puccinia polysora.")
                BulletPoint(text = "Hojas saludables - Sin presencia de enfermedades.")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Proceso de análisis:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "1. Capturar una imagen clara de la hoja, preferiblemente con buena iluminación y enfocando en las áreas con síntomas visibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "2. El algoritmo de inteligencia artificial analiza la imagen comparándola con miles de muestras de entrenamiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "3. Se presenta el resultado con un porcentaje de confianza que indica la seguridad del diagnóstico.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "4. Si la confianza es menor al 50%, la aplicación mostrará 'La imagen no se ve como una hoja' para evitar diagnósticos incorrectos.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ManualCard(title = "Estadísticas y consejos") {
                Text(
                    text = "Estadísticas:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "La pantalla de inicio muestra un gráfico circular con la distribución de enfermedades en tus análisis.")
                BulletPoint(text = "Cada sector muestra el número de análisis y la precisión promedio para esa enfermedad.")
                BulletPoint(text = "El centro del gráfico muestra el número total de hojas analizadas.")
                BulletPoint(text = "La leyenda permite identificar fácilmente qué color corresponde a cada enfermedad.")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Consejos de prevención:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "La sección 'Consejos para el control de enfermedades' muestra recomendaciones específicas para cada enfermedad.")
                BulletPoint(text = "Los consejos cambian automáticamente cada 5 segundos.")
                BulletPoint(text = "Incluyen estrategias de prevención, control y manejo de cada enfermedad.")
                BulletPoint(text = "Se basan en prácticas agrícolas recomendadas por expertos.")
            }

            ManualCard(title = "Configuración y ajustes") {
                Text(
                    text = "Perfil de usuario:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Accede al perfil tocando el icono circular en la esquina superior derecha.")
                BulletPoint(text = "Puedes editar tu nombre y cambiar tu foto de perfil.")
                BulletPoint(text = "También puedes ver estadísticas personales como total de hojas analizadas y precisión promedio.")
                BulletPoint(text = "La opción 'Cambiar de usuario' te permite acceder a la selección de perfiles.")
                BulletPoint(text = "Si deseas, puedes eliminar tu perfil actual, pero ten en cuenta que perderás todos tus análisis.")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Configuración del servidor:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Accede a la configuración del servidor desde el botón de información (i) en la esquina superior izquierda.")
                BulletPoint(text = "Puedes cambiar la dirección IP del servidor en caso de que se modifique la conexión.")
                BulletPoint(text = "La dirección predeterminada es 192.168.1.45 con puerto 5000.")
                BulletPoint(text = "Esta configuración es crucial para la funcionalidad de análisis de imágenes, ya que determina dónde se envían las imágenes para su procesamiento.")
            }

            ManualCard(title = "Recomendaciones y mejores prácticas") {
                Text(
                    text = "Para obtener mejores resultados:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BulletPoint(text = "Toma fotos con buena iluminación, preferiblemente luz natural.")
                BulletPoint(text = "Enfoca claramente la hoja y asegúrate de que los síntomas sean visibles.")
                BulletPoint(text = "Utiliza la función de edición para centrar la imagen en el área afectada.")
                BulletPoint(text = "Mantén la hoja sobre un fondo que contraste con ella para obtener mejores resultados.")
                BulletPoint(text = "Para enfermedades en etapas tempranas, es recomendable tomar múltiples fotos y analizar varias áreas de la hoja.")
                BulletPoint(text = "Asegúrate de que la aplicación esté conectada a la misma red que el servidor de análisis.")
                BulletPoint(text = "Para un diagnóstico más preciso, considera analizar varias hojas del mismo cultivo.")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ManualCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FolivixGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider(
                color = FolivixGreen.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            content()
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp, top = 0.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}