package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento

// Consumo pelo método "tanque cheio a tanque cheio": a bomba diz quantos litros ENTRARAM, então só
// se sabe quanto a moto gastou entre dois tanques cheios — os litros do 2º (mais os parciais no
// meio) são exatamente o que foi queimado naqueles km. Ex.: cheio aos 16.000, cheio aos 16.300
// com 7,5 L → 300 ÷ 7,5 = 40 km/l. Parcial antes do 1º tanque cheio não entra (não há ponto de
// partida). Trecho fora de 5–100 km/l é erro de registro (quase sempre um abastecimento
// esquecido no meio) e fica fora da média.

const val KM_POR_LITRO_MINIMO = 5.0
const val KM_POR_LITRO_MAXIMO = 100.0

// valor = soma em centavos dos abastecimentos do trecho; null se algum deles não tem valor
data class TrechoDeConsumo(val kmInicio: Int, val kmFim: Int, val mililitros: Int, val valor: Int?) {
    val km get() = kmFim - kmInicio
    val kmPorLitro get() = km / (mililitros / 1000.0)
}

fun calcularTrechos(abastecimentos: List<Abastecimento>): List<TrechoDeConsumo> {
    val trechos = mutableListOf<TrechoDeConsumo>()
    var inicio: Int? = null
    var mililitros = 0
    var valor: Int? = 0
    abastecimentos.sortedWith(compareBy({ it.km }, { it.data }, { it.id })).forEach { a ->
        val kmInicio = inicio
        if (kmInicio == null) {
            if (a.tanqueCheio) inicio = a.km
            return@forEach
        }
        mililitros += a.mililitros
        valor = valor?.let { v -> if (a.valor > 0) v + a.valor else null }
        if (a.tanqueCheio) {
            val trecho = TrechoDeConsumo(kmInicio, a.km, mililitros, valor)
            if (trecho.km > 0 && mililitros > 0 && trecho.kmPorLitro in KM_POR_LITRO_MINIMO..KM_POR_LITRO_MAXIMO) trechos += trecho
            inicio = a.km
            mililitros = 0
            valor = 0
        }
    }
    return trechos
}

data class ResumoConsumo(
    val ultimoKmPorLitro: Double?,     // do último trecho fechado
    val mediaKmPorLitro: Double?,      // km total ÷ litros totais (não é média das médias)
    val trechos: Int,
    val precoPorLitro: Int?,           // centavos por litro, do abastecimento mais recente com valor
    val custoPorKm: Double?,           // R$ de combustível por km, nos trechos com valor em todos
)

fun resumirConsumo(abastecimentos: List<Abastecimento>): ResumoConsumo {
    val trechos = calcularTrechos(abastecimentos)
    val km = trechos.sumOf { it.km }
    val mililitros = trechos.sumOf { it.mililitros }
    val comValor = trechos.filter { it.valor != null }
    val kmComValor = comValor.sumOf { it.km }
    val ultimoComValor = abastecimentos.filter { it.valor > 0 && it.mililitros > 0 }.maxWithOrNull(compareBy({ it.km }, { it.data }, { it.id }))
    return ResumoConsumo(
        ultimoKmPorLitro = trechos.lastOrNull()?.kmPorLitro,
        mediaKmPorLitro = if (mililitros > 0) km / (mililitros / 1000.0) else null,
        trechos = trechos.size,
        precoPorLitro = ultimoComValor?.let { (it.valor.toLong() * 1000 / it.mililitros).toInt() },
        custoPorKm = if (kmComValor > 0) comValor.sumOf { it.valor!! } / 100.0 / kmComValor else null,
    )
}

// ── litros digitados ──

// Maior abastecimento aceito: 100 L (tanque de moto fica entre 5 e 30)
private const val MILILITROS_MAXIMO = 100_000

// "8", "8,5", "8,734" ou "8.734" (bomba mostra 3 casas) → mililitros. Vírgula ou ponto são decimal:
// ninguém abastece mil litros. Vazio, zero ou ilegível → null.
fun lerLitros(texto: String): Int? {
    val limpo = texto.trim().removeSuffix("L").removeSuffix("l").trim().replace(',', '.')
    if (limpo.isEmpty() || limpo.count { it == '.' } > 1 || limpo.any { !it.isDigit() && it != '.' }) return null
    val partes = limpo.split('.')
    val inteira = partes[0]
    val decimal = partes.getOrElse(1) { "" }
    if (decimal.length > 3 || (inteira.isEmpty() && decimal.isEmpty()) || inteira.length > 3) return null
    val ml = inteira.ifEmpty { "0" }.toInt() * 1000 + decimal.padEnd(3, '0').toInt()
    return ml.takeIf { it in 1..MILILITROS_MAXIMO }
}

// mililitros → texto do campo/da tela: 8734 → "8,734", 8500 → "8,5", 8000 → "8"
fun litrosParaTexto(mililitros: Int): String {
    val inteira = mililitros / 1000
    val decimal = (mililitros % 1000).toString().padStart(3, '0').trimEnd('0')
    return if (decimal.isEmpty()) "$inteira" else "$inteira,$decimal"
}
