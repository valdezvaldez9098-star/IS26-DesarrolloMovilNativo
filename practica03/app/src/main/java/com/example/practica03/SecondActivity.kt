package com.example.practica03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Leemos los extras que vinieron adjuntos en el Intent
        val nombreRecibido = intent.getStringExtra("EXTRA_NOMBRE") ?: ""
        val edadRecibida = intent.getStringExtra("EXTRA_EDAD") ?: ""

        setContent {
            PantallaDestino(nombre = nombreRecibido, edad = edadRecibida)
        }
    }
}
