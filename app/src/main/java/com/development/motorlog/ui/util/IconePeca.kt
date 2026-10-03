package com.development.motorlog.ui.util

import com.development.motorlog.R
import com.development.motorlog.domain.TipoCuidado

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
    TipoCuidado.CERA -> R.drawable.ic_ml_spark
    TipoCuidado.CORRENTE -> R.drawable.ic_ml_chain
    TipoCuidado.CALIBRAGEM -> R.drawable.ic_ml_tire
}
