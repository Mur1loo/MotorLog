package com.development.motorlog.ui.util

import com.development.motorlog.R
import com.development.motorlog.domain.EventoDaHistoria
import com.development.motorlog.domain.TipoCuidado
import com.development.motorlog.domain.TipoEvento

// Nome da peça → ícone do protótipo (partIcon do data.jsx), por palavra-chave. Sem campo no banco.
fun iconeDaPeca(nome: String): Int {
    val n = semAcento(nome).lowercase()
    return when {
        "oleo" in n && ("suspens" in n || "bengala" in n) -> R.drawable.ic_ml_fluid
        "oleo" in n -> R.drawable.ic_ml_oil
        "filtro de ar" in n -> R.drawable.ic_ml_air
        "combust" in n -> R.drawable.ic_ml_fuel
        "pneu" in n || "camara" in n || "raio" in n || "roda" in n -> R.drawable.ic_ml_tire
        "pastilha" in n || "lona" in n || "freio" in n || "pinca" in n || "burrinho" in n -> R.drawable.ic_ml_brake
        "corrente" in n || "relacao" in n || "coroa" in n || "pinhao" in n || "saboneteira" in n || "coxim" in n -> R.drawable.ic_ml_chain
        "vela" in n -> R.drawable.ic_ml_spark
        "bateria" in n || "partida" in n || "rele" in n || "eletric" in n -> R.drawable.ic_ml_battery
        "fluido" in n || "liquido" in n -> R.drawable.ic_ml_fluid
        else -> R.drawable.ic_ml_wrench
    }
}

// ícone de cada cuidado do diário (TipoCuidado)
fun iconeDoCuidado(tipo: TipoCuidado): Int = when (tipo) {
    TipoCuidado.LAVAGEM -> R.drawable.ic_ml_fluid
    TipoCuidado.LAVAGEM_DETALHADA, TipoCuidado.CERA -> R.drawable.ic_ml_spark
    TipoCuidado.CORRENTE -> R.drawable.ic_ml_chain
    TipoCuidado.CALIBRAGEM -> R.drawable.ic_ml_tire
}

// ícone de cada momento da linha do tempo da moto
fun iconeDoEvento(evento: EventoDaHistoria): Int = when (evento.tipo) {
    TipoEvento.CHEGADA -> R.drawable.ic_ml_moto
    TipoEvento.FOTO -> R.drawable.ic_ml_camera
    TipoEvento.TROCA -> iconeDaPeca(evento.referencia ?: "")
    TipoEvento.VISITA -> R.drawable.ic_ml_wrench
    TipoEvento.CUIDADO -> evento.referencia?.let(TipoCuidado::doCodigo)?.let(::iconeDoCuidado) ?: R.drawable.ic_ml_check
    TipoEvento.MARCO_KM -> R.drawable.ic_ml_road
    TipoEvento.ANIVERSARIO -> R.drawable.ic_ml_calendar
    TipoEvento.DESPEDIDA -> R.drawable.ic_ml_tag
    TipoEvento.PERSONALIZACAO -> R.drawable.ic_ml_spark
}
