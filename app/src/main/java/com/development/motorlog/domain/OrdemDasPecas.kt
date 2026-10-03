package com.development.motorlog.domain

import com.development.motorlog.data.Peca

// As peças que o motoboy troca toda hora. O catálogo vem na ordem das partes do motor (óleo,
// filtros, válvulas, comando...), e pneu e pastilha ficavam lá pelo 40º item da lista.
val PECAS_DO_DIA_A_DIA = listOf(
    "Óleo do motor",
    "Filtro de óleo",
    "Kit Relação (Coroa, Corrente e Pinhão)",
    "Pastilha de freio dianteira",
    "Pastilha/Lona de freio traseira",
    "Pneu traseiro",
    "Pneu dianteiro",
    "Vela de ignição",
    "Filtro de ar",
    "Cabo de embreagem",
    "Fluido de freio",
    "Bateria",
)

// Ordem da lista de "Troquei uma peça": primeiro as que ESTA moto mais troca (usos = pecaId →
// quantas trocas registradas); empate ou nenhum histórico → as do dia a dia; depois o resto do
// catálogo na ordem de sempre. A busca continua filtrando por nome por cima disso.
fun ordenarPecasPorUso(pecas: List<Peca>, usos: Map<Long, Int>): List<Peca> {
    fun posicaoNoDiaADia(p: Peca) = PECAS_DO_DIA_A_DIA.indexOfFirst { it.equals(p.nome, ignoreCase = true) }
        .let { if (it < 0) Int.MAX_VALUE else it }
    return pecas.withIndex()
        .sortedWith(
            compareByDescending<IndexedValue<Peca>> { usos[it.value.id] ?: 0 }
                .thenBy { posicaoNoDiaADia(it.value) }
                .thenBy { it.index },
        )
        .map { it.value }
}
