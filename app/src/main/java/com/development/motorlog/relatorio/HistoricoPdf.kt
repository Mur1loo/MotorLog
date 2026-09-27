package com.development.motorlog.relatorio

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.development.motorlog.R
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.ItemDoHistorico
import com.development.motorlog.domain.OrigemItem
import com.development.motorlog.domain.Recomendacao
import com.development.motorlog.domain.RelatorioMoto
import com.development.motorlog.domain.StatusTroca
import com.development.motorlog.domain.calcularRecomendacoes
import com.development.motorlog.domain.comRevisao
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.kmRodadosNoApp
import com.development.motorlog.domain.montarRelatorio
import com.development.motorlog.domain.nomeDoArquivoPdf
import com.development.motorlog.domain.recomendacaoDeRevisao
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.formatarReais
import com.development.motorlog.ui.util.formatarReaisCentavos
import com.development.motorlog.ui.util.hojeUtcMillis
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Histórico da moto em PDF (A4), pronto pra mandar no WhatsApp: pro comprador na hora de vender,
// pro mecânico, ou pra guardar. Lê o banco, monta o conteúdo com montarRelatorio (domain) e
// desenha com o PdfDocument do próprio Android — sem biblioteca extra.
suspend fun AppDatabase.gerarHistoricoPdf(contexto: Context, moto: Moto): File = withContext(Dispatchers.IO) {
    val pecas = pecaDao().listarPecas()
    val registros = registroDao().listarRegistros(moto.id)
    val servicos = servicoDao().query(moto.id)
    val recs = calcularRecomendacoes(moto.kilometragem, pecas, registros)
    val relatorio = montarRelatorio(
        moto = moto,
        pecas = pecas,
        registros = registros,
        servicos = servicos,
        recomendacoes = comRevisao(recs, recomendacaoDeRevisao(moto.kilometragem, moto.intervaloRevisaoKm, servicos)),
        kmRodados = kmRodadosNoApp(historicoKmDao().listarPorMoto(moto.id), moto.kilometragem),
    )
    val capa = escolherCapa(fotoMotoDao().listarPorMoto(moto.id), moto.fotoCapaId)
        ?.let { ArmazemDeFotos.carregarParaPdf(contexto, it.arquivo) }

    val pasta = File(contexto.cacheDir, "relatorios").apply { mkdirs() }
    pasta.listFiles()?.forEach { it.delete() }   // só o último fica (é cache, o compartilhar já levou)
    val arquivo = File(pasta, nomeDoArquivoPdf(moto))
    DesenhistaDoHistorico(contexto, relatorio, capa, accentDaMoto(moto).toArgb()).gravar(arquivo)
    arquivo
}

// Abre o "compartilhar" do Android com o PDF anexado (WhatsApp, e-mail, Drive…)
fun compartilharPdf(contexto: Context, arquivo: File, moto: Moto) {
    val uri = FileProvider.getUriForFile(contexto, contexto.packageName + ArmazemDeFotos.AUTORIDADE_SUFIXO, arquivo)
    val enviar = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_SUBJECT, "Histórico de manutenção — ${moto.modelo} ${moto.placa}".trim())
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    enviar.clipData = ClipData.newRawUri(arquivo.name, uri)
    contexto.startActivity(Intent.createChooser(enviar, "Compartilhar histórico"))
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// Desenho: cada pedaço da página é um Bloco (altura medida + como desenhar). Primeiro distribui
// os blocos nas páginas (assim o rodapé sabe o total: "página 1 de 3"), depois desenha.

private const val LARGURA = 595f     // A4 em pontos
private const val ALTURA = 842f
private const val MARGEM = 40f
private const val RODAPE = 34f
private const val CONTEUDO = LARGURA - 2 * MARGEM

private val TINTA = 0xFF1A1D23.toInt()
private val APAGADO = 0xFF6B7280.toInt()
private val LINHA = 0xFFE3E6EB.toInt()
private val FUNDO_CAIXA = 0xFFF5F6F8.toInt()
private val COR_OK = 0xFF1E9E5A.toInt()        // versões escuras das cores de status (papel branco)
private val COR_PERTO = 0xFFC98A00.toInt()
private val COR_VENCIDA = 0xFFD64545.toInt()

private class Bloco(val altura: Float, val desenhar: Canvas.(y: Float) -> Unit)

private class DesenhistaDoHistorico(contexto: Context, private val r: RelatorioMoto, private val capa: Bitmap?, accent: Int) {
    // o accent da moto é claro (feito pro tema escuro); no papel, escurece um pouco pra ter contraste
    private val destaque = escurecer(accent, 0.78f)
    private val chakra = runCatching { ResourcesCompat.getFont(contexto, R.font.chakra_petch_bold) }.getOrNull() ?: Typeface.DEFAULT_BOLD
    private val chakraMedio = runCatching { ResourcesCompat.getFont(contexto, R.font.chakra_petch_semibold) }.getOrNull() ?: Typeface.DEFAULT_BOLD

    private fun pincel(tamanho: Float, cor: Int = TINTA, fonte: Typeface = Typeface.DEFAULT, alinhar: Paint.Align = Paint.Align.LEFT) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = tamanho; color = cor; typeface = fonte; textAlign = alinhar }

    private val titulo = pincel(24f, fonte = chakra)
    private val subtitulo = pincel(10.5f, APAGADO)
    private val rotulo = pincel(7.5f, APAGADO, Typeface.DEFAULT_BOLD).apply { letterSpacing = 0.12f }
    private val secao = pincel(9f, TINTA, chakraMedio).apply { letterSpacing = 0.1f }
    private val numero = pincel(15f, fonte = chakra)
    private val texto = pincel(10f)
    private val textoForte = pincel(10.5f, fonte = Typeface.DEFAULT_BOLD)
    private val miudo = pincel(8.5f, APAGADO)
    private val valorDireita = pincel(10.5f, fonte = chakra, alinhar = Paint.Align.RIGHT)
    private val miudoDireita = pincel(8.5f, APAGADO, alinhar = Paint.Align.RIGHT)
    private val preenchido = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun gravar(arquivo: File) {
        val paginas = paginar(montarBlocos())
        val doc = PdfDocument()
        try {
            paginas.forEachIndexed { i, blocos ->
                val pagina = doc.startPage(PdfDocument.PageInfo.Builder(LARGURA.toInt(), ALTURA.toInt(), i + 1).create())
                val c = pagina.canvas
                c.drawColor(android.graphics.Color.WHITE)
                preenchido.color = destaque
                c.drawRect(0f, 0f, LARGURA, 5f, preenchido)   // filete na cor da moto
                var y = MARGEM
                blocos.forEach { b -> b.desenhar.invoke(c, y); y += b.altura }
                desenharRodape(c, i + 1, paginas.size)
                doc.finishPage(pagina)
            }
            arquivo.outputStream().use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
    }

    private fun paginar(blocos: List<Bloco>): List<List<Bloco>> {
        val limite = ALTURA - MARGEM - RODAPE
        val paginas = mutableListOf(mutableListOf<Bloco>())
        var y = MARGEM
        blocos.forEach { b ->
            if (y + b.altura > limite && paginas.last().isNotEmpty()) {
                paginas.add(mutableListOf())
                y = MARGEM
            }
            paginas.last().add(b)
            y += b.altura
        }
        return paginas
    }

    private fun desenharRodape(c: Canvas, pagina: Int, total: Int) {
        val y = ALTURA - MARGEM + 12f
        preenchido.color = LINHA
        c.drawRect(MARGEM, y - 14f, LARGURA - MARGEM, y - 13.4f, preenchido)
        c.drawText("Gerado pelo app MotorLog em ${formatarData(hojeUtcMillis())} · registros feitos pelo dono da moto", MARGEM, y, miudo)
        c.drawText("página $pagina de $total", LARGURA - MARGEM, y, miudoDireita)
    }

    // ── conteúdo ──

    private fun montarBlocos(): List<Bloco> = buildList {
        add(cabecalho())
        add(espaco(16f))
        add(resumo())
        add(espaco(22f))
        if (r.situacao.isNotEmpty()) {
            add(tituloDeSecao("SITUAÇÃO DAS PEÇAS", "com ${formatarKm(r.moto.kilometragem)} no painel"))
            add(cabecalhoDaSituacao())
            r.situacao.forEach { add(linhaDaSituacao(it)) }
            add(espaco(22f))
        }
        add(tituloDeSecao("HISTÓRICO", "${r.itens.size} registro${if (r.itens.size == 1) "" else "s"}, do mais recente pro mais antigo"))
        if (r.itens.isEmpty()) add(linhaDeTexto("Nenhuma troca ou visita à oficina registrada ainda.", miudo))
        r.itens.forEach { add(itemDoHistorico(it)) }
    }

    private fun espaco(h: Float) = Bloco(h) {}

    private fun cabecalho(): Bloco {
        val alturaFoto = 104f
        val larguraFoto = if (capa != null) alturaFoto * 1.45f else 0f
        return Bloco(alturaFoto + 6f) { y ->
            val xTexto = if (capa != null) MARGEM + larguraFoto + 16f else MARGEM
            if (capa != null) desenharFoto(this, capa, RectF(MARGEM, y, MARGEM + larguraFoto, y + alturaFoto))
            drawText("MOTORLOG · HISTÓRICO DE MANUTENÇÃO", xTexto, y + 12f, rotulo.comCor(destaque))
            val largura = LARGURA - MARGEM - xTexto
            drawText(caber(r.moto.modelo, titulo, largura), xTexto, y + 42f, titulo)
            drawText(listOf(r.moto.anoFabricacao.toString(), r.moto.placa).filter { it.isNotBlank() }.joinToString(" · "), xTexto, y + 60f, subtitulo)
            drawText(formatarKm(r.moto.kilometragem), xTexto, y + 88f, numero)
            val atualizado = if (r.moto.kmAtualizadoEm > 0) "no painel · atualizado em ${formatarData(r.moto.kmAtualizadoEm)}" else "no painel"
            drawText(atualizado, xTexto + numero.measureText(formatarKm(r.moto.kilometragem)) + 8f, y + 88f, miudo)
        }
    }

    private fun resumo(): Bloco {
        val caixas = listOf(
            "TOTAL INVESTIDO" to (formatarReais(r.gastoTotal) to "oficina + por conta"),
            "NA OFICINA" to (formatarReais(r.gastoOficina) to "${r.visitas} visita${if (r.visitas == 1) "" else "s"}"),
            "POR CONTA PRÓPRIA" to (formatarReais(r.gastoPorConta) to "${r.trocasPorConta} troca${if (r.trocasPorConta == 1) "" else "s"} de peça"),
            "CUSTO POR KM" to ((r.custoPorKm?.let(::formatarReaisCentavos) ?: "—") to if (r.custoPorKm != null) "desde o 1º registro" else "rode mais pra calcular"),
        )
        val gap = 8f
        val largura = (CONTEUDO - gap * (caixas.size - 1)) / caixas.size
        return Bloco(58f) { y ->
            caixas.forEachIndexed { i, (rot, par) ->
                val x = MARGEM + i * (largura + gap)
                preenchido.color = FUNDO_CAIXA
                drawRoundRect(RectF(x, y, x + largura, y + 58f), 8f, 8f, preenchido)
                drawText(rot, x + 10f, y + 16f, rotulo)
                drawText(caber(par.first, numero, largura - 20f), x + 10f, y + 36f, if (i == 0) numero.comCor(destaque) else numero)
                drawText(caber(par.second, miudo, largura - 20f), x + 10f, y + 50f, miudo)
            }
        }
    }

    private fun tituloDeSecao(t: String, apoio: String) = Bloco(26f) { y ->
        drawText(t, MARGEM, y + 12f, secao)
        drawText(apoio, MARGEM + secao.measureText(t) + 10f, y + 12f, miudo)
        preenchido.color = TINTA
        drawRect(MARGEM, y + 18f, LARGURA - MARGEM, y + 19.2f, preenchido)
    }

    // colunas da tabela de situação
    private val colUltima = MARGEM + CONTEUDO * 0.44f
    private val colProxima = MARGEM + CONTEUDO * 0.60f
    private val colSituacao = MARGEM + CONTEUDO * 0.76f

    private fun cabecalhoDaSituacao() = Bloco(16f) { y ->
        drawText("PEÇA", MARGEM, y + 9f, rotulo)
        drawText("ÚLTIMA TROCA", colUltima, y + 9f, rotulo)
        drawText("PRÓXIMA", colProxima, y + 9f, rotulo)
        drawText("SITUAÇÃO", colSituacao, y + 9f, rotulo)
    }

    private fun linhaDaSituacao(rec: Recomendacao) = Bloco(20f) { y ->
        val base = y + 13f
        drawText(caber(rec.pecaNome, texto, colUltima - MARGEM - 8f), MARGEM, base, texto)
        drawText(rec.kmUltimaTroca?.let(::formatarKm) ?: "—", colUltima, base, texto)
        drawText(rec.kmProximaTroca?.let(::formatarKm) ?: "—", colProxima, base, texto)
        val restante = rec.kmRestante ?: 0
        val (cor, situacao) = when (rec.statusTroca) {
            StatusTroca.VENCIDA -> COR_VENCIDA to (if (restante < 0) "vencida · ${formatarKm(-restante)}" else "vence agora")
            StatusTroca.PERTO -> COR_PERTO to "faltam ${formatarKm(restante)}"
            else -> COR_OK to "em dia"
        }
        preenchido.color = cor
        drawCircle(colSituacao + 3.5f, base - 3.5f, 3.5f, preenchido)
        drawText(caber(situacao, texto, LARGURA - MARGEM - colSituacao - 12f), colSituacao + 12f, base, texto.comCor(cor))
        preenchido.color = LINHA
        drawRect(MARGEM, y + 19.4f, LARGURA - MARGEM, y + 20f, preenchido)
    }

    private fun itemDoHistorico(item: ItemDoHistorico): Bloco {
        val xMeio = MARGEM + 88f
        val xValor = LARGURA - MARGEM
        val larguraMeio = xValor - 80f - xMeio
        val linhasTitulo = quebrar(item.titulo, textoForte, larguraMeio)
        val detalhe = when (item.origem) {
            OrigemItem.OFICINA -> "Oficina" + (item.local?.let { " · $it" } ?: "")
            OrigemItem.POR_CONTA -> "Troca de peça por conta própria"
        }
        val linhasPecas = item.pecas.map { p -> "•  ${p.nome}" + if (p.preco > 0) " — ${formatarReais(p.preco)}" else "" }
            .flatMap { quebrar(it, miudo, larguraMeio) }
        val altura = 10f + linhasTitulo.size * 13f + 12f + linhasPecas.size * 11.5f + 10f
        return Bloco(altura) { y ->
            var yy = y + 10f + 9f
            // coluna da esquerda: dia e km
            drawText(item.data?.let(::formatarData) ?: "dia não informado", MARGEM, yy, if (item.data != null) texto else miudo)
            drawText(formatarKm(item.km), MARGEM, yy + 12f, miudo)
            // meio: o que foi feito
            linhasTitulo.forEach { drawText(it, xMeio, yy, textoForte); yy += 13f }
            // marcador de origem: quadradinho na cor da moto (oficina) ou cinza (por conta)
            preenchido.color = if (item.origem == OrigemItem.OFICINA) destaque else APAGADO
            drawRect(xMeio - 10f, y + 12.5f, xMeio - 5f, y + 17.5f, preenchido)
            drawText(detalhe, xMeio, yy - 1f, miudo); yy += 12f
            linhasPecas.forEach { drawText(it, xMeio + 4f, yy - 1f, miudo); yy += 11.5f }
            // direita: valor
            drawText(if (item.valor > 0) formatarReais(item.valor) else "—", xValor, y + 19f, valorDireita)
            preenchido.color = LINHA
            drawRect(MARGEM, y + altura - 0.6f, LARGURA - MARGEM, y + altura, preenchido)
        }
    }

    private fun linhaDeTexto(t: String, p: Paint) = Bloco(20f) { y -> drawText(t, MARGEM, y + 14f, p) }

    // ── utilidades de desenho ──

    private fun desenharFoto(c: Canvas, bmp: Bitmap, destino: RectF) {
        // corta a foto pra preencher o retângulo (como ContentScale.Crop), com cantos arredondados
        val escala = maxOf(destino.width() / bmp.width, destino.height() / bmp.height)
        val w = destino.width() / escala
        val h = destino.height() / escala
        val origem = android.graphics.Rect(((bmp.width - w) / 2).toInt(), ((bmp.height - h) / 2).toInt(), ((bmp.width + w) / 2).toInt(), ((bmp.height + h) / 2).toInt())
        c.save()
        c.clipPath(Path().apply { addRoundRect(destino, 10f, 10f, Path.Direction.CW) })
        c.drawBitmap(bmp, origem, destino, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        c.restore()
    }

    private fun Paint.comCor(cor: Int) = Paint(this).apply { color = cor }

    // corta com "…" o que não cabe numa linha
    private fun caber(t: String, p: Paint, largura: Float): String {
        if (p.measureText(t) <= largura) return t
        var fim = t.length
        while (fim > 1 && p.measureText(t, 0, fim) + p.measureText("…") > largura) fim--
        return t.substring(0, fim).trimEnd() + "…"
    }

    // quebra em linhas pela largura (palavra inteira; palavra gigante é cortada)
    private fun quebrar(t: String, p: Paint, largura: Float): List<String> {
        val linhas = mutableListOf<String>()
        var atual = ""
        t.split(' ').forEach { palavra ->
            val tentativa = if (atual.isEmpty()) palavra else "$atual $palavra"
            if (p.measureText(tentativa) <= largura || atual.isEmpty()) atual = tentativa
            else { linhas += atual; atual = palavra }
        }
        if (atual.isNotEmpty()) linhas += atual
        return linhas.map { caber(it, p, largura) }.ifEmpty { listOf("") }
    }

    private fun escurecer(cor: Int, fator: Float): Int {
        val a = cor ushr 24 and 0xFF
        val r = ((cor shr 16 and 0xFF) * fator).toInt()
        val g = ((cor shr 8 and 0xFF) * fator).toInt()
        val b = ((cor and 0xFF) * fator).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
