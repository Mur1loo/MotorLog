package com.development.motorlog.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

// Long (millis) -> "28/06/2026", em UTC.
// Convenção do projeto: 'data' é a meia-noite UTC do dia escolhido (o DatePicker devolve assim).
fun formatarData(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

// "28/06/2026" -> Long (meia-noite UTC), ou null se não for uma data. Inverso de formatarData.
fun lerData(texto: String): Long? = try {
    java.time.LocalDate.parse(texto.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
} catch (_: java.time.format.DateTimeParseException) { null }

// hoje à meia-noite UTC — a MESMA base que o DatePicker usa. Convenção pra todo Long de data do projeto.
fun hojeUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private val numeroBr: NumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"))

// 16000 -> "16.000" (o ponto de milhar é o que o motoboy lê no painel e no talão da oficina)
fun formatarNumero(n: Int): String = numeroBr.format(n)
fun formatarKm(km: Int): String = "${formatarNumero(km)} km"
fun formatarReais(valor: Int): String = "R$ ${formatarNumero(valor)}"

private val reaisComCentavos: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
// 0.4217 -> "R$ 0,42" (custo por km precisa dos centavos)
fun formatarReaisCentavos(valor: Double): String = reaisComCentavos.format(valor).replace('\u00A0', ' ')
