package com.development.motorlog.relatorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.DadosDaFicha
import com.development.motorlog.domain.QuadroDaFicha
import com.development.motorlog.domain.descreverTempoJuntos
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.quadrosDaFicha
import com.development.motorlog.ui.theme.MlOk
import com.development.motorlog.ui.theme.MlOver
import com.development.motorlog.ui.theme.MlSoon
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarMesAno
import com.development.motorlog.ui.util.formatarNumero
import com.development.motorlog.ui.util.formatarUmaCasa
import com.development.motorlog.ui.util.hojeUtcMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Ficha da moto em imagem 1080×1350: foto de capa, nome, modelo e ano, há quanto tempo estão
// juntos, o km e até 4 quadros (domain/Ficha.kt). Pra mostrar a moto com orgulho ou anunciar na
// venda. Sem placa: a imagem vai pra rede.
suspend fun gerarFichaDaMoto(contexto: Context, moto: Moto, dados: DadosDaFicha, capa: String?): File = withContext(Dispatchers.IO) {
    val accent = accentDaMoto(moto).toArgb()
    val chakra = fonteChakra(contexto)
    val bmp = Bitmap.createBitmap(CARTAO_LARGURA, CARTAO_ALTURA, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(CARTAO_FUNDO)
    c.desenharCapaNoTopo(contexto, capa, accent, alturaFoto = 520)
    val largura = CARTAO_LARGURA - 2 * CARTAO_MARGEM
    fun pincel(tamanho: Float, cor: Int, negrito: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = tamanho; color = cor; if (negrito) typeface = chakra
    }
    fun tinta(cor: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cor }

    // faixa na cor da moto, nome (numa linha só), modelo e ano, tempo juntos
    c.drawRoundRect(RectF(CARTAO_MARGEM, 556f, CARTAO_MARGEM + 120f, 568f), 6f, 6f, tinta(accent))
    val nome = nomeDaMoto(moto)
    val titulo = pincel(92f, CARTAO_TEXTO, negrito = true)
    c.drawText(caberNumaLinha(nome, titulo, largura, minimo = 56f), CARTAO_MARGEM, 656f, titulo)
    val subtitulo = pincel(40f, CARTAO_APAGADO)
    val linhaMoto = listOf(if (nome != moto.modelo) moto.modelo else "", moto.anoFabricacao.takeIf { it > 0 }?.toString().orEmpty())
        .filter { it.isNotBlank() }.joinToString(" · ")
    if (linhaMoto.isNotEmpty()) c.drawText(caberNumaLinha(linhaMoto, subtitulo, largura, minimo = 30f), CARTAO_MARGEM, 714f, subtitulo)
    descreverTempoJuntos(moto.chegouEm, hojeUtcMillis())?.let { juntos ->
        val texto = (if (juntos == "hoje") "Chegou hoje" else "Juntos há $juntos") + " · desde ${formatarMesAno(moto.chegouEm)}"
        val p = pincel(38f, accent)
        c.drawText(caberNumaLinha(texto, p, largura, minimo = 28f), CARTAO_MARGEM, 768f, p)
    }

    // o km, grande
    val km = pincel(84f, CARTAO_TEXTO, negrito = true)
    val textoKm = formatarKm(moto.kilometragem)
    c.drawText(textoKm, CARTAO_MARGEM, 866f, km)
    c.drawText("no painel", CARTAO_MARGEM + km.measureText(textoKm) + 18f, 866f, pincel(32f, CARTAO_APAGADO))

    // quadros em 2 colunas
    val quadros = quadrosDaFicha(dados)
    val gap = 24f
    val larguraQuadro = (largura - gap) / 2
    val alturaQuadro = 160f
    quadros.forEachIndexed { i, q ->
        val x = CARTAO_MARGEM + (i % 2) * (larguraQuadro + gap)
        val y = 910f + (i / 2) * (alturaQuadro + gap)
        c.drawRoundRect(RectF(x, y, x + larguraQuadro, y + alturaQuadro), 24f, 24f, tinta(CARTAO_CAIXA))
        val (rotulo, valor, unidade, apoio) = textoDoQuadro(q, dados)
        val cor = if (q == QuadroDaFicha.INDICE) corDaNota(dados.indice?.nota ?: 0) else CARTAO_TEXTO
        c.drawText(rotulo, x + 28f, y + 44f, pincel(24f, CARTAO_APAGADO).apply { letterSpacing = 0.12f; isFakeBoldText = true })
        val pValor = pincel(60f, cor, negrito = true)
        c.drawText(valor, x + 28f, y + 108f, pValor)
        if (unidade.isNotEmpty()) c.drawText(unidade, x + 28f + pValor.measureText(valor) + 10f, y + 108f, pincel(28f, CARTAO_APAGADO))
        val pApoio = pincel(26f, CARTAO_APAGADO)
        c.drawText(caberNumaLinha(apoio, pApoio, larguraQuadro - 56f, minimo = 20f), x + 28f, y + 142f, pApoio)
    }

    c.desenharRodapeDoCartao(accent, chakra)
    salvarCartao(contexto, bmp, "motorlog_ficha_${moto.id}.png")
}

private fun textoDoQuadro(q: QuadroDaFicha, d: DadosDaFicha): TextoDoQuadro = when (q) {
    QuadroDaFicha.INDICE -> TextoDoQuadro("ÍNDICE DE CUIDADO", "${d.indice?.nota ?: 0}", "/100", d.indice?.faixa.orEmpty())
    QuadroDaFicha.KM_JUNTOS -> TextoDoQuadro("RODAMOS JUNTOS", formatarNumero(d.kmJuntos ?: 0), "km", "desde que ela chegou")
    QuadroDaFicha.MANUTENCAO -> TextoDoQuadro(
        "MANUTENÇÃO", formatarNumero(d.trocas + d.visitas), "registros",
        "${d.trocas} troca${if (d.trocas == 1) "" else "s"} · ${d.visitas} visita${if (d.visitas == 1) "" else "s"} à oficina",
    )
    QuadroDaFicha.CONSUMO -> TextoDoQuadro("CONSUMO", formatarUmaCasa(d.kmPorLitro ?: 0.0), "km/l", "média dos abastecimentos")
    QuadroDaFicha.CUIDADOS -> TextoDoQuadro("CUIDADOS", formatarNumero(d.cuidados), "", "anotados no diário")
}

// mesmas faixas do card do Painel (ui/components/CardIndiceDeCuidado.kt)
private fun corDaNota(nota: Int): Int = when {
    nota >= 70 -> MlOk
    nota >= 50 -> MlSoon
    else -> MlOver
}.toArgb()
