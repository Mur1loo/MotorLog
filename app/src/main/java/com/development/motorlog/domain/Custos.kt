package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico
import java.time.LocalDate

// Quanto a moto custa pra rodar: o motoboy pensa em "quanto gasto por km / por mês".
// Gasto total = visitas à oficina + trocas avulsas com preço, em CENTAVOS (Dinheiro.kt). Km rodados =
// km atual − 1º km registrado no app (HistoricoKm). Sem km rodado ainda, não há custo por km (null).

fun gastoTotal(servicos: List<Servico>, trocasAvulsas: List<Registro>): Int =
    servicos.sumOf { it.custo } + trocasAvulsas.filter { it.servicoId == null }.sumOf { it.preco }

fun kmRodadosNoApp(historico: List<HistoricoKm>, kmAtual: Int): Int {
    val primeiro = historico.minByOrNull { it.data } ?: return 0
    return (kmAtual - primeiro.km).coerceAtLeast(0)
}

// R$ por km (ex.: 0.42), a partir do gasto em centavos. null quando ainda não há km rodado registrado.
fun custoPorKm(gastoTotal: Int, kmRodados: Int): Double? =
    if (kmRodados <= 0 || gastoTotal <= 0) null else gastoTotal / 100.0 / kmRodados

// Primeiro dia do mês de 'hoje' (meia-noite UTC), na mesma convenção de todas as datas do app
fun inicioDoMes(hoje: Long): Long =
    LocalDate.ofEpochDay(hoje / MILLIS_POR_DIA).withDayOfMonth(1).toEpochDay() * MILLIS_POR_DIA

// Visitas à oficina + trocas por conta própria do mês. Troca sem dia conhecido (data 0, anterior
// à v11) não entra; troca de dentro de um serviço já está no custo dele.
fun gastoNoMes(servicos: List<Servico>, trocasAvulsas: List<Registro>, hoje: Long): Int {
    val inicio = inicioDoMes(hoje)
    return servicos.filter { it.data in inicio..hoje }.sumOf { it.custo } +
        trocasAvulsas.filter { it.servicoId == null && it.data > 0 && it.data in inicio..hoje }.sumOf { it.preco }
}
