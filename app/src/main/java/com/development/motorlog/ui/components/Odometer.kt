package com.development.motorlog.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.motorlog.ui.theme.MlAccent
import com.development.motorlog.ui.theme.MlOdoBg
import com.development.motorlog.ui.theme.MlTextFaint
import com.development.motorlog.ui.theme.chakra
import com.development.motorlog.ui.util.formatarNumero

enum class TamanhoOdometro(val altura: Dp, val largura: Dp, val fonte: Int, val vao: Dp) {
    GRANDE(56.dp, 38.dp, 40, 5.dp),
    MEDIO(40.dp, 27.dp, 28, 4.dp),
    PEQUENO(30.dp, 19.dp, 20, 3.dp),
}

// Odômetro de painel: um dígito por célula escura, ponto de milhar apagado, "KM" em accent.
// É o elemento-herói do app (Odometer do protótipo).
@Composable
fun Odometer(km: Int, modifier: Modifier = Modifier, tamanho: TamanhoOdometro = TamanhoOdometro.GRANDE, accent: Color = MlAccent) {
    val digitos = formatarNumero(km)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        digitos.forEachIndexed { i, ch ->
            if (i > 0) Spacer(Modifier.width(tamanho.vao))
            if (ch == '.') {
                Text(".", style = chakra((tamanho.fonte * 0.7).sp), color = MlTextFaint,
                    modifier = Modifier.width(tamanho.largura * 0.34f).padding(top = tamanho.altura * 0.25f))
            } else {
                Box(
                    modifier = Modifier
                        .size(tamanho.largura, tamanho.altura)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MlOdoBg)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = 0.06f),
                                0.5f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.45f),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // linha do "tambor" no meio da célula
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.07f)))
                    // o dígito "rola" como num odômetro de verdade quando o km muda
                    AnimatedContent(
                        targetState = ch,
                        transitionSpec = {
                            (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                        },
                        label = "digito",
                    ) { d -> Text(d.toString(), style = chakra(tamanho.fonte.sp), color = Color.White) }
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "KM", style = chakra((tamanho.fonte * 0.4).sp), color = accent,
            modifier = Modifier.padding(top = tamanho.altura * 0.3f),
        )
    }
}
