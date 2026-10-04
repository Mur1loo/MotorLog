package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento

// Consumo (km/l) a partir dos abastecimentos — funcionando com o que a maioria faz: colocar um
// pouco (parcial), sem encher o tanque e sem saber o tamanho dele.
//
// A conta: entre o 1º e o último abastecimento, a moto gastou os litros colocados nos
// abastecimentos DEPOIS do 1º (eles repõem o que foi queimado) mais a diferença de nível do
// tanque entre o 1º e o último. Essa diferença não dá pra saber, mas ela é no máximo um tanque e
// se dilui: quanto mais abastecimentos na conta, menor o erro. Por isso
//     km/l ≈ (km do último − km do 1º) ÷ litros dos abastecimentos depois do 1º
// e o app avisa o quanto confiar ("aproximada" → "boa" conforme os litros somam).
// Quem às vezes completa o tanque ganha um bônus: entre dois tanques cheios o nível é o mesmo
// (cheio), a diferença some e a conta fica EXATA — aí o app usa esse trecho.
// Valor fora de 5–100 km/l é erro de registro (abastecimento esquecido, km errado) e não aparece.

const val KM_POR_LITRO_MINIMO = 5.0
const val KM_POR_LITRO_MAXIMO = 100.0
// pra mostrar a primeira média: 3 abastecimentos (2 intervalos) e 150 km rodados entre eles
const val ABASTECIMENTOS_MINIMOS = 3
const val KM_MINIMO_PRA_MEDIA = 150
// litros na conta a partir dos quais a média aproximada já é "boa" (erro de nível diluído)
const val MILILITROS_PRA_CONFIANCA_BOA = 60_000
// média recente: os últimos abastecimentos (mostra se o consumo mudou)
const val ABASTECIMENTOS_NA_MEDIA_RECENTE = 6

enum class Confianca { APROXIMADA, BOA, EXATA }

data class EstimativaConsumo(val kmPorLitro: Double, val km: Int, val mililitros: Int, val confianca: Confianca)

private val ordem = compareBy<Abastecimento>({ it.km }, { it.data }, { it.id })

fun estimarConsumo(abastecimentos: List<Abastecimento>): EstimativaConsumo? {
    val ord = abastecimentos.sortedWith(ordem)
    // bônus: do 1º ao último tanque cheio a conta é exata (se o trecho for longo o bastante)
    val cheios = ord.indices.filter { ord[it].tanqueCheio }
    val exato = cheios.size >= 2 && ord[cheios.last()].km - ord[cheios.first()].km >= KM_MINIMO_PRA_MEDIA
    val (ini, fim) = if (exato) cheios.first() to cheios.last() else 0 to ord.lastIndex
    if (!exato && ord.size < ABASTECIMENTOS_MINIMOS) return null
    if (fim <= ini) return null
    val km = ord[fim].km - ord[ini].km
    val mililitros = ord.subList(ini + 1, fim + 1).sumOf { it.mililitros }
    if (km < KM_MINIMO_PRA_MEDIA || mililitros <= 0) return null
    val kmPorLitro = km / (mililitros / 1000.0)
    if (kmPorLitro !in KM_POR_LITRO_MINIMO..KM_POR_LITRO_MAXIMO) return null
    val confianca = when {
        exato -> Confianca.EXATA
        mililitros >= MILILITROS_PRA_CONFIANCA_BOA -> Confianca.BOA
        else -> Confianca.APROXIMADA
    }
    return EstimativaConsumo(kmPorLitro, km, mililitros, confianca)
}

data class ResumoConsumo(
    val media: EstimativaConsumo?,
    // últimos ABASTECIMENTOS_NA_MEDIA_RECENTE (só quando há mais que isso: senão é a própria média)
    val recente: EstimativaConsumo?,
    // quantos abastecimentos faltam pra primeira média (0 = já tem, ou falta km rodado)
    val faltamAbastecimentos: Int,
    // centavos por litro, do abastecimento mais recente com valor (vem preenchido no "Abasteci")
    val precoPorLitro: Int?,
    // R$ de combustível por km = preço médio do litro ÷ km/l (vale mesmo se alguns não têm valor)
    val custoPorKm: Double?,
)

fun resumirConsumo(abastecimentos: List<Abastecimento>): ResumoConsumo {
    val media = estimarConsumo(abastecimentos)
    val recente = if (abastecimentos.size > ABASTECIMENTOS_NA_MEDIA_RECENTE)
        estimarConsumo(abastecimentos.sortedWith(ordem).takeLast(ABASTECIMENTOS_NA_MEDIA_RECENTE)) else null
    val comValor = abastecimentos.filter { it.valor > 0 && it.mililitros > 0 }
    val ultimoComValor = comValor.maxWithOrNull(ordem)
    val precoMedio = if (comValor.isEmpty()) null else comValor.sumOf { it.valor.toLong() } * 1000.0 / comValor.sumOf { it.mililitros.toLong() }
    return ResumoConsumo(
        media = media,
        recente = recente,
        faltamAbastecimentos = (ABASTECIMENTOS_MINIMOS - abastecimentos.size).coerceAtLeast(0),
        precoPorLitro = ultimoComValor?.let { (it.valor.toLong() * 1000 / it.mililitros).toInt() },
        custoPorKm = if (media != null && precoMedio != null) precoMedio / 100.0 / media.kmPorLitro else null,
    )
}

// "Paguei R$ 30 com o litro a R$ 6,29": quantos litros entraram (mililitros, arredondado)
fun litrosPeloValor(valorCentavos: Int, precoPorLitroCentavos: Int): Int? =
    if (valorCentavos <= 0 || precoPorLitroCentavos <= 0) null
    else ((valorCentavos.toLong() * 1000 + precoPorLitroCentavos / 2) / precoPorLitroCentavos).toInt()

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
