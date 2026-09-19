package com.development.motorlog.ui.util

import java.text.Normalizer

// "Óleo" -> "Oleo": decompõe a letra acentuada (NFD) e remove as marcas Unicode (Mn).
// Usado nos DOIS lados da busca de peça, pra "oleo" achar "Óleo".
fun semAcento(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

fun String.contemSemAcento(busca: String): Boolean =
    semAcento(this).contains(semAcento(busca), ignoreCase = true)
