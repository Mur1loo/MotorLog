package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Desejo
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Passeio
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import java.time.LocalDate

// Retrospectiva do ano: "Seu 2026 com a Pretinha" — km rodados, rolês, cuidados, manutenção,
// combustível, fotos e o que foi instalado, num resumo pra rever e compartilhar. Momento anual de
// orgulho (os apps de esporte mostram o quanto isso é valorizado) — e aqui sem cobrar nada.
// Tudo a partir do que o app já guarda; datas em meia-noite UTC, como em todo o app.

data class DadosDaMoto(
    val moto: Moto,
    val historicoKm: List<HistoricoKm> = emptyList(),
    val registros: List<Registro> = emptyList(),
    val servicos: List<Servico> = emptyList(),
    val abastecimentos: List<Abastecimento> = emptyList(),
    val cuidados: List<Cuidado> = emptyList(),
    val fotos: List<FotoMoto> = emptyList(),
    val desejos: List<Desejo> = emptyList(),
    val roles: List<Passeio> = emptyList(),
)

data class Retrospectiva(
    val ano: Int,
    val kmRodados: Int?,          // null = sem registros de km suficientes no ano
    val trocas: Int,              // trocas de peça (avulsas e de visitas)
    val visitas: Int,             // visitas à oficina
    val gastoManutencao: Int,     // centavos: oficina + peças trocadas por conta própria
    val abastecimentos: Int,
    val mililitros: Int,
    val gastoCombustivel: Int,    // centavos
    val kmPorLitro: Double?,      // consumo dos abastecimentos do ano
    val cuidados: Int,
    val lavagens: Int,
    val fotos: List<FotoMoto>,    // fotos do ano, as com legenda primeiro
    val roles: List<Passeio>,
    val kmEmRoles: Int,
    val instalados: List<Desejo>,
    val marcos: List<Marco>,
) {
    // ano sem nada registrado: não tem o que contar
    val vazia get() = (kmRodados ?: 0) == 0 && trocas == 0 && visitas == 0 && abastecimentos == 0 && cuidados == 0 &&
        fotos.isEmpty() && roles.isEmpty() && instalados.isEmpty()
}

private fun inicioDoAno(ano: Int) = LocalDate.of(ano, 1, 1).toEpochDay() * MILLIS_POR_DIA
fun anoDe(data: Long): Int = LocalDate.ofEpochDay(data / MILLIS_POR_DIA).year

// km rodados no ano: do último km antes de 1º de janeiro (ou o primeiro do ano, ou o da chegada
// se ela chegou no ano) até o último km do ano
fun kmRodadosNoAno(pontos: List<HistoricoKm>, ano: Int, chegouEm: Long = 0, kmChegada: Int = -1): Int? {
    val inicio = inicioDoAno(ano)
    val fim = inicioDoAno(ano + 1)
    val ordenados = pontos.sortedWith(compareBy({ it.data }, { it.id }))
    val doAno = ordenados.filter { it.data in inicio until fim }
    val ultimo = doAno.lastOrNull() ?: return null
    val partida = ordenados.lastOrNull { it.data < inicio }?.km
        ?: kmChegada.takeIf { it >= 0 && chegouEm in inicio until fim }
        ?: doAno.first().km
    return (ultimo.km - partida).takeIf { it >= 0 }
}

fun montarRetrospectiva(ano: Int, d: DadosDaMoto): Retrospectiva {
    val inicio = inicioDoAno(ano)
    val fim = inicioDoAno(ano + 1)
    fun noAno(data: Long) = data in inicio until fim
    val servicos = d.servicos.filter { noAno(it.data) }
    val idsDosServicos = servicos.map { it.id }.toSet()
    // troca avulsa conta pelo dia dela; a de uma visita, pelo dia da visita
    val trocas = d.registros.filter { if (it.servicoId == null) noAno(it.data) else it.servicoId in idsDosServicos }
    val avulsas = trocas.filter { it.servicoId == null }
    val abastecimentos = d.abastecimentos.filter { noAno(it.data) }
    val cuidados = d.cuidados.filter { noAno(it.data) }
    val lavagens = setOf(TipoCuidado.LAVAGEM.codigo, TipoCuidado.LAVAGEM_DETALHADA.codigo, TipoCuidado.CERA.codigo)
    val roles = d.roles.filter { noAno(it.data) }.sortedBy { it.data }
    val fimDoAno = minOf(fim - MILLIS_POR_DIA, fimDaHistoria(d.moto, Long.MAX_VALUE))
    return Retrospectiva(
        ano = ano,
        kmRodados = kmRodadosNoAno(d.historicoKm, ano, d.moto.chegouEm, d.moto.kmChegada),
        trocas = trocas.size,
        visitas = servicos.size,
        gastoManutencao = servicos.sumOf { it.custo } + avulsas.sumOf { it.preco },
        abastecimentos = abastecimentos.size,
        mililitros = abastecimentos.sumOf { it.mililitros },
        gastoCombustivel = abastecimentos.sumOf { it.valor },
        kmPorLitro = estimarConsumo(abastecimentos)?.kmPorLitro,
        cuidados = cuidados.size,
        lavagens = cuidados.count { it.tipo in lavagens },
        fotos = d.fotos.filter { noAno(it.data) }.sortedWith(compareBy<FotoMoto> { it.legenda.isBlank() }.thenByDescending { it.data }),
        roles = roles,
        kmEmRoles = roles.sumOf { kmDoRole(it) ?: 0 },
        instalados = d.desejos.filter { estaInstalado(it) && noAno(it.instaladoEm) }.sortedBy { it.instaladoEm },
        marcos = marcosDaMoto(nomeDaMoto(d.moto), d.moto.chegouEm, d.historicoKm, fimDoAno).filter { noAno(it.data) },
    )
}

// A retrospectiva aparece sozinha no Painel em dezembro (o ano que está acabando) e em janeiro
// (o ano que passou). Fora disso, fica na linha do tempo. null = não é época.
fun anoDaRetrospectiva(hoje: Long): Int? {
    val dia = LocalDate.ofEpochDay(hoje / MILLIS_POR_DIA)
    return when (dia.monthValue) {
        12 -> dia.year
        1 -> dia.year - 1
        else -> null
    }
}

// anos com alguma coisa registrada, do mais recente pro mais antigo (os chips da tela)
fun anosComHistoria(d: DadosDaMoto, hoje: Long): List<Int> {
    val datas = buildList {
        if (d.moto.chegouEm > 0) add(d.moto.chegouEm)
        addAll(d.historicoKm.map { it.data }); addAll(d.servicos.map { it.data }); addAll(d.registros.map { it.data })
        addAll(d.abastecimentos.map { it.data }); addAll(d.cuidados.map { it.data }); addAll(d.fotos.map { it.data })
        addAll(d.roles.map { it.data }); addAll(d.desejos.map { it.instaladoEm })
    }.filter { it in 1..hoje }
    return datas.map(::anoDe).distinct().sortedDescending()
}

// quadros do cartão pra compartilhar: só os que têm o que mostrar, no máximo 6 (2 × 3)
enum class QuadroDaRetrospectiva { KM, ROLES, MANUTENCAO, CUIDADOS, CONSUMO, FOTOS, INSTALADOS }

fun quadrosDaRetrospectiva(r: Retrospectiva): List<QuadroDaRetrospectiva> = buildList {
    if ((r.kmRodados ?: 0) > 0) add(QuadroDaRetrospectiva.KM)
    if (r.roles.isNotEmpty()) add(QuadroDaRetrospectiva.ROLES)
    if (r.trocas + r.visitas > 0) add(QuadroDaRetrospectiva.MANUTENCAO)
    if (r.cuidados > 0) add(QuadroDaRetrospectiva.CUIDADOS)
    if (r.kmPorLitro != null) add(QuadroDaRetrospectiva.CONSUMO)
    if (r.fotos.isNotEmpty()) add(QuadroDaRetrospectiva.FOTOS)
    if (r.instalados.isNotEmpty()) add(QuadroDaRetrospectiva.INSTALADOS)
}.take(6)
