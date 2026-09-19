package com.development.motorlog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.development.motorlog.R

// Chakra Petch (OFL, embutida em res/font): a fonte dos NÚMEROS e títulos do protótipo — cara de
// instrumento de painel. Texto corrido continua na fonte do sistema (Roboto), como no design.
val ChakraPetch = FontFamily(
    Font(R.font.chakra_petch_regular, FontWeight.Normal),
    Font(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(R.font.chakra_petch_bold, FontWeight.Bold),
)

// Estilo de número/título em Chakra Petch, no tamanho pedido
fun chakra(tamanho: TextUnit, peso: FontWeight = FontWeight.Bold, lineHeight: TextUnit = TextUnit.Unspecified) =
    TextStyle(fontFamily = ChakraPetch, fontWeight = peso, fontSize = tamanho, lineHeight = lineHeight)

val Typography = Typography(
    // títulos de tela / cabeçalhos (Header do protótipo: 19 normal, 27 "big")
    headlineMedium = chakra(27.sp, lineHeight = 31.sp),
    titleLarge = chakra(19.sp, lineHeight = 23.sp),
    // títulos de card
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.5.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, lineHeight = 19.sp),
    // texto corrido
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 17.sp),
    // rótulos (SectionLabel usa labelSmall + uppercase)
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.5.sp, lineHeight = 15.sp, letterSpacing = 0.9.sp),
)

@Composable
@ReadOnlyComposable
fun numeroGrande() = chakra(25.sp)
