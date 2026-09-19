package com.example.practica03

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PantallaPrincipal(onEnviar = { nombre, edad ->
                // Intent EXPLICITO: indicamos exactamente que Activity debe abrirse
                val intent = Intent(this, SecondActivity::class.java)
                // Adjuntamos los datos capturados como "extras" del Intent
                intent.putExtra("EXTRA_NOMBRE", nombre)
                intent.putExtra("EXTRA_EDAD", edad)
                startActivity(intent)
            })
        }
    }
}
