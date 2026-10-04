package com.development.motorlog.domain

import com.development.motorlog.data.Passeio

// Rolês e viagens: pra onde, quando, km na saída e na volta (do painel, sem GPS), quem foi junto
// e uma foto do álbum. Aqui as contas e a validação; a tela é ui/screens/RolesScreen.kt.

// km rodados no rolê; null = volta não anotada (ou anotada errada, abaixo da saída)
fun kmDoRole(p: Passeio): Int? = p.kmChegada.takeIf { it >= p.kmSaida }?.minus(p.kmSaida)

data class ResumoRoles(val quantidade: Int, val kmTotal: Int, val maior: Passeio?)

fun resumirRoles(roles: List<Passeio>): ResumoRoles = ResumoRoles(
    quantidade = roles.size,
    kmTotal = roles.sumOf { kmDoRole(it) ?: 0 },
    maior = roles.filter { (kmDoRole(it) ?: 0) > 0 }.maxByOrNull { kmDoRole(it) ?: 0 },
)

data class ErrosRole(val destino: String?, val kmSaida: String?, val kmChegada: String?) {
    val ok get() = destino == null && kmSaida == null && kmChegada == null
}

// destino e km da saída obrigatórios; a volta é opcional, mas não pode ter menos km que a saída
fun validarRole(destino: String, kmSaida: String, kmChegada: String): ErrosRole {
    val saida = kmSaida.trim().toIntOrNull()
    return ErrosRole(
        destino = if (destino.isBlank()) "Pra onde vocês foram?" else null,
        kmSaida = erroDeKm(kmSaida),
        kmChegada = when {
            kmChegada.isBlank() -> null
            erroDeKm(kmChegada) != null -> erroDeKm(kmChegada)
            saida != null && kmChegada.trim().toInt() < saida -> "A volta não pode ter menos km que a saída"
            else -> null
        },
    )
}

// "320 km · com a galera" (o que dá pra dizer do rolê numa linha)
fun detalheDoRole(p: Passeio, formatarKm: (Int) -> String): String? =
    listOfNotNull(kmDoRole(p)?.takeIf { it > 0 }?.let(formatarKm), p.companhia.trim().ifBlank { null }?.let { "com $it" })
        .joinToString(" · ").ifBlank { null }
