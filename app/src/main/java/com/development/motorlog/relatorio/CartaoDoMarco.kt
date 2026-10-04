package com.development.motorlog.relatorio

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
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

// Cartão de um marco ("Passou dos 50.000 km!", "1 ano com a Pretinha") em imagem 1080×1350
// (formato de post e de status), pra mandar no WhatsApp ou postar. Foto de capa em cima, na cor da
// moto (peças comuns em Cartoes.kt).
suspend fun gerarCartaoDoMarco(contexto: Context, moto: Moto, marco: Marco, capa: String?): File = withContext(Dispatchers.IO) {
    val accent = accentDaMoto(moto).toArgb()
    val chakra = fonteChakra(contexto)
    val bmp = Bitmap.createBitmap(CARTAO_LARGURA, CARTAO_ALTURA, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(CARTAO_FUNDO)
    c.desenharCapaNoTopo(contexto, capa, accent, alturaFoto = 760)

    // faixa na cor da moto + título do marco (quebrado em linhas) + nome e km
    c.drawRoundRect(RectF(CARTAO_MARGEM, 800f, CARTAO_MARGEM + 120f, 812f), 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })
    val titulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_TEXTO; textSize = 96f; typeface = chakra }
    var y = 920f
    quebrarEmLinhas(marco.titulo, titulo, CARTAO_LARGURA - 2 * CARTAO_MARGEM).forEach { linha ->
        c.drawText(linha, CARTAO_MARGEM, y, titulo)
        y += 108f
    }
    val subtitulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_APAGADO; textSize = 40f }
    val nome = nomeDaMoto(moto)
    val linhaMoto = if (nome != moto.modelo) "$nome · ${moto.modelo}" else nome
    c.drawText(linhaMoto, CARTAO_MARGEM, y + 10f, subtitulo)
    c.drawText("${formatarData(marco.data)} · ${formatarKm(moto.kilometragem)}", CARTAO_MARGEM, y + 66f, subtitulo)

    c.desenharRodapeDoCartao(accent, chakra)
    salvarCartao(contexto, bmp, "motorlog_${marco.chave}.png")
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
