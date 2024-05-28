package com.bloqueo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import android.widget.Toast
import android.content.Intent
import android.view.Window


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val minutosEditText = findViewById<EditText>(R.id.minutosEditText)
        val bloquearNotifSwitch = findViewById<SwitchCompat>(R.id.bloquearNotifSwitch)
        val bloquearButton = findViewById<Button>(R.id.bloquearButton)

        bloquearButton.setOnClickListener {
            val minutosText = minutosEditText.text.toString()

            if (minutosText.isNotEmpty()) {
                val minutos = minutosText.toInt()
                val bloquearNotif = bloquearNotifSwitch.isChecked

                mostrarConfirmacionBloqueo(minutos, bloquearNotif)
            } else {
                Toast.makeText(this, "Por favor, ingrese los minutos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarConfirmacionBloqueo(minutos: Int, bloquearNotif: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
            .setMessage("¿Estás seguro que deseas bloquear el teléfono?")
            .setPositiveButton("Bloquear teléfono") { _, _ ->
                bloquearTelefono(minutos, bloquearNotif)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun bloquearTelefono(minutos: Int, bloquearNotif: Boolean) {
        val intent = Intent(this, BloqueoActivity::class.java)
        intent.putExtra("minutos", minutos)
        intent.putExtra("bloquearNotif", bloquearNotif)
        startActivity(intent)
    }


}
