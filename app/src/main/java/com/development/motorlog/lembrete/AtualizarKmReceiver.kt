package com.development.motorlog.lembrete

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.atualizarKm
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.util.hojeUtcMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Resposta rápida da notificação ("Atualizar km"): salva o km sem abrir o app e troca a
// notificação pela confirmação. Km menor que o atual NÃO salva (quase sempre é dedo errado):
// pede pra confirmar no app, que tem a trava de 2 toques.
class AtualizarKmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val motoId = intent.getLongExtra(LembreteWorker.EXTRA_MOTO_ID, -1L)
        val digitado = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_KM)?.toString() ?: return
        val novoKm = digitado.filter { it.isDigit() }.toIntOrNull()
        if (motoId <= 0) return

        val pendente = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val moto = db.motoDao().listarTodas().find { it.id == motoId } ?: return@launch
                val texto = when {
                    novoKm == null || novoKm <= 0 -> "Não entendi \"$digitado\". Digite só os números do painel."
                    novoKm < moto.kilometragem -> "${formatarKm(novoKm)} é menor que o atual (${formatarKm(moto.kilometragem)}). Abra o app pra confirmar."
                    else -> {
                        db.atualizarKm(moto, novoKm, hojeUtcMillis())
                        "Km atualizado: ${formatarKm(novoKm)} ✓"
                    }
                }
                LembreteWorker.notificar(context, moto, texto, comResposta = false)
            } finally {
                pendente.finish()
            }
        }
    }

    companion object {
        const val KEY_KM = "km"
    }
}
