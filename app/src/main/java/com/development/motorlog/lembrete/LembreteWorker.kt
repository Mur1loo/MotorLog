package com.development.motorlog.lembrete

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.development.motorlog.MainActivity
import com.development.motorlog.R
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.calcularRecomendacoes
import com.development.motorlog.domain.calcularRitmoKmMes
import com.development.motorlog.domain.comRevisao
import com.development.motorlog.domain.recomendacaoDeRevisao
import com.development.motorlog.domain.montarLembrete
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.hojeUtcMillis
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

// Roda 2x por dia (começo do dia e fim de tarde): pra cada moto, calcula as recomendações e, se houver troca
// vencida/perto ou km parado há DIAS_PARA_LEMBRAR_KM dias, notifica. Sem motivo → silêncio.
class LembreteWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!podeNotificar(ctx)) return Result.success()

        val db = AppDatabase.getDatabase(ctx)
        val pecas = db.pecaDao().listarPecas()
        val hoje = hojeUtcMillis()
        criarCanal(ctx)

        db.motoDao().listarTodas().forEach { moto ->
            val recsPecas = calcularRecomendacoes(moto.kilometragem, pecas, db.registroDao().listarRegistros(moto.id))
            val recs = comRevisao(recsPecas, recomendacaoDeRevisao(moto.kilometragem, moto.intervaloRevisaoKm, db.servicoDao().query(moto.id)))
            val ritmo = calcularRitmoKmMes(db.historicoKmDao().listarPorMoto(moto.id), hoje)
            val texto = montarLembrete(moto.modelo, moto.kmAtualizadoEm, hoje, recs, ritmo, ::formatarData) ?: return@forEach
            // o título da notificação já é o modelo; o corpo não precisa repetir
            notificar(ctx, moto, texto.removePrefix("${moto.modelo}: "), comResposta = true)
        }
        return Result.success()
    }

    companion object {
        const val CANAL = "lembretes"
        const val EXTRA_MOTO_ID = "motoId"
        // nome novo: quem atualiza o app troca o trabalho de 1x/dia pelo de 2x/dia (ver agendar)
        private const val TRABALHO = "lembrete-2x-dia"
        private const val TRABALHO_ANTIGO = "lembrete-diario"
        // antes de sair pra rodar e no fim do expediente do motoboy — 12h de distância, então um
        // periódico de 12h ancorado no próximo horário cai sempre num dos dois
        private val HORARIOS = listOf(LocalTime.of(7, 0), LocalTime.of(19, 0))

        fun podeNotificar(ctx: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        // Notificação da moto. comResposta = ação "Atualizar km" com campo de resposta rápida
        // (o motoboy digita o km do painel sem abrir o app — AtualizarKmReceiver salva).
        fun notificar(ctx: Context, moto: Moto, texto: String, comResposta: Boolean) {
            if (!podeNotificar(ctx)) return
            criarCanal(ctx)
            val abrirPainel = Intent(ctx, MainActivity::class.java)
                .putExtra(EXTRA_MOTO_ID, moto.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendente = PendingIntent.getActivity(
                ctx, moto.id.toInt(), abrirPainel,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val builder = NotificationCompat.Builder(ctx, CANAL)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setContentTitle(moto.modelo)
                .setContentText(texto)
                .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(pendente)
                .setAutoCancel(true)
            if (comResposta) {
                val responder = Intent(ctx, AtualizarKmReceiver::class.java).putExtra(EXTRA_MOTO_ID, moto.id)
                // RemoteInput exige PendingIntent MUTÁVEL (o sistema escreve a resposta nele)
                val pendenteResposta = PendingIntent.getBroadcast(
                    ctx, moto.id.toInt(), responder,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                )
                val campo = RemoteInput.Builder(AtualizarKmReceiver.KEY_KM).setLabel("Km do painel (só números)").build()
                builder.addAction(
                    NotificationCompat.Action.Builder(R.drawable.ic_ml_gauge, "Atualizar km", pendenteResposta)
                        .addRemoteInput(campo)
                        .setAllowGeneratedReplies(false)
                        .build(),
                )
            }
            // id por moto: a notificação do dia seguinte substitui a de hoje em vez de empilhar.
            // A permissão pode ser revogada entre a checagem e o notify → SecurityException = ficar em silêncio.
            try {
                NotificationManagerCompat.from(ctx).notify(moto.id.toInt(), builder.build())
            } catch (_: SecurityException) {
            }
        }

        fun criarCanal(ctx: Context) {
            val canal = NotificationChannel(CANAL, "Lembretes de manutenção", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Km sem atualizar e trocas vencidas" }
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        // Pra testar sem esperar as 7h/19h:
        //   adb shell am start -n com.development.motorlog/.MainActivity --ez lembreteAgora true
        const val EXTRA_RODAR_AGORA = "lembreteAgora"
        fun rodarAgora(ctx: Context) {
            WorkManager.getInstance(ctx).enqueue(OneTimeWorkRequestBuilder<LembreteWorker>().build())
        }

        // Idempotente (KEEP): chamar a cada abertura do app não duplica o trabalho.
        fun agendar(ctx: Context) {
            val agora = LocalDateTime.now()
            val proximo = HORARIOS
                .map { hora -> agora.with(hora).let { if (it.isAfter(agora)) it else it.plusDays(1) } }
                .min()
            val atraso = Duration.between(agora, proximo)
            val pedido = PeriodicWorkRequestBuilder<LembreteWorker>(12, TimeUnit.HOURS)
                .setInitialDelay(atraso.toMinutes(), TimeUnit.MINUTES)
                .build()
            val wm = WorkManager.getInstance(ctx)
            wm.cancelUniqueWork(TRABALHO_ANTIGO)   // o de 1x/dia (versões até a 1.2.0)
            wm.enqueueUniquePeriodicWork(TRABALHO, ExistingPeriodicWorkPolicy.KEEP, pedido)
        }
    }
}
