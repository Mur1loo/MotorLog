package com.development.motorlog.domain

const val MILLIS_POR_DIA = 86_400_000L

// Dias inteiros entre dois instantes em millis (meia-noite UTC → meia-noite UTC). Negativo se 'ate' < 'de'.
fun diasEntre(de: Long, ate: Long): Int = ((ate - de) / MILLIS_POR_DIA).toInt()

// Resumo do que exige atenção numa moto: base dos alertas da Garagem e da notificação.
data class ResumoAlertas(val vencidas: Int, val perto: Int) {
    val temAlerta: Boolean get() = vencidas > 0 || perto > 0
}

fun resumirAlertas(recomendacoes: List<Recomendacao>) = ResumoAlertas(
    vencidas = recomendacoes.count { it.statusTroca == StatusTroca.VENCIDA },
    perto = recomendacoes.count { it.statusTroca == StatusTroca.PERTO },
)

// Depois de quantos dias sem atualizar o km vale lembrar (motoboy roda todo dia; 3 dias = ~500 km sem registro)
const val DIAS_PARA_LEMBRAR_KM = 3

// Texto da notificação diária, ou null se não há motivo pra incomodar.
// kmAtualizadoEm = 0 significa "nunca registrado" (moto anterior à v9): não cobra atualização.
fun montarLembrete(
    modelo: String,
    kmAtualizadoEm: Long,
    hoje: Long,
    recomendacoes: List<Recomendacao>,
): String? {
    val alertas = resumirAlertas(recomendacoes)
    val diasSemKm = if (kmAtualizadoEm > 0) diasEntre(kmAtualizadoEm, hoje) else null
    val partes = buildList {
        if (alertas.vencidas > 0) {
            val nomes = recomendacoes.filter { it.statusTroca == StatusTroca.VENCIDA }.map { it.pecaNome }
            add(if (nomes.size == 1) "${nomes[0]} vencido" else "${nomes.size} trocas vencidas (${nomes.take(2).joinToString()}…)".replace("…)", if (nomes.size > 2) "…)" else ")"))
        }
        if (alertas.perto > 0) add(if (alertas.perto == 1) "1 troca perto de vencer" else "${alertas.perto} trocas perto de vencer")
        if (diasSemKm != null && diasSemKm >= DIAS_PARA_LEMBRAR_KM) add("faz $diasSemKm dias que o km não é atualizado")
    }
    if (partes.isEmpty()) return null
    return "$modelo: " + partes.joinToString(" · ")
}
