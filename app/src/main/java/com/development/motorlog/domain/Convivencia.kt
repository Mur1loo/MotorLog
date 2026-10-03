package com.development.motorlog.domain

import com.development.motorlog.data.Moto
import java.time.LocalDate
import java.time.Period

// A moto como "alguém": o nome que o dono dá e o tempo de vida juntos. Quem conquistou a moto
// depois de anos de consórcio quer ver isso — "Juntos há 1 ano e 3 meses · 23.400 km".

// Como o app chama a moto: o apelido, ou o modelo quando não há apelido
fun nomeDaMoto(moto: Moto): String = moto.apelido.trim().ifBlank { moto.modelo }

// "1 ano e 3 meses", "2 anos", "5 meses", "12 dias", "1 dia", "hoje". null = chegada não
// informada (0) ou no futuro. Datas em meia-noite UTC, como em todo o app.
fun descreverTempoJuntos(chegouEm: Long, hoje: Long): String? {
    if (chegouEm <= 0 || chegouEm > hoje) return null
    val periodo = Period.between(
        LocalDate.ofEpochDay(chegouEm / MILLIS_POR_DIA),
        LocalDate.ofEpochDay(hoje / MILLIS_POR_DIA),
    )
    fun plural(n: Int, um: String, varios: String) = if (n == 1) "1 $um" else "$n $varios"
    return when {
        periodo.years > 0 -> plural(periodo.years, "ano", "anos") +
            if (periodo.months > 0) " e " + plural(periodo.months, "mês", "meses") else ""
        periodo.months > 0 -> plural(periodo.months, "mês", "meses")
        periodo.days > 0 -> plural(periodo.days, "dia", "dias")
        else -> "hoje"
    }
}

// km rodados desde que ela chegou; null = km da chegada não informado (-1)
fun kmJuntos(kmChegada: Int, kmAtual: Int): Int? = if (kmChegada < 0) null else (kmAtual - kmChegada).coerceAtLeast(0)
