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
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.development.motorlog.MainActivity
import com.development.motorlog.R
import com.development.motorlog.data.AppDatabase
import com.development.motorlog.domain.calcularRecomendacoes
import com.development.motorlog.domain.montarLembrete
import com.development.motorlog.ui.util.hojeUtcMillis
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

// Roda 1x por dia (fim de tarde): pra cada moto, calcula as recomendações e, se houver troca
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
            val recs = calcularRecomendacoes(moto.kilometragem, pecas, db.registroDao().listarRegistros(moto.id))
            val texto = montarLembrete(moto.modelo, moto.kmAtualizadoEm, hoje, recs) ?: return@forEach
            notificar(ctx, moto.id, texto)
        }
        return Result.success()
    }

    private fun notificar(ctx: Context, motoId: Long, texto: String) {
        val abrirPainel = Intent(ctx, MainActivity::class.java)
            .putExtra(EXTRA_MOTO_ID, motoId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendente = PendingIntent.getActivity(
            ctx, motoId.toInt(), abrirPainel,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notificacao = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MotorLog")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendente)
            .setAutoCancel(true)
            .build()
        // id por moto: a notificação do dia seguinte substitui a de hoje em vez de empilhar.
        // A permissão pode ser revogada entre a checagem e o notify → SecurityException = ficar em silêncio.
        try {
            NotificationManagerCompat.from(ctx).notify(motoId.toInt(), notificacao)
        } catch (_: SecurityException) {
        }
    }

    companion object {
        const val CANAL = "lembretes"
        const val EXTRA_MOTO_ID = "motoId"
        private const val TRABALHO = "lembrete-diario"
        private val HORA_DO_LEMBRETE = LocalTime.of(19, 0) // fim do expediente do motoboy

        fun podeNotificar(ctx: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        fun criarCanal(ctx: Context) {
            val canal = NotificationChannel(CANAL, "Lembretes de manutenção", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Km sem atualizar e trocas vencidas" }
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        // Idempotente (KEEP): chamar a cada abertura do app não duplica o trabalho.
        fun agendar(ctx: Context) {
            val agora = LocalDateTime.now()
            var proximo = agora.with(HORA_DO_LEMBRETE)
            if (!proximo.isAfter(agora)) proximo = proximo.plusDays(1)
            val atraso = Duration.between(agora, proximo)
            val pedido = PeriodicWorkRequestBuilder<LembreteWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(atraso.toMinutes(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork(TRABALHO, ExistingPeriodicWorkPolicy.KEEP, pedido)
        }
    }
}
