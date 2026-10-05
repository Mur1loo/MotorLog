package com.development.motorlog.domain

import com.development.motorlog.data.FotoMoto

// Fotos no backup automático do Google. O backup do app tem teto de 25 MB: se passar, NADA é
// salvo — nem o banco. Por isso as fotos originais (~300-600 KB) ficam de fora e cada foto ganha
// uma cópia reduzida (fotos_nuvem/, ~100-200 KB) que entra no backup, até um orçamento seguro.
// Celular roubado ou novo: o histórico volta com a conta Google, e as fotos voltam menores.

const val ORCAMENTO_FOTOS_NO_BACKUP = 15L * 1024 * 1024    // dos 25 MB, sobra folga pro banco
const val TAMANHO_ESTIMADO_COPIA = 200L * 1024             // cópia que ainda não foi gerada

// Quais fotos têm cópia no backup: as capas das motos primeiro, depois da mais recente pra mais
// antiga, enquanto couber no orçamento. tamanhos = arquivo → bytes das cópias que já existem.
fun fotosNoBackup(
    fotos: List<FotoMoto>,
    capas: Set<Long>,
    tamanhos: Map<String, Long>,
    orcamento: Long = ORCAMENTO_FOTOS_NO_BACKUP,
): Set<String> {
    val ordem = fotos.sortedWith(compareByDescending<FotoMoto> { it.id in capas }.thenByDescending { it.data }.thenByDescending { it.id })
    val escolhidas = linkedSetOf<String>()
    var usado = 0L
    for (f in ordem) {
        val tamanho = tamanhos[f.arquivo] ?: TAMANHO_ESTIMADO_COPIA
        if (usado + tamanho > orcamento) continue   // uma foto grande não impede as menores de entrar
        usado += tamanho
        escolhidas += f.arquivo
    }
    return escolhidas
}
