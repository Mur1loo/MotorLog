package com.development.motorlog.domain

import com.development.motorlog.data.Cuidado

// Diário de cuidados: os rituais de quem trata bem a moto. Lavar e encerar são carinho (sem
// cobrança); lubrificar a corrente e calibrar os pneus também são segurança, então ganham um
// "já está na hora" suave no card — sem notificação (os lembretes são pras trocas).

enum class TipoCuidado(val codigo: String, val acao: String, val nome: String) {
    LAVAGEM("lavagem", "Lavei", "Lavagem"),
    CERA("cera", "Encerei", "Cera"),
    CORRENTE("corrente", "Lubrifiquei a corrente", "Corrente lubrificada"),
    CALIBRAGEM("calibragem", "Calibrei os pneus", "Pneus calibrados");

    companion object {
        fun doCodigo(codigo: String): TipoCuidado? = entries.find { it.codigo == codigo }
    }
}

// Lubrificação da corrente: a cada ~500 km pra quem roda todo dia na rua (poeira, chuva).
// Calibragem: semanal, com os pneus frios.
const val CORRENTE_A_CADA_KM = 500
const val CALIBRAGEM_A_CADA_DIAS = 7

data class SituacaoCuidado(
    val tipo: TipoCuidado,
    val ultimo: Cuidado?,
    // "hoje", "há 12 dias", "há 340 km", "ainda não registrado"
    val quando: String,
    // corrente ou calibragem passaram do ponto: o card mostra em amarelo
    val jaEstaNaHora: Boolean,
)

fun situacaoDosCuidados(cuidados: List<Cuidado>, kmAtual: Int, hoje: Long): List<SituacaoCuidado> =
    TipoCuidado.entries.map { tipo ->
        val ultimo = cuidados.filter { it.tipo == tipo.codigo }.maxWithOrNull(compareBy({ it.data }, { it.id }))
        if (ultimo == null) {
            SituacaoCuidado(tipo, null, "ainda não registrado", jaEstaNaHora = tipo == TipoCuidado.CORRENTE || tipo == TipoCuidado.CALIBRAGEM)
        } else {
            val dias = diasEntre(ultimo.data, hoje).coerceAtLeast(0)
            val km = (kmAtual - ultimo.km).coerceAtLeast(0)
            when (tipo) {
                TipoCuidado.CORRENTE -> SituacaoCuidado(tipo, ultimo, if (km == 0) "agora há pouco" else "há $km km", km >= CORRENTE_A_CADA_KM)
                TipoCuidado.CALIBRAGEM -> SituacaoCuidado(tipo, ultimo, descreverDiasAtras(dias), dias >= CALIBRAGEM_A_CADA_DIAS)
                else -> SituacaoCuidado(tipo, ultimo, descreverDiasAtras(dias), jaEstaNaHora = false)
            }
        }
    }

// 0 → "hoje", 1 → "ontem", n → "há n dias"
fun descreverDiasAtras(dias: Int): String = when (dias) {
    0 -> "hoje"
    1 -> "ontem"
    else -> "há $dias dias"
}

// O mesmo cuidado duas vezes no mesmo dia é toque repetido, não um novo registro
fun jaRegistradoHoje(cuidados: List<Cuidado>, tipo: TipoCuidado, hoje: Long): Boolean =
    cuidados.any { it.tipo == tipo.codigo && it.data == hoje }
