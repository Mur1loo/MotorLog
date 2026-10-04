package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico

// O conteúdo do "histórico da moto" em PDF — o que o dono mostra pro comprador, pro mecânico ou
// guarda pra si. Puro: decide O QUE entra e em que ordem; desenhar a página é com a camada Android.

enum class OrigemItem { OFICINA, POR_CONTA }

data class PecaDoItem(val nome: String, val preco: Int)

data class ItemDoHistorico(
    val origem: OrigemItem,
    val titulo: String,          // tipo do serviço ou nome da peça trocada
    val local: String?,          // oficina (null na troca por conta própria)
    val km: Int,
    val data: Long?,             // null = dia desconhecido
    val valor: Int,
    val pecas: List<PecaDoItem>, // peças trocadas na visita (vazio na troca por conta)
)

data class RelatorioMoto(
    val moto: Moto,
    val itens: List<ItemDoHistorico>,
    val situacao: List<Recomendacao>,   // só o que já tem registro, do mais urgente pro menos
    val gastoOficina: Int,
    val visitas: Int,
    val gastoPorConta: Int,
    val trocasPorConta: Int,
    val custoPorKm: Double?,
    val indice: IndiceDeCuidado? = null,   // índice de cuidado (domain/IndiceDeCuidado.kt), quando dá pra calcular
) {
    val gastoTotal get() = gastoOficina + gastoPorConta
}

fun montarRelatorio(
    moto: Moto,
    pecas: List<Peca>,
    registros: List<Registro>,
    servicos: List<Servico>,
    recomendacoes: List<Recomendacao>,
    kmRodados: Int,
): RelatorioMoto {
    val nomePeca = pecas.associate { it.id to it.nome }
    val ordemAlfabetica = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("pt-BR"))   // "Óleo" antes de "Vela"
    fun nome(r: Registro) = nomePeca[r.pecaId] ?: "Peça #${r.pecaId}"
    val doServico = registros.filter { it.servicoId != null }.groupBy { it.servicoId }
    val avulsas = registros.filter { it.servicoId == null }

    val itens = servicos.map { s ->
        ItemDoHistorico(
            origem = OrigemItem.OFICINA,
            titulo = s.tipoServico,
            local = s.local.ifBlank { null },
            km = s.kilometragem,
            data = s.data.takeIf { it > 0 },
            valor = s.custo,
            pecas = doServico[s.id].orEmpty().sortedWith(compareBy(ordemAlfabetica) { nome(it) }).map { PecaDoItem(nome(it), it.preco) },
        )
    } + avulsas.map { r ->
        ItemDoHistorico(OrigemItem.POR_CONTA, nome(r), null, r.kmTroca, r.data.takeIf { it > 0 }, r.preco, emptyList())
    }

    val gastoOficina = servicos.sumOf { it.custo }
    val gastoPorConta = avulsas.sumOf { it.preco }
    return RelatorioMoto(
        moto = moto,
        // mais recente primeiro, como no Histórico do app (km é o eixo; data desempata)
        itens = itens.sortedWith(compareByDescending<ItemDoHistorico> { it.km }.thenByDescending { it.data ?: 0L }),
        situacao = recomendacoes.filter { it.statusTroca != StatusTroca.NUNCA_TROCADA },
        gastoOficina = gastoOficina,
        visitas = servicos.size,
        gastoPorConta = gastoPorConta,
        trocasPorConta = avulsas.size,
        custoPorKm = custoPorKm(gastoOficina + gastoPorConta, kmRodados),
    )
}

// "MotorLog_Crosser_ABC1D23.pdf" — sem acento, espaço nem símbolo (alguns apps de mensagem estranham)
fun nomeDoArquivoPdf(moto: Moto): String {
    val base = "${moto.modelo} ${moto.placa}"
    val limpo = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
    return "MotorLog_${limpo.ifBlank { "moto" }}.pdf"
}
