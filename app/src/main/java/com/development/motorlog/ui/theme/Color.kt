package com.development.motorlog.ui.theme

import androidx.compose.ui.graphics.Color
import com.development.motorlog.data.Moto

// Paleta do protótipo (MotorLog_design2/theme.jsx) — tema dark, accent laranja
val MlBg = Color(0xFF0E1014)
val MlBgElev = Color(0xFF14171D)
val MlSurface = Color(0xFF181B22)
val MlSurfaceAlt = Color(0xFF20242D)
val MlSurfaceHi = Color(0xFF262B35)
val MlText = Color(0xFFF3F5F8)
val MlTextMuted = Color(0xFF9AA3B0)
// "apagado" (rótulos de seção, abas inativas, dicas): era #5E6675 (3,0:1 no card, abaixo do mínimo
// de leitura). #8A93A0 dá 4,6:1 a 6,1:1 em todos os fundos do app (WCAG AA pra texto pequeno)
val MlTextFaint = Color(0xFF8A93A0)
val MlBorder = Color(0xFF272C36)
val MlBorderHi = Color(0xFF333A46)
// borda do campo de texto parado: precisa de 3:1 contra o fundo pra se ver onde digitar (WCAG
// 1.4.11). MlBorderHi dava 1,5:1 e o campo sumia no sol. Esta dá 3,4:1 no card e 3,0:1 no diálogo.
val MlBordaCampo = Color(0xFF666E7D)
val MlOdoBg = Color(0xFF05070A)      // fundo dos dígitos do odômetro
val MlAccent = Color(0xFFFF6A2B)
val MlOnAccent = Color(0xFF1A0A00)

// status (semânticas, versão dark) — fg; o bg é a mesma cor com alpha (Pill/IconBox)
val MlOk = Color(0xFF5FD08A)
val MlSoon = Color(0xFFFFC24B)
val MlOver = Color(0xFFFF6B6B)

// accent por moto: o dono escolhe no cadastro (Moto.cor = índice aqui). As 4 primeiras são as
// ACCENTS do protótipo e continuam sendo a cor automática (pelo id) de quem não escolheu — não
// mude a ordem delas, senão a moto de quem já usa o app troca de cor sozinha.
val MlAccentsMoto = listOf(
    MlAccent, Color(0xFF23E0C8), Color(0xFFB6FF3D), Color(0xFFFF3B3B),
    Color(0xFF4D8DFF), Color(0xFFB07CFF), Color(0xFFFFD23F), Color(0xFFFF5FA2),
)
private const val CORES_AUTOMATICAS = 4
val NOMES_DAS_CORES = listOf("Laranja", "Turquesa", "Verde-limão", "Vermelho", "Azul", "Roxo", "Amarelo", "Rosa")

// índice da cor que a moto mostra hoje (a escolhida, ou a automática pelo id)
fun indiceDaCor(moto: Moto): Int =
    if (moto.cor in MlAccentsMoto.indices) moto.cor else (moto.id % CORES_AUTOMATICAS).toInt()

fun accentDaMoto(moto: Moto): Color = MlAccentsMoto[indiceDaCor(moto)]
