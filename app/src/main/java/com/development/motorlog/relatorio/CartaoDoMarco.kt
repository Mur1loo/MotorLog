package com.development.motorlog.relatorio

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.development.motorlog.R
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.Marco
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

// Cartão de um marco ("Passou dos 50.000 km!", "1 ano com a Pretinha") em imagem 1080×1350
// (formato de post e de status), pra mandar no WhatsApp ou postar. Foto de capa em cima, na cor da
// moto; desenhado com o Canvas do Android, como o PDF — sem biblioteca extra.
private const val LARGURA = 1080
private const val ALTURA = 1350
private const val MARGEM = 80f
private val FUNDO = 0xFF0E1014.toInt()
private val TEXTO = 0xFFF3F5F8.toInt()
private val APAGADO = 0xFF9AA3B0.toInt()

suspend fun gerarCartaoDoMarco(contexto: Context, moto: Moto, marco: Marco, capa: String?): File = withContext(Dispatchers.IO) {
    val accent = accentDaMoto(moto).toArgb()
    val chakra = runCatching { ResourcesCompat.getFont(contexto, R.font.chakra_petch_bold) }.getOrNull() ?: Typeface.DEFAULT_BOLD
    val bmp = Bitmap.createBitmap(LARGURA, ALTURA, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(FUNDO)

    // foto de capa: corte central na faixa de cima, escurecendo até o fundo
    val alturaFoto = 760
    val foto = capa?.let { ArmazemDeFotos.carregar(contexto, it, LARGURA) }
    if (foto != null) {
        val escala = max(LARGURA.toFloat() / foto.width, alturaFoto.toFloat() / foto.height)
        val w = (LARGURA / escala).toInt()
        val h = (alturaFoto / escala).toInt()
        val origem = Rect((foto.width - w) / 2, (foto.height - h) / 2, (foto.width + w) / 2, (foto.height + h) / 2)
        c.drawBitmap(foto, origem, Rect(0, 0, LARGURA, alturaFoto), Paint(Paint.FILTER_BITMAP_FLAG))
        val sombra = Paint().apply {
            shader = LinearGradient(0f, alturaFoto * 0.35f, 0f, alturaFoto.toFloat(), 0x000E1014, FUNDO, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, LARGURA.toFloat(), alturaFoto.toFloat(), sombra)
    } else {
        // sem foto: um brilho na cor da moto
        val brilho = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, alturaFoto.toFloat(), (accent and 0x00FFFFFF) or 0x55000000, FUNDO, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, LARGURA.toFloat(), alturaFoto.toFloat(), brilho)
    }

    // faixa na cor da moto + título do marco (quebrado em linhas) + nome e km
    c.drawRoundRect(RectF(MARGEM, 800f, MARGEM + 120f, 812f), 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })
    val titulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TEXTO; textSize = 96f; typeface = chakra }
    var y = 920f
    quebrarEmLinhas(marco.titulo, titulo, LARGURA - 2 * MARGEM).forEach { linha ->
        c.drawText(linha, MARGEM, y, titulo)
        y += 108f
    }
    val subtitulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = APAGADO; textSize = 40f }
    val nome = nomeDaMoto(moto)
    val linhaMoto = if (nome != moto.modelo) "$nome · ${moto.modelo}" else nome
    c.drawText(linhaMoto, MARGEM, y + 10f, subtitulo)
    c.drawText("${formatarData(marco.data)} · ${formatarKm(moto.kilometragem)}", MARGEM, y + 66f, subtitulo)

    // rodapé
    val rodape = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; textSize = 34f; typeface = chakra }
    c.drawText("MotorLog", MARGEM, ALTURA - MARGEM, rodape)
    val rodapeApagado = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = APAGADO; textSize = 30f; textAlign = Paint.Align.RIGHT }
    c.drawText("o caderninho da moto", LARGURA - MARGEM, ALTURA - MARGEM, rodapeApagado)

    val pasta = File(contexto.cacheDir, "cartoes").apply { mkdirs() }
    pasta.listFiles()?.forEach { it.delete() }   // só o último fica (é cache, o compartilhar já levou)
    val arquivo = File(pasta, "motorlog_${marco.chave}.png")
    arquivo.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bmp.recycle()
    arquivo
}

fun compartilharCartao(contexto: Context, arquivo: File, texto: String) {
    val uri = FileProvider.getUriForFile(contexto, contexto.packageName + ArmazemDeFotos.AUTORIDADE_SUFIXO, arquivo)
    val enviar = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_TEXT, texto)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    enviar.clipData = ClipData.newRawUri(arquivo.name, uri)
    contexto.startActivity(Intent.createChooser(enviar, "Compartilhar"))
}

// quebra por palavras pra caber na largura (o Canvas não quebra texto sozinho)
private fun quebrarEmLinhas(texto: String, pincel: Paint, largura: Float): List<String> {
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
