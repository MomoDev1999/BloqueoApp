package com.bloqueo.app

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BloqueoActivity : AppCompatActivity() {

    private lateinit var textViewMensaje: TextView
    private lateinit var textViewCronometro: TextView
    private lateinit var textViewEstadoNotificaciones: TextView

    private var minutos: Int = 0
    private var bloquearNotif: Boolean = false

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var alertDialog: AlertDialog
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationReceiver: NotificationReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_bloqueo)

        textViewMensaje = findViewById(R.id.textViewMensaje)
        textViewCronometro = findViewById(R.id.textViewCronometro)
        textViewEstadoNotificaciones = findViewById(R.id.textViewEstadoNotificaciones)

        /* Eliminar título */
        supportActionBar?.setDisplayShowTitleEnabled(false)

        /* Crear instancia de sonido */
        mediaPlayer = MediaPlayer.create(this, R.raw.desbloqueo)

        /* Obtener los datos pasados desde la actividad anterior */
        val extras = intent.extras
        if (extras != null) {
            minutos = extras.getInt("minutos", 0)
            bloquearNotif = extras.getBoolean("bloquearNotif", false)
        }

        /* Configurar el texto del mensaje */
        textViewMensaje.text = getString(R.string.mensajebloqueo)

        /* Configurar el texto del estado de las notificaciones */
        val estadoNotif = if (bloquearNotif) getString(R.string.notificacionesbloqueadas) else getString(
            R.string.notificacionesnobloqueadas
        )
        textViewEstadoNotificaciones.text =
            getString(R.string.estadonotificaciones, estadoNotif)

        /* Crear el AlertDialog */
        val builder = AlertDialog.Builder(this@BloqueoActivity)
            .setMessage("Se ha acabado el tiempo, ya puedes volver a utilizar tu teléfono")
            .setPositiveButton("Volver al inicio") { dialog, _ ->
                dialog.dismiss()
                volverAlInicio()
            }
            .setCancelable(false)

        alertDialog = builder.create()

        /* Iniciar el cronómetro */
        iniciarCronometro()

        /* Ocultar los botones de navegación (barra de navegación virtual) */
        hideSystemUI()

        /* Bloquear la interacción con la pantalla */
        blockTouchEvents()

        /* Esconder barra de notificaciones */
        hideStatusBar()

        /* Bloquear deslizamiento hacia abajo */
        blockNotificationBar()

        /* Registrar el receptor de difusión para cancelar notificaciones */
        registerNotificationReceiver()
    }


    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    private fun blockTouchEvents() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun blockNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun iniciarCronometro() {
        countDownTimer = object : CountDownTimer((minutos * 60000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutosRestantes = (millisUntilFinished / 60000).toInt()
                val segundosRestantes = (millisUntilFinished % 60000 / 1000).toInt()
                val tiempoRestante = String.format("%02d:%02d", minutosRestantes, segundosRestantes)
                textViewCronometro.text = tiempoRestante
            }

            override fun onFinish() {
                mediaPlayer.start()
                alertDialog.show()
            }
        }.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                /* No hacer nada al tocar la pantalla */
            }
            MotionEvent.ACTION_UP -> {
                /* No hacer nada al levantar el dedo de la pantalla */
            }
            MotionEvent.ACTION_MOVE -> {
                /* Bloquear el deslizamiento hacia abajo */
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun volverAlInicio() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        countDownTimer.cancel()
        mediaPlayer.release()

        val intent = Intent(this@BloqueoActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun registerNotificationReceiver() {
        notificationReceiver = NotificationReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.NOTIFICATION_CANCELLED")
        registerReceiver(notificationReceiver, filter)
    }

    private fun unregisterNotificationReceiver() {
        unregisterReceiver(notificationReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (bloquearNotif) {
            notificationReceiver.setTimerActive(true)
        }
    }

    override fun onPause() {
        super.onPause()
        notificationReceiver.setTimerActive(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNotificationReceiver()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}


