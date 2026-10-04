package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico

// Índice de cuidado: uma nota de 0 a 100 pra "o quanto essa moto é bem cuidada", juntando o que
// o app já sabe. É orgulho pro dono e argumento na revenda (vai no PDF). Média ponderada de 5
// partes; parte sem dado nenhum (ex.: revisão não configurada) sai da conta e os pesos se
// redistribuem. Sem histórico nenhum ainda, não há nota (não dá 10/100 pra quem acabou de chegar).
//
//  Trocas em dia (35)          peças registradas: em dia 100, perto 80, vencida 60 → 0 conforme o atraso
//  Manutenção registrada (25)  metade recência (última troca/visita) + metade cobertura das peças
//                              essenciais (óleo, filtro, relação, pastilhas, pneus) com registro
//  Revisão (15)                só com intervalo de revisão configurado
//  Cuidados de rotina (15)     corrente, calibragem e lavagem em dia
//  Km em dia (10)              há quantos dias o km foi atualizado

data class ParteDoIndice(val nome: String, val nota: Int, val peso: Int, val dica: String?)

data class IndiceDeCuidado(val nota: Int, val faixa: String, val partes: List<ParteDoIndice>, val dica: String?)

// as peças que todo motoboy troca: a cobertura delas mostra se a manutenção está sendo registrada
val PECAS_ESSENCIAIS = PECAS_DO_DIA_A_DIA.take(7)

fun faixaDoIndice(nota: Int): String = when {
    nota >= 85 -> "Excelente"
    nota >= 70 -> "Bem cuidada"
    nota >= 50 -> "Pede atenção"
    else -> "Precisa de cuidado"
}

// nota de uma recomendação: vencida perde pontos conforme o atraso (meio intervalo atrasado = 0)
private fun notaDaRecomendacao(r: Recomendacao): Int = when (r.statusTroca) {
    StatusTroca.OK -> 100
    StatusTroca.PERTO -> 80
    StatusTroca.NUNCA_TROCADA -> 0
    StatusTroca.VENCIDA -> {
        val intervalo = (r.kmProximaTroca ?: 0) - (r.kmUltimaTroca ?: 0)
        val atraso = -(r.kmRestante ?: 0)
        if (intervalo <= 0) 30 else (60 * (1 - atraso / (intervalo * 0.5))).toInt().coerceIn(0, 60)
    }
}

private fun notaPorDias(dias: Int?, faixas: List<Pair<Int, Int>>, semRegistro: Int): Int =
    if (dias == null) semRegistro else faixas.firstOrNull { dias <= it.first }?.second ?: faixas.last().second

fun calcularIndiceDeCuidado(
    recomendacoes: List<Recomendacao>,
    pecas: List<Peca>,
    registros: List<Registro>,
    servicos: List<Servico>,
    cuidados: List<Cuidado>,
    kmAtual: Int,
    kmAtualizadoEm: Long,
    hoje: Long,
): IndiceDeCuidado? {
    if (registros.isEmpty() && servicos.isEmpty() && cuidados.isEmpty()) return null
    val partes = mutableListOf<ParteDoIndice>()

    // 1. trocas em dia (só as peças com registro: "nunca trocada" entra na cobertura, abaixo)
    val pecasComRegistro = recomendacoes.filter { !it.ehRevisao && it.statusTroca != StatusTroca.NUNCA_TROCADA }
    if (pecasComRegistro.isNotEmpty()) {
        val vencidas = pecasComRegistro.filter { it.statusTroca == StatusTroca.VENCIDA }
        partes += ParteDoIndice(
            "Trocas em dia", pecasComRegistro.map(::notaDaRecomendacao).average().toInt(), 35,
            when {
                vencidas.size == 1 -> "trocar ${vencidas[0].pecaNome.lowercase()} (vencida)"
                vencidas.size > 1 -> "fazer as ${vencidas.size} trocas vencidas"
                else -> null
            },
        )
    }

    // 2. manutenção registrada: recência + cobertura das essenciais
    val datas = registros.map { it.data }.filter { it > 0 } + servicos.map { it.data }.filter { it > 0 }
    val diasDesdeUltima = datas.maxOrNull()?.let { diasEntre(it, hoje).coerceAtLeast(0) }
    val recencia = when {
        registros.isEmpty() && servicos.isEmpty() -> 0
        else -> notaPorDias(diasDesdeUltima, listOf(90 to 100, 180 to 70, 365 to 40, Int.MAX_VALUE to 15), semRegistro = 15)
    }
    val idsComRegistro = registros.map { it.pecaId }.toSet()
    val essenciais = PECAS_ESSENCIAIS.mapNotNull { nome -> pecas.find { it.nome.equals(nome, ignoreCase = true) } }
    val faltando = essenciais.filter { it.id !in idsComRegistro }
    val cobertura = if (essenciais.isEmpty()) 100 else (essenciais.size - faltando.size) * 100 / essenciais.size
    partes += ParteDoIndice(
        "Manutenção registrada", (recencia + cobertura) / 2, 25,
        when {
            faltando.isNotEmpty() -> "registrar a última troca de ${faltando[0].nome.lowercase()}"
            recencia < 70 -> "registrar a próxima visita à oficina"
            else -> null
        },
    )

    // 3. revisão (só quando configurada: a recomendação de revisão existe)
    recomendacoes.firstOrNull { it.ehRevisao }?.let { rev ->
        partes += ParteDoIndice(
            "Revisão", notaDaRecomendacao(rev), 15,
            when (rev.statusTroca) {
                StatusTroca.VENCIDA -> "fazer a revisão (vencida)"
                StatusTroca.NUNCA_TROCADA -> "registrar a última revisão em \"Fui à oficina\""
                else -> null
            },
        )
    }

    // 4. cuidados de rotina: corrente, calibragem, lavagem
    val situacoes = situacaoDosCuidados(cuidados, kmAtual, hoje).associateBy { it.tipo }
    fun notaRotina(s: SituacaoCuidado?) = when {
        s?.ultimo == null -> 0
        s.jaEstaNaHora -> 40
        else -> 100
    }
    val lavagens = setOf(TipoCuidado.LAVAGEM.codigo, TipoCuidado.LAVAGEM_DETALHADA.codigo, TipoCuidado.CERA.codigo)
    val diasDesdeLavagem = cuidados.filter { it.tipo in lavagens && it.data > 0 }.maxOfOrNull { it.data }?.let { diasEntre(it, hoje).coerceAtLeast(0) }
    val notaLavagem = notaPorDias(diasDesdeLavagem, listOf(30 to 100, 60 to 60, Int.MAX_VALUE to 30), semRegistro = 0)
    val corrente = situacoes[TipoCuidado.CORRENTE]
    val calibragem = situacoes[TipoCuidado.CALIBRAGEM]
    partes += ParteDoIndice(
        "Cuidados de rotina", (notaRotina(corrente) + notaRotina(calibragem) + notaLavagem) / 3, 15,
        when {
            corrente?.jaEstaNaHora == true -> "lubrificar a corrente"
            calibragem?.jaEstaNaHora == true -> "calibrar os pneus"
            notaLavagem < 60 -> "dar uma lavada na moto"
            else -> null
        },
    )

    // 5. km em dia
    val diasSemKm = if (kmAtualizadoEm > 0) diasEntre(kmAtualizadoEm, hoje).coerceAtLeast(0) else null
    val notaKm = notaPorDias(diasSemKm, listOf(3 to 100, 7 to 70, 14 to 40, Int.MAX_VALUE to 10), semRegistro = 50)
    partes += ParteDoIndice("Km em dia", notaKm, 10, if (notaKm < 70) "atualizar o km" else null)

    val pesoTotal = partes.sumOf { it.peso }
    val nota = (partes.sumOf { it.nota * it.peso }.toDouble() / pesoTotal).toInt().coerceIn(0, 100)
    // a dica que mais sobe a nota: a parte que mais perde pontos (peso × o que falta pra 100)
    val dica = partes.filter { it.dica != null }.maxByOrNull { it.peso * (100 - it.nota) }?.dica
    return IndiceDeCuidado(nota, faixaDoIndice(nota), partes, dica)
}
