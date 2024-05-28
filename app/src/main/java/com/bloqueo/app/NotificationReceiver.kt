package com.bloqueo.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager

class NotificationReceiver : BroadcastReceiver() {

    private var isTimerActive = false

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /* Verificar si el cronómetro está activo y se debe bloquear las notificaciones */
        if (isTimerActive) {
            /* Cancelar la notificación entrante */
            val notificationId = intent.getIntExtra("notification_id", 0)
            notificationManager.cancel(notificationId)

            /* Abortar la propagación de la transmisión */
            abortBroadcast()
        } else {
            /* Procesar la notificación normalmente */
        }
    }

    fun setTimerActive(active: Boolean) {
        isTimerActive = active
    }
}

