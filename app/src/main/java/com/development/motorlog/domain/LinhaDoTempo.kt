package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Desejo
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Passeio
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico

// A "biografia" da moto: tudo o que o app guarda dela numa história só, do mais recente pro dia
// em que ela chegou — chegada, fotos, trocas, visitas à oficina, cuidados e marcos. Abastecimento
// fica de fora (seria um a cada dois dias: vira ruído na história). Registro sem dia conhecido
// (data 0, anterior à v11) também fica de fora: não tem onde entrar na linha.

enum class TipoEvento { CHEGADA, FOTO, TROCA, VISITA, CUIDADO, MARCO_KM, ANIVERSARIO, DESPEDIDA, PERSONALIZACAO, ROLE }

data class EventoDaHistoria(
    val data: Long,
    val km: Int?,
    val tipo: TipoEvento,
    val titulo: String,
    val detalhe: String? = null,
    // arquivo do álbum (FOTO)
    val foto: String? = null,
    // TipoCuidado (CUIDADO) ou nome da peça (TROCA), pra UI escolher o ícone
    val referencia: String? = null,
)

fun montarLinhaDoTempo(
    moto: Moto,
    pecas: List<Peca>,
    registros: List<Registro>,
    servicos: List<Servico>,
    fotos: List<FotoMoto>,
    cuidados: List<Cuidado>,
    marcos: List<Marco>,
    desejos: List<Desejo> = emptyList(),
    roles: List<Passeio> = emptyList(),
): List<EventoDaHistoria> {
    val nome = nomeDaMoto(moto)
    val nomePeca = pecas.associate { it.id to it.nome }
    val pecasPorServico = registros.filter { it.servicoId != null }.groupBy { it.servicoId }
    val eventos = mutableListOf<EventoDaHistoria>()

    if (moto.chegouEm > 0) {
        eventos += EventoDaHistoria(moto.chegouEm, moto.kmChegada.takeIf { it >= 0 }, TipoEvento.CHEGADA, "A $nome chegou")
    }
    fotos.filter { it.data > 0 }.forEach { f ->
        eventos += EventoDaHistoria(f.data, f.km, TipoEvento.FOTO, f.legenda.ifBlank { "Foto da $nome" }, foto = f.arquivo)
    }
    registros.filter { it.servicoId == null && it.data > 0 }.forEach { r ->
        val peca = nomePeca[r.pecaId] ?: "Peça #${r.pecaId}"
        eventos += EventoDaHistoria(r.data, r.kmTroca, TipoEvento.TROCA, "Troquei: $peca", referencia = peca)
    }
    servicos.filter { it.data > 0 }.forEach { s ->
        val qtd = pecasPorServico[s.id].orEmpty().size
        val detalhe = listOfNotNull(
            s.local.ifBlank { null },
            if (qtd > 0) "$qtd peça${if (qtd == 1) "" else "s"} trocada${if (qtd == 1) "" else "s"}" else null,
        ).joinToString(" · ").ifBlank { null }
        eventos += EventoDaHistoria(s.data, s.kilometragem, TipoEvento.VISITA, "Oficina: ${s.tipoServico}", detalhe)
    }
    cuidados.filter { it.data > 0 }.forEach { c ->
        val tipo = TipoCuidado.doCodigo(c.tipo)
        eventos += EventoDaHistoria(c.data, c.km, TipoEvento.CUIDADO, tipo?.acao ?: c.tipo, c.nota.ifBlank { null }, referencia = c.tipo)
    }
    marcos.forEach { m ->
        val tipo = if (m.tipo == TipoMarco.KM) TipoEvento.MARCO_KM else TipoEvento.ANIVERSARIO
        eventos += EventoDaHistoria(m.data, if (m.tipo == TipoMarco.KM) m.valor else null, tipo, m.titulo)
    }
    // o que saiu da lista de desejos e foi pra moto
    desejos.filter(::estaInstalado).forEach { d ->
        val detalhe = listOfNotNull(d.preco.takeIf { it > 0 }?.let { "R$ ${reaisParaTexto(it)}" }, d.nota.ifBlank { null }).joinToString(" · ").ifBlank { null }
        eventos += EventoDaHistoria(d.instaladoEm, d.kmInstalado.takeIf { it >= 0 }, TipoEvento.PERSONALIZACAO, "Instalei: ${d.nome}", detalhe)
    }
    // rolês e viagens, com a foto escolhida do álbum
    val fotoPorId = fotos.associateBy { it.id }
    roles.filter { it.data > 0 }.forEach { r ->
        eventos += EventoDaHistoria(r.data, r.kmSaida, TipoEvento.ROLE, "Rolê: ${r.destino}", detalheDoRole(r, ::kmComMilhar), foto = fotoPorId[r.fotoId]?.arquivo)
    }
    if (estaVendida(moto)) {
        eventos += EventoDaHistoria(moto.vendidaEm, moto.kilometragem, TipoEvento.DESPEDIDA, "A $nome passou adiante")
    }
    // mais recente primeiro; no mesmo dia, o maior km por cima (a despedida sempre no topo e a
    // chegada sempre por último)
    return eventos.sortedWith(
        compareByDescending<EventoDaHistoria> { it.data }
            .thenBy { when (it.tipo) { TipoEvento.DESPEDIDA -> -1; TipoEvento.CHEGADA -> 1; else -> 0 } }
            .thenByDescending { it.km ?: -1 },
    )
}

private val milharDaHistoria = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("pt-BR"))
private fun kmComMilhar(km: Int) = "${milharDaHistoria.format(km)} km"
