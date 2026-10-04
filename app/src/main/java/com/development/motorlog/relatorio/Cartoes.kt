package com.development.motorlog.relatorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.development.motorlog.R
import com.development.motorlog.fotos.ArmazemDeFotos
import java.io.File
import kotlin.math.max

// Peças comuns dos cartões em imagem 1080×1350 (formato de post e de status) — marco, ficha da
// moto, antes e depois: fundo escuro, foto em cima escurecendo até o fundo, rodapé "MotorLog".
// Tudo com o Canvas do Android, como o PDF — sem biblioteca extra.
internal const val CARTAO_LARGURA = 1080
internal const val CARTAO_ALTURA = 1350
internal const val CARTAO_MARGEM = 80f
internal val CARTAO_FUNDO = 0xFF0E1014.toInt()
internal val CARTAO_TEXTO = 0xFFF3F5F8.toInt()
internal val CARTAO_APAGADO = 0xFF9AA3B0.toInt()
internal val CARTAO_CAIXA = 0xFF181B22.toInt()

internal fun fonteChakra(contexto: Context): Typeface =
    runCatching { ResourcesCompat.getFont(contexto, R.font.chakra_petch_bold) }.getOrNull() ?: Typeface.DEFAULT_BOLD

// foto de capa: corte central na faixa de cima, escurecendo até o fundo. Sem foto: um brilho na cor da moto
internal fun Canvas.desenharCapaNoTopo(contexto: Context, capa: String?, accent: Int, alturaFoto: Int) {
    val foto = capa?.let { ArmazemDeFotos.carregar(contexto, it, CARTAO_LARGURA) }
    if (foto != null) {
        desenharCortado(foto, Rect(0, 0, CARTAO_LARGURA, alturaFoto))
        val sombra = Paint().apply {
            shader = LinearGradient(0f, alturaFoto * 0.35f, 0f, alturaFoto.toFloat(), 0x000E1014, CARTAO_FUNDO, Shader.TileMode.CLAMP)
        }
        drawRect(0f, 0f, CARTAO_LARGURA.toFloat(), alturaFoto.toFloat(), sombra)
    } else {
        val brilho = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, alturaFoto.toFloat(), (accent and 0x00FFFFFF) or 0x55000000, CARTAO_FUNDO, Shader.TileMode.CLAMP)
        }
        drawRect(0f, 0f, CARTAO_LARGURA.toFloat(), alturaFoto.toFloat(), brilho)
    }
}

// desenha a foto cobrindo o destino inteiro (corte central, sem distorcer)
internal fun Canvas.desenharCortado(foto: Bitmap, destino: Rect) {
    val escala = max(destino.width().toFloat() / foto.width, destino.height().toFloat() / foto.height)
    val w = (destino.width() / escala).toInt()
    val h = (destino.height() / escala).toInt()
    val origem = Rect((foto.width - w) / 2, (foto.height - h) / 2, (foto.width + w) / 2, (foto.height + h) / 2)
    drawBitmap(foto, origem, destino, Paint(Paint.FILTER_BITMAP_FLAG))
}

// rodapé: "MotorLog" na cor da moto à esquerda, "o caderninho da moto" à direita
internal fun Canvas.desenharRodapeDoCartao(accent: Int, chakra: Typeface) {
    val y = CARTAO_ALTURA - CARTAO_MARGEM
    drawText("MotorLog", CARTAO_MARGEM, y, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; textSize = 34f; typeface = chakra })
    val apagado = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_APAGADO; textSize = 30f; textAlign = Paint.Align.RIGHT }
    drawText("o caderninho da moto", CARTAO_LARGURA - CARTAO_MARGEM, y, apagado)
}

// grava o PNG em cache/cartoes (o caminho do FileProvider). Só o último fica: é cache, o compartilhar já levou
internal fun salvarCartao(contexto: Context, bmp: Bitmap, nome: String): File {
    val pasta = File(contexto.cacheDir, "cartoes").apply { mkdirs() }
    pasta.listFiles()?.forEach { it.delete() }
    val arquivo = File(pasta, nome)
    arquivo.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bmp.recycle()
    return arquivo
}

// quebra por palavras pra caber na largura (o Canvas não quebra texto sozinho)
internal fun quebrarEmLinhas(texto: String, pincel: Paint, largura: Float): List<String> {
    val linhas = mutableListOf<String>()
    var atual = ""
    texto.split(' ').forEach { palavra ->
        val tentativa = if (atual.isEmpty()) palavra else "$atual $palavra"
        if (pincel.measureText(tentativa) <= largura || atual.isEmpty()) atual = tentativa
        else { linhas += atual; atual = palavra }
    }
    if (atual.isNotEmpty()) linhas += atual
    return linhas
}

// diminui a fonte até o texto caber numa linha (nome longo de moto); no mínimo `minimo`, depois corta com "…"
internal fun caberNumaLinha(texto: String, pincel: Paint, largura: Float, minimo: Float): String {
    while (pincel.measureText(texto) > largura && pincel.textSize > minimo) pincel.textSize -= 2f
    if (pincel.measureText(texto) <= largura) return texto
    var t = texto
    while (t.length > 1 && pincel.measureText("$t…") > largura) t = t.dropLast(1)
    return "$t…"
}
