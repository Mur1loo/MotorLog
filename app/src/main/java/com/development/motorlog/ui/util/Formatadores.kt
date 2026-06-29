package com.development.motorlog.ui.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Long (millis) -> "28/06/2026", em UTC.
// Convenção do projeto: 'data' é a meia-noite UTC do dia escolhido (o DatePicker devolve assim).
fun formatarData(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
