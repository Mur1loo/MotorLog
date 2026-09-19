package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import java.time.LocalDate

// Quanto a moto custa pra rodar: o motoboy pensa em "quanto gasto por km / por mês".
// Gasto total = visitas à oficina + trocas avulsas com preço. Km rodados = km atual − 1º km
// registrado no app (HistoricoKm). Sem km rodado ainda, não há custo por km (null).

fun gastoTotal(servicos: List<Servico>, trocasAvulsas: List<Registro>): Int =
    servicos.sumOf { it.custo } + trocasAvulsas.filter { it.servicoId == null }.sumOf { it.preco }

fun kmRodadosNoApp(historico: List<HistoricoKm>, kmAtual: Int): Int {
    val primeiro = historico.minByOrNull { it.data } ?: return 0
    return (kmAtual - primeiro.km).coerceAtLeast(0)
}

// R$ por km (ex.: 0.42). null quando ainda não há km rodado registrado.
fun custoPorKm(gastoTotal: Int, kmRodados: Int): Double? =
    if (kmRodados <= 0 || gastoTotal <= 0) null else gastoTotal.toDouble() / kmRodados

// Primeiro dia do mês de 'hoje' (meia-noite UTC), na mesma convenção de todas as datas do app
fun inicioDoMes(hoje: Long): Long =
    LocalDate.ofEpochDay(hoje / MILLIS_POR_DIA).withDayOfMonth(1).toEpochDay() * MILLIS_POR_DIA

// Só serviços têm data; trocas avulsas não entram (não sabemos o dia)
fun gastoNoMes(servicos: List<Servico>, hoje: Long): Int {
    val inicio = inicioDoMes(hoje)
    return servicos.filter { it.data in inicio..hoje }.sumOf { it.custo }
}
