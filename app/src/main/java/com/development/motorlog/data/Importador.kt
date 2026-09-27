package com.development.motorlog.data

import com.development.motorlog.domain.DadosImportados

data class ResumoImportacao(val motos: Int, val trocas: Int, val servicos: Int, val pecas: Int, val ignorados: Int)

// Aplica um DadosImportados no banco SEM duplicar: peça casa por nome, moto por modelo+placa,
// serviço por (moto, km, data, tipo), troca por (moto, peça, km). O que já existe é ignorado —
// restaurar um backup duas vezes não dobra nada.
suspend fun AppDatabase.importar(dados: DadosImportados): ResumoImportacao {
    var novasPecas = 0; var novasMotos = 0; var novosServicos = 0; var novasTrocas = 0; var ignorados = 0

    // 1. catálogo de peças (as trocas dependem dele)
    val pecas = pecaDao().listarPecas().toMutableList()
    fun pecaPorNome(nome: String) = pecas.find { it.nome.equals(nome.trim(), ignoreCase = true) }
    dados.pecas.forEach { p ->
        if (pecaPorNome(p.nome) == null) {
            pecaDao().inserir(Peca(nome = p.nome, intervaloKm = p.intervaloKm)); novasPecas++
        } else ignorados++
    }
    if (novasPecas > 0) { pecas.clear(); pecas += pecaDao().listarPecas() }

    // 2. motos
    val motos = motoDao().listarTodas().toMutableList()
    fun motoPorChave(chave: String) = motos.find { "${it.modelo} ${it.placa}".trim().equals(chave.trim(), ignoreCase = true) }
    dados.motos.forEach { m ->
        if (motoPorChave(m.chave) == null) {
            val id = motoDao().inserir(Moto(modelo = m.modelo, placa = m.placa, anoFabricacao = m.ano, kilometragem = m.km, kmAtualizadoEm = m.kmAtualizadoEm ?: 0, intervaloRevisaoKm = m.intervaloRevisaoKm, cor = m.cor))
            if (m.kmAtualizadoEm != null) historicoKmDao().inserir(HistoricoKm(motoId = id, km = m.km, data = m.kmAtualizadoEm))
            novasMotos++
        } else ignorados++
    }
    motos.clear(); motos += motoDao().listarTodas()

    // 3. serviços
    val servicosPorMoto = mutableMapOf<Long, MutableList<Servico>>()
    motos.forEach { servicosPorMoto[it.id] = servicoDao().query(it.id).toMutableList() }
    dados.servicos.forEach { s ->
        val moto = motoPorChave(s.motoChave) ?: run { ignorados++; return@forEach }
        val lista = servicosPorMoto.getValue(moto.id)
        val existe = lista.any { it.kilometragem == s.km && it.data == (s.data ?: 0) && it.tipoServico.equals(s.tipo, true) }
        if (existe) { ignorados++; return@forEach }
        val novo = Servico(motoId = moto.id, custo = s.custo, kilometragem = s.km, tipoServico = s.tipo, data = s.data ?: 0, local = s.oficina)
        val id = servicoDao().inserir(novo)
        lista += novo.copy(id = id); novosServicos++
    }

    // 4. trocas (ligadas ao serviço da mesma moto e mesmo km, quando vieram "em serviço")
    dados.trocas.forEach { t ->
        val moto = motoPorChave(t.motoChave) ?: run { ignorados++; return@forEach }
        val peca = pecaPorNome(t.peca) ?: run {
            // peça desconhecida no catálogo: cria com um intervalo genérico pra não perder a troca
            pecaDao().inserir(Peca(nome = t.peca, intervaloKm = 10000)); novasPecas++
            pecas.clear(); pecas += pecaDao().listarPecas()
            pecaPorNome(t.peca) ?: run { ignorados++; return@forEach }
        }
        val existentes = registroDao().listarRegistros(moto.id)
        if (existentes.any { it.pecaId == peca.id && it.kmTroca == t.km }) { ignorados++; return@forEach }
        val servicoId = if (t.emServico) servicosPorMoto[moto.id]?.find { it.kilometragem == t.km }?.id else null
        registroDao().inserirRegistro(Registro(motoId = moto.id, pecaId = peca.id, kmTroca = t.km, servicoId = servicoId, preco = t.preco, data = t.data ?: 0))
        novasTrocas++
    }
    return ResumoImportacao(novasMotos, novasTrocas, novosServicos, novasPecas, ignorados)
}
