package com.development.motorlog.relatorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.QuadroDaRetrospectiva
import com.development.motorlog.domain.Retrospectiva
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.quadrosDaRetrospectiva
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarUmaCasa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Cartão da retrospectiva (1080×1350): a foto do ano em cima, "Meu 2026 com a Pretinha" e até 6
// quadros (domain/Retrospectiva.kt). Peças comuns em Cartoes.kt.
suspend fun gerarCartaoDaRetrospectiva(contexto: Context, moto: Moto, r: Retrospectiva, foto: String?): File = withContext(Dispatchers.IO) {
    val accent = accentDaMoto(moto).toArgb()
    val chakra = fonteChakra(contexto)
    val bmp = Bitmap.createBitmap(CARTAO_LARGURA, CARTAO_ALTURA, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(CARTAO_FUNDO)
    c.desenharCapaNoTopo(contexto, foto, accent, alturaFoto = 440)
    val largura = CARTAO_LARGURA - 2 * CARTAO_MARGEM
    fun pincel(tamanho: Float, cor: Int, negrito: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = tamanho; color = cor; if (negrito) typeface = chakra
    }

    c.drawRoundRect(RectF(CARTAO_MARGEM, 476f, CARTAO_MARGEM + 120f, 488f), 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })
    val titulo = pincel(76f, CARTAO_TEXTO, negrito = true)
    c.drawText(caberNumaLinha("Meu ${r.ano} com a ${nomeDaMoto(moto)}", titulo, largura, minimo = 44f), CARTAO_MARGEM, 568f, titulo)
    val sub = pincel(36f, CARTAO_APAGADO)
    c.drawText(caberNumaLinha(moto.modelo, sub, largura, minimo = 26f), CARTAO_MARGEM, 618f, sub)

    val gap = 20f
    val larguraQuadro = (largura - gap) / 2
    val alturaQuadro = 150f
    quadrosDaRetrospectiva(r).forEachIndexed { i, q ->
        val x = CARTAO_MARGEM + (i % 2) * (larguraQuadro + gap)
        val y = 660f + (i / 2) * (alturaQuadro + gap)
        c.drawRoundRect(RectF(x, y, x + larguraQuadro, y + alturaQuadro), 24f, 24f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARTAO_CAIXA })
        val (rotulo, valor, unidade, apoio) = textoDoQuadro(q, r)
        c.drawText(rotulo, x + 28f, y + 42f, pincel(24f, CARTAO_APAGADO).apply { letterSpacing = 0.12f; isFakeBoldText = true })
        val pValor = pincel(56f, if (i == 0) accent else CARTAO_TEXTO, negrito = true)
        c.drawText(valor, x + 28f, y + 102f, pValor)
        if (unidade.isNotEmpty()) c.drawText(unidade, x + 28f + pValor.measureText(valor) + 10f, y + 102f, pincel(28f, CARTAO_APAGADO))
        val pApoio = pincel(26f, CARTAO_APAGADO)
        c.drawText(caberNumaLinha(apoio, pApoio, larguraQuadro - 56f, minimo = 20f), x + 28f, y + 134f, pApoio)
    }

    c.desenharRodapeDoCartao(accent, chakra)
    salvarCartao(contexto, bmp, "motorlog_retrospectiva_${r.ano}.png")
}

private data class TextoDoQuadro(val rotulo: String, val valor: String, val unidade: String, val apoio: String)

private fun plural(n: Int, um: String, varios: String) = "$n ${if (n == 1) um else varios}"

private fun textoDoQuadro(q: QuadroDaRetrospectiva, r: Retrospectiva): TextoDoQuadro = when (q) {
    QuadroDaRetrospectiva.KM -> TextoDoQuadro("RODAMOS", formatarNumero(r.kmRodados ?: 0), "km", "juntos no ano")
    QuadroDaRetrospectiva.ROLES -> TextoDoQuadro(
        "ROLÊS", formatarNumero(r.roles.size), "",
        if (r.kmEmRoles > 0) "${formatarNumero(r.kmEmRoles)} km na estrada" else r.roles.joinToString(", ") { it.destino },
    )
    QuadroDaRetrospectiva.MANUTENCAO -> TextoDoQuadro(
        "MANUTENÇÃO", formatarNumero(r.trocas + r.visitas), "registros",
        "${plural(r.trocas, "troca", "trocas")} · ${plural(r.visitas, "visita", "visitas")}",
    )
    QuadroDaRetrospectiva.CUIDADOS -> TextoDoQuadro("CUIDADOS", formatarNumero(r.cuidados), "", if (r.lavagens > 0) plural(r.lavagens, "lavagem", "lavagens") else "anotados no diário")
    QuadroDaRetrospectiva.CONSUMO -> TextoDoQuadro("CONSUMO", formatarUmaCasa(r.kmPorLitro ?: 0.0), "km/l", plural(r.abastecimentos, "abastecimento", "abastecimentos"))
    QuadroDaRetrospectiva.FOTOS -> TextoDoQuadro("FOTOS", formatarNumero(r.fotos.size), "", "guardadas no álbum")
    QuadroDaRetrospectiva.INSTALADOS -> TextoDoQuadro("GANHOU", formatarNumero(r.instalados.size), "", r.instalados.joinToString(", ") { it.nome })
}
