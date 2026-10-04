package com.development.motorlog.domain

import com.development.motorlog.data.HistoricoKm
import java.text.NumberFormat
import java.util.Locale

// Registros de km (1 por dia): a base das médias de km/mês, dos marcos e do "atualizado há".
// Um km digitado errado num dia passado (150.000 em vez de 15.000) bagunçava tudo pra sempre —
// agora dá pra corrigir ou apagar. Aqui as regras; a tela é ui/screens/RegistrosDeKmScreen.kt.

private val milharKm = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"))
private val ordemDosPontos = compareBy<HistoricoKm>({ it.data }, { it.id })

// o registro que manda no km da moto: o do dia mais recente
fun pontoMaisRecente(pontos: List<HistoricoKm>): HistoricoKm? = pontos.maxWithOrNull(ordemDosPontos)

// O km corrigido briga com os vizinhos? (o painel só sobe com o tempo.) É só um aviso: às vezes o
// errado é o vizinho, e aí a pessoa corrige ele depois. null = em ordem.
fun avisoDeKmForaDeOrdem(pontos: List<HistoricoKm>, ponto: HistoricoKm, novoKm: Int, formatarData: (Long) -> String): String? {
    val outros = pontos.filter { it.id != ponto.id }.sortedWith(ordemDosPontos)
    val antes = outros.lastOrNull { it.data < ponto.data }
    val depois = outros.firstOrNull { it.data > ponto.data }
    return when {
        antes != null && novoKm < antes.km -> "Fica abaixo do registro de ${formatarData(antes.data)} (${milharKm.format(antes.km)} km)"
        depois != null && novoKm > depois.km -> "Fica acima do registro de ${formatarData(depois.data)} (${milharKm.format(depois.km)} km)"
        else -> null
    }
}

// km rodados desde o registro anterior (null no primeiro): "+320 km" na lista
fun kmDesdeOAnterior(pontos: List<HistoricoKm>): Map<Long, Int> {
    val ordenados = pontos.sortedWith(ordemDosPontos)
    return ordenados.zipWithNext().associate { (antes, depois) -> depois.id to depois.km - antes.km }
}
