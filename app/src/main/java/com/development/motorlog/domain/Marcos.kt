package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import java.time.LocalDate
import java.util.Locale

// Marcos da vida da moto, pra celebrar: "Passou dos 50.000 km!" e "1 ano com a Pretinha".
// Só conta o que aconteceu com o app acompanhando — o km cruzado entre dois registros
// (HistoricoKm), e os aniversários do dia em que ela chegou. Nada de ranking: é a moto
// comparada com ela mesma.

enum class TipoMarco { KM, ANIVERSARIO }

// chave: estável e única por moto (ex.: "km-50000", "aniversario-1"), pra lembrar o que já foi visto
data class Marco(val chave: String, val tipo: TipoMarco, val titulo: String, val data: Long, val valor: Int)

// 5.000, 10.000 e depois de 10 em 10 mil (motoboy roda 3–5 mil por mês: marco todo bimestre)
fun marcosDeKm(ateKm: Int): List<Int> = buildList {
    add(5_000)
    var km = 10_000
    while (km <= ateKm) { add(km); km += 10_000 }
}.filter { it <= ateKm }

private val milhar = java.text.NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"))

fun marcosDaMoto(nome: String, chegouEm: Long, pontos: List<HistoricoKm>, hoje: Long): List<Marco> {
    val marcos = mutableListOf<Marco>()
    // km: cruzado entre dois registros consecutivos (o 1º registro já acima não conta: foi antes do app)
    val ordenados = pontos.sortedWith(compareBy({ it.data }, { it.km }))
    val maior = ordenados.maxOfOrNull { it.km } ?: 0
    marcosDeKm(maior).forEach { limite ->
        val cruzou = ordenados.zipWithNext().firstOrNull { (antes, depois) -> antes.km < limite && depois.km >= limite }
        if (cruzou != null) {
            marcos += Marco("km-$limite", TipoMarco.KM, "Passou dos ${milhar.format(limite)} km!", cruzou.second.data, limite)
        }
    }
    // aniversários da chegada
    if (chegouEm in 1..hoje) {
        val chegada = LocalDate.ofEpochDay(chegouEm / MILLIS_POR_DIA)
        var anos = 1
        while (true) {
            val dia = chegada.plusYears(anos.toLong()).toEpochDay() * MILLIS_POR_DIA
            if (dia > hoje) break
            marcos += Marco("aniversario-$anos", TipoMarco.ANIVERSARIO, "${if (anos == 1) "1 ano" else "$anos anos"} com a $nome", dia, anos)
            anos++
        }
    }
    return marcos.sortedBy { it.data }
}

// O marco que merece a comemoração agora: o mais recente dos últimos 30 dias que ainda não foi
// visto (o dono tocou em "Valeu!"). Marco antigo não aparece do nada depois de meses.
const val DIAS_PRA_COMEMORAR = 30

fun marcoPraComemorar(marcos: List<Marco>, hoje: Long, vistos: Set<String>): Marco? =
    marcos.filter { it.chave !in vistos && diasEntre(it.data, hoje) in 0..DIAS_PRA_COMEMORAR }.maxByOrNull { it.data }
