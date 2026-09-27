package com.development.motorlog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.theme.MlSurfaceAlt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Foto do álbum (arquivo em filesDir/fotos), lida fora da main thread e cortada pra preencher.
// ladoMaxPx: tamanho de leitura — miniatura pequena não abre a foto inteira.
@Composable
fun FotoArquivo(
    arquivo: String,
    modifier: Modifier = Modifier,
    ladoMaxPx: Int = 480,
    contentScale: ContentScale = ContentScale.Crop,
    descricao: String? = null,
) {
    val contexto = LocalContext.current
    val imagem by produceState<ImageBitmap?>(ArmazemDeFotos.doCache(arquivo, ladoMaxPx)?.asImageBitmap(), arquivo, ladoMaxPx) {
        // o estado sobrevive à troca de 'arquivo' (mesmo slot na lista) → sempre recarrega
        value = withContext(Dispatchers.IO) { ArmazemDeFotos.carregar(contexto, arquivo, ladoMaxPx) }?.asImageBitmap()
    }
    Box(modifier.background(MlSurfaceAlt)) {
        imagem?.let { Image(it, contentDescription = descricao, modifier = Modifier.fillMaxSize(), contentScale = contentScale) }
    }
}

// Avatar da moto nos cards: a foto de capa quando o dono tem uma; senão, o badge desenhado.
@Composable
fun AvatarDaMoto(capa: String?, accent: Color, tamanho: Dp, modifier: Modifier = Modifier, raio: Dp = 16.dp) {
    if (capa == null) {
        BikeBadge(accent, modifier, tamanho = tamanho, raio = raio)
    } else {
        val forma = RoundedCornerShape(raio)
        FotoArquivo(
            capa,
            modifier.size(tamanho).clip(forma).border(1.5.dp, accent.copy(alpha = 0.7f), forma),
            ladoMaxPx = 256,
            descricao = "Foto da moto",
        )
    }
}

