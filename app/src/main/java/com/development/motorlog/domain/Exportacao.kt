package com.development.motorlog.domain

import com.development.motorlog.data.Abastecimento
import com.development.motorlog.data.Moto
import com.development.motorlog.data.Peca
import com.development.motorlog.data.Registro
import com.development.motorlog.data.Servico

// Texto CSV (separador ';', o que o Excel pt-BR abre direto) com tudo que o dono tem no app.
// Puro: recebe o formatador de data de fora pra não decidir fuso aqui (a UI formata em UTC).
// Valores em reais com vírgula ("45,90"; "45" quando não há centavos), como a pessoa lê na planilha.
fun montarExportacao(
    motos: List<Moto>,
    pecas: List<Peca>,
    registros: List<Registro>,
    servicos: List<Servico>,
    abastecimentos: List<Abastecimento> = emptyList(),
    formatarData: (Long) -> String,
): String {
    val nomeMoto = motos.associate { it.id to "${it.modelo} ${it.placa}".trim() }
    val nomePeca = pecas.associate { it.id to it.nome }
    fun limpo(s: String) = s.replace(';', ',').replace('\r', ' ').replace('\n', ' ')

    return buildString {
        appendLine("MotorLog")
        appendLine()
        appendLine("MOTOS")
        appendLine("modelo;placa;ano;km_atual;km_atualizado_em;revisao_a_cada_km;cor;apelido;chegou_em;km_chegada")
        motos.forEach { m ->
            appendLine("${limpo(m.modelo)};${limpo(m.placa)};${m.anoFabricacao};${m.kilometragem};${if (m.kmAtualizadoEm > 0) formatarData(m.kmAtualizadoEm) else ""};${m.intervaloRevisaoKm};${m.cor};${limpo(m.apelido)};${if (m.chegouEm > 0) formatarData(m.chegouEm) else ""};${m.kmChegada}")
        }
        appendLine()
        appendLine("TROCAS")
        appendLine("moto;peca;km_troca;preco;em_servico;data")
        registros.sortedWith(compareBy({ it.motoId }, { it.kmTroca })).forEach { r ->
            appendLine("${nomeMoto[r.motoId] ?: r.motoId};${limpo(nomePeca[r.pecaId] ?: "Peça #${r.pecaId}")};${r.kmTroca};${reaisParaTexto(r.preco)};${if (r.servicoId != null) "sim" else "não"};${if (r.data > 0) formatarData(r.data) else ""}")
        }
        appendLine()
        appendLine("SERVIÇOS")
        appendLine("moto;tipo;data;km;custo;oficina")
        servicos.sortedWith(compareBy({ it.motoId }, { it.data })).forEach { s ->
            appendLine("${nomeMoto[s.motoId] ?: s.motoId};${limpo(s.tipoServico)};${formatarData(s.data)};${s.kilometragem};${reaisParaTexto(s.custo)};${limpo(s.local)}")
        }
        appendLine()
        appendLine("ABASTECIMENTOS")
        appendLine("moto;km;litros;valor;tanque_cheio;data")
        abastecimentos.sortedWith(compareBy({ it.motoId }, { it.km })).forEach { a ->
            appendLine("${nomeMoto[a.motoId] ?: a.motoId};${a.km};${litrosParaTexto(a.mililitros)};${reaisParaTexto(a.valor)};${if (a.tanqueCheio) "sim" else "não"};${formatarData(a.data)}")
        }
        appendLine()
        appendLine("PEÇAS (catálogo)")
        appendLine("peca;intervalo_km")
        pecas.forEach { p -> appendLine("${limpo(p.nome)};${p.intervaloKm}") }
    }
}
