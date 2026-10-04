package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto

// Antes e depois: duas fotos do álbum lado a lado (lavagem, peça nova, personalização) — o
// resultado do cuidado, do jeito que a pessoa quer mostrar. O "antes" é sempre a mais antiga,
// não importa a ordem em que foram tocadas.

data class AntesEDepois(val antes: FotoMoto, val depois: FotoMoto)

fun montarAntesEDepois(a: FotoMoto, b: FotoMoto): AntesEDepois {
    val ordem = compareBy<FotoMoto>({ it.data }, { it.km }, { it.id })
    return if (ordem.compare(a, b) <= 0) AntesEDepois(a, b) else AntesEDepois(b, a)
}

// "12 dias depois", "3 meses depois", "1 ano e 2 meses depois", "no mesmo dia"
fun descreverIntervalo(par: AntesEDepois): String {
    val tempo = descreverTempoJuntos(par.antes.data, par.depois.data)
    return when (tempo) {
        null, "hoje" -> "no mesmo dia"
        else -> "$tempo depois"
    }
}

// título sugerido: a legenda da foto de depois ("Depois da lavagem"), senão "Antes e depois"
fun tituloDoAntesEDepois(par: AntesEDepois): String = par.depois.legenda.trim().ifBlank { "Antes e depois" }
