package com.example.folivix.data.repository

import com.example.folivix.R
import com.example.folivix.model.DiseaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiseaseInfoRepositoryImpl @Inject constructor() : DiseaseInfoRepository {

    private val tips = listOf(
        "El maíz es uno de los cultivos más importantes a nivel mundial, con más de 1.000 millones de toneladas producidas anualmente.",
        "La roya común puede reducir el rendimiento del maíz hasta en un 45% en condiciones severas.",
        "La rotación de cultivos es una estrategia efectiva para reducir la incidencia de enfermedades foliares.",
        "El monitoreo temprano de enfermedades puede ahorrar hasta un 30% en costos de tratamiento.",
        "Las variedades híbridas de maíz suelen tener mayor resistencia a enfermedades foliares."
    )

    private val diseaseInfoList = MutableStateFlow(
        listOf(
            DiseaseInfo(
                name = "Roya común",
                description = "Causada por el hongo Puccinia sorghi. Se caracteriza por pústulas de color marrón rojizo en ambas caras de las hojas.",
                imageResId = R.drawable.common_rust1,
                detailImageResIds = listOf(
                    R.drawable.common_rust2,
                    R.drawable.common_rust3,
                    R.drawable.common_rust4,
                    R.drawable.common_rust5,
                    R.drawable.common_rust1
                ),
                detailedDescription = "La roya común es una enfermedad foliar del maíz con amplia distribución mundial.\n\n" +
                        "Patógeno: Causada por el hongo Puccinia sorghi, un patógeno biotrófico.\n\n" +
                        "Síntomas: Pústulas circulares a ovaladas de color marrón-rojizo que aparecen en ambas caras de las hojas. Las pústulas pueden cambiar a color negro al final de la temporada.\n\n" +
                        "Características: Las pústulas liberan esporas que son dispersadas por el viento a largas distancias.\n\n" +
                        "Impacto: Puede reducir el rendimiento del maíz hasta en un 45% en condiciones severas. Afecta principalmente al área foliar, reduciendo la capacidad fotosintética de la planta.\n\n" +
                        "Control: Rotación de cultivos, uso de variedades resistentes y aplicación de fungicidas foliares."
            ),

            DiseaseInfo(
                name = "Mancha gris",
                description = "Causada por el hongo Cercospora zeae-maydis. Produce lesiones rectangulares de color gris a marrón entre las nervaduras de las hojas.",
                imageResId = R.drawable.gray_leaf_spot1,
                detailImageResIds = listOf(
                    R.drawable.gray_leaf_spot2,
                    R.drawable.gray_leaf_spot3,
                    R.drawable.gray_leaf_spot4,
                    R.drawable.gray_leaf_spot5,
                    R.drawable.gray_leaf_spot1
                ),
                detailedDescription = "La mancha gris es una enfermedad foliar importante en regiones húmedas productoras de maíz.\n\n" +
                        "Patógeno: Causada por el hongo Cercospora zeae-maydis.\n\n" +
                        "Síntomas: Lesiones rectangulares de color gris a marrón, estrictamente limitadas por las nervaduras de las hojas, dando un aspecto de patrón rectangular característico.\n\n" +
                        "Características: Las lesiones son más visibles durante la etapa de floración y llenado de grano. El hongo sobrevive en residuos de cultivos infectados.\n\n" +
                        "Impacto: Puede reducir el rendimiento hasta un 40% en condiciones favorables para la enfermedad. Las plantas severamente afectadas presentan muerte prematura del follaje.\n\n" +
                        "Control: Rotación de cultivos, labranza de conservación, uso de híbridos resistentes y aplicación de fungicidas."
            ),

            DiseaseInfo(
                name = "Tizón foliar del norte",
                description = "Causada por el hongo Exserohilum turcicum. Produce lesiones grandes, alargadas y elípticas de color gris verdoso a marrón.",
                imageResId = R.drawable.northern_leaf_blight1,
                detailImageResIds = listOf(
                    R.drawable.northern_leaf_blight2,
                    R.drawable.northern_leaf_blight3,
                    R.drawable.northern_leaf_blight4,
                    R.drawable.northern_leaf_blight5,
                    R.drawable.northern_leaf_blight1
                ),
                detailedDescription = "El tizón foliar del norte es una de las enfermedades más destructivas del maíz en climas templados y húmedos.\n\n" +
                        "Patógeno: Causado por el hongo Exserohilum turcicum (antes Helminthosporium turcicum).\n\n" +
                        "Síntomas: Lesiones necróticas grandes, alargadas y elípticas, de color gris verdoso a marrón, con forma de cigarro o bote, que pueden llegar a medir de 2 a 15 cm de longitud.\n\n" +
                        "Características: Las lesiones comienzan en las hojas inferiores y avanzan hacia arriba. En condiciones húmedas, el hongo produce esporas oscuras sobre las lesiones.\n\n" +
                        "Impacto: Puede reducir el rendimiento del maíz hasta en un 50% cuando la infección ocurre antes o durante la floración. Afecta seriamente la producción de grano al reducir el área fotosintética.\n\n" +
                        "Control: Uso de híbridos con resistencia genética, rotación de cultivos, eliminación de residuos y aplicación de fungicidas."
            ),

            DiseaseInfo(
                name = "Mancha foliar Phaeosphaeria",
                description = "Causada por el hongo Phaeosphaeria maydis. Produce lesiones circulares a oblongas de color pajizo con bordes marrones.",
                imageResId = R.drawable.phaeosphaeria_leaf_spot1,
                detailImageResIds = listOf(
                    R.drawable.phaeosphaeria_leaf_spot2,
                    R.drawable.phaeosphaeria_leaf_spot3,
                    R.drawable.phaeosphaeria_leaf_spot4,
                    R.drawable.phaeosphaeria_leaf_spot5,
                    R.drawable.phaeosphaeria_leaf_spot1
                ),
                detailedDescription = "La mancha foliar Phaeosphaeria es una enfermedad importante en regiones tropicales y subtropicales.\n\n" +
                        "Patógeno: Causada por el hongo Phaeosphaeria maydis (fase sexual) y Phoma maydis (fase asexual).\n\n" +
                        "Síntomas: Lesiones circulares a oblongas de color pajizo con bordes marrones bien definidos. Las lesiones pueden fusionarse formando áreas necróticas más grandes.\n\n" +
                        "Características: La enfermedad se desarrolla rápidamente en condiciones de alta humedad y temperaturas moderadas. Las lesiones suelen aparecer primero en las hojas inferiores.\n\n" +
                        "Impacto: En condiciones severas, puede causar senescencia prematura de las hojas y reducir el rendimiento hasta en un 60%. Afecta gravemente la producción en regiones tropicales.\n\n" +
                        "Control: Uso de variedades resistentes, rotación de cultivos y aplicación de fungicidas a base de estrobilurinas."
            ),

            DiseaseInfo(
                name = "Roya del sur",
                description = "Causada por el hongo Puccinia polysora. Produce pústulas pequeñas, circulares a ovales, de color anaranjado a marrón claro.",
                imageResId = R.drawable.southern_rust1,
                detailImageResIds = listOf(
                    R.drawable.southern_rust2,
                    R.drawable.southern_rust3,
                    R.drawable.southern_rust4,
                    R.drawable.southern_rust5,
                    R.drawable.southern_rust1
                ),
                detailedDescription = "La roya del sur es una enfermedad agresiva en regiones cálidas y húmedas productoras de maíz.\n\n" +
                        "Patógeno: Causada por el hongo Puccinia polysora, un patógeno biótrofo específico del maíz.\n\n" +
                        "Síntomas: Pústulas pequeñas, circulares a ovales, de color anaranjado a marrón claro, distribuidas principalmente en el haz de las hojas. Son más pequeñas y de color más claro que las de la roya común.\n\n" +
                        "Características: La enfermedad se desarrolla rápidamente en condiciones cálidas (25-32°C) y húmedas. Las esporas se dispersan por el viento y pueden infectar nuevas plantas en 7-10 días.\n\n" +
                        "Impacto: En zonas tropicales y subtropicales puede causar pérdidas de rendimiento de hasta un 70%. Es particularmente dañina cuando la infección ocurre antes o durante la floración.\n\n" +
                        "Control: Uso de híbridos resistentes, siembra temprana para evitar altas presiones de la enfermedad y aplicación de fungicidas protectores y sistémicos."
            ),

            DiseaseInfo(
                name = "Hoja saludable",
                description = "Una hoja de maíz saludable presenta un color verde uniforme, sin manchas, lesiones o decoloraciones.",
                imageResId = R.drawable.healthy1,
                detailImageResIds = listOf(
                    R.drawable.healthy2,
                    R.drawable.healthy3,
                    R.drawable.healthy4,
                    R.drawable.healthy5,
                    R.drawable.healthy1

                ),
                detailedDescription = "Las hojas saludables son fundamentales para el crecimiento y productividad óptimos del maíz.\n\n" +
                        "Apariencia: Color verde uniforme, textura firme y erecta, sin manchas ni decoloraciones. El tono verde puede variar según la variedad y el estado nutricional.\n\n" +
                        "Características: Nervaduras bien definidas, ausencia de lesiones necróticas o cloróticas. El tejido foliar es flexible y resiliente.\n\n" +
                        "Importancia: El mantenimiento de hojas saludables es crucial ya que son los órganos fotosintéticos primarios, responsables de la producción de carbohidratos que se translocan al grano. Cada hoja sana contribuye significativamente al rendimiento final.\n\n" +
                        "Prevención: Buenas prácticas agronómicas como nutrición balanceada, control preventivo de plagas y enfermedades, manejo adecuado del riego y densidad de siembra óptima ayudan a mantener la sanidad foliar. La selección de híbridos adaptados a las condiciones locales también es fundamental."
            )
        )
    )

    override fun getAllDiseaseInfo(): Flow<List<DiseaseInfo>> {
        return diseaseInfoList
    }

    override fun getDiseaseInfo(name: String): Flow<DiseaseInfo?> {
        return diseaseInfoList.map { list ->
            list.find { it.name == name }
        }
    }

    override fun getRandomTip(): Flow<String> {
        return MutableStateFlow(tips.random())
    }
}