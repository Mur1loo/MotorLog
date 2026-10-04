package com.development.motorlog.domain

import com.development.motorlog.data.Moto

// Despedida: quando a moto é vendida, em vez de apagar tudo ela "passa adiante" — sai da garagem
// do dia a dia (sem lembretes, sem km pra atualizar) e vira lembrança, com a história inteira
// guardada. Dá pra desfazer se ela voltar.

fun estaVendida(moto: Moto): Boolean = moto.vendidaEm > 0

// até quando a história conta: o dia da venda, ou hoje pra quem ainda está na garagem
fun fimDaHistoria(moto: Moto, hoje: Long): Long = if (estaVendida(moto)) minOf(moto.vendidaEm, hoje) else hoje

data class Garagem(val ativas: List<Moto>, val lembrancas: List<Moto>)

// as motos do dia a dia (na ordem de sempre) e as lembranças (a venda mais recente primeiro)
fun separarGaragem(motos: List<Moto>): Garagem {
    val (vendidas, ativas) = motos.partition(::estaVendida)
    return Garagem(ativas, vendidas.sortedByDescending { it.vendidaEm })
}
