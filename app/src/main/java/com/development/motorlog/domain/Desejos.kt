package com.development.motorlog.domain

import com.development.motorlog.data.Desejo

// Lista de desejos: o que o dono quer colocar na moto (baú, protetor de motor, viseira fumê).
// Planejar a personalização é parte do sonho; instalado, o item vira um capítulo da história.
// O dinheiro daqui é personalização, não manutenção: fica fora do gasto total e do custo por km.

fun estaInstalado(d: Desejo): Boolean = d.instaladoEm > 0

data class ResumoDesejos(
    val pendentes: List<Desejo>,     // o que ainda é sonho, o mais novo primeiro
    val instalados: List<Desejo>,    // o que já está na moto, o mais recente primeiro
    val faltaInvestir: Int,          // centavos somados dos pendentes com preço
    val investido: Int,              // centavos somados dos instalados
)

fun resumirDesejos(desejos: List<Desejo>): ResumoDesejos {
    val (instalados, pendentes) = desejos.partition(::estaInstalado)
    return ResumoDesejos(
        pendentes = pendentes.sortedWith(compareByDescending<Desejo> { it.criadoEm }.thenByDescending { it.id }),
        instalados = instalados.sortedWith(compareByDescending<Desejo> { it.instaladoEm }.thenByDescending { it.id }),
        faltaInvestir = pendentes.sumOf { it.preco },
        investido = instalados.sumOf { it.preco },
    )
}

data class ErrosDesejo(val nome: String?, val preco: String?) {
    val ok get() = nome == null && preco == null
}

// nome obrigatório; preço opcional, mas se vier tem que ler ("89,90")
fun validarDesejo(nome: String, preco: String) = ErrosDesejo(
    nome = if (nome.isBlank()) "Diga o que você quer colocar" else null,
    preco = if (preco.isNotBlank() && lerReais(preco) == null) "Valor inválido, ex.: 89,90" else null,
)
