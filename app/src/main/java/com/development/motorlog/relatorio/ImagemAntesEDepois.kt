package com.development.motorlog.relatorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.AntesEDepois
import com.development.motorlog.domain.descreverIntervalo
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Antes e depois em imagem 1080×1350: as duas fotos empilhadas (moto se fotografa deitada), cada
// uma com "ANTES"/"DEPOIS", o dia e o km; embaixo o título ("Depois da lavagem"), a moto e o tempo
// entre as duas. Peças comuns em Cartoes.kt.
private const val ALTURA_FOTO = 520
private const val VAO = 12

suspend fun gerarAntesEDepois(contexto: Context, moto: Moto, par: AntesEDepois, titulo: String): File = withContext(Dispatchers.IO) {
    val accent = accentDaMoto(moto).toArgb()
    val chakra = fonteChakra(contexto)
    val bmp = Bitmap.createBitmap(CARTAO_LARGURA, CARTAO_ALTURA, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(CARTAO_FUNDO)

    c.desenharFotoRotulada(contexto, par.antes, "ANTES", 0, CARTAO_APAGADO, chakra)
    c.desenharFotoRotulada(contexto, par.depois, "DEPOIS", ALTURA_FOTO + VAO, accent, chakra)

    // título + "Pretinha · 12 dias depois"
    val largura = CARTAO_LARGURA - 2 * CARTAO_MARGEM
    val pTitulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_TEXTO; textSize = 60f; typeface = chakra }
    val yTitulo = 2f * ALTURA_FOTO + VAO + 80f
    c.drawText(caberNumaLinha(titulo.ifBlank { "Antes e depois" }, pTitulo, largura, minimo = 40f), CARTAO_MARGEM, yTitulo, pTitulo)
    val pSub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_APAGADO; textSize = 32f }
    c.drawText(caberNumaLinha("${nomeDaMoto(moto)} · ${descreverIntervalo(par)}", pSub, largura, minimo = 24f), CARTAO_MARGEM, yTitulo + 46f, pSub)

    c.desenharRodapeDoCartao(accent, chakra)
    salvarCartao(contexto, bmp, "motorlog_antes_depois_${par.antes.id}_${par.depois.id}.png")
}

// foto cobrindo a faixa inteira, sombra embaixo, pílula com o rótulo e "12/03/2026 · 23.400 km"
private fun Canvas.desenharFotoRotulada(contexto: Context, foto: FotoMoto, rotulo: String, topo: Int, corRotulo: Int, chakra: Typeface) {
    val destino = Rect(0, topo, CARTAO_LARGURA, topo + ALTURA_FOTO)
    val bmp = ArmazemDeFotos.carregar(contexto, foto.arquivo, CARTAO_LARGURA)
    if (bmp != null) desenharCortado(bmp, destino)
    else drawRect(destino, Paint().apply { color = CARTAO_CAIXA })
    val sombra = Paint().apply {
        shader = LinearGradient(0f, topo + ALTURA_FOTO * 0.6f, 0f, (topo + ALTURA_FOTO).toFloat(), 0x000E1014, 0xCC0E1014.toInt(), Shader.TileMode.CLAMP)
    }
    drawRect(destino, sombra)

    val pRotulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_FUNDO; textSize = 30f; typeface = chakra; letterSpacing = 0.1f }
    val larguraPilula = pRotulo.measureText(rotulo) + 44f
    val x = CARTAO_MARGEM - 32f
    val y = topo + 36f
    drawRoundRect(RectF(x, y, x + larguraPilula, y + 52f), 26f, 26f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = corRotulo })
    drawText(rotulo, x + 22f, y + 37f, pRotulo)

    val pInfo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_TEXTO; textSize = 32f }
    drawText("${formatarData(foto.data)} · ${formatarKm(foto.km)}", CARTAO_MARGEM - 32f, (topo + ALTURA_FOTO - 30).toFloat(), pInfo)
}
