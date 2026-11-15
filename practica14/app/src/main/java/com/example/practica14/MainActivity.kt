package com.example.practica14

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practica14.ui.theme.Practica14Theme
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuración global de OSMdroid (caché, user-agent)
        val configuracion = Configuration.getInstance()
        configuracion.load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        configuracion.userAgentValue = "Practica14_UAS/1.0"

        val baseOsmdroid = File(getExternalFilesDir(null) ?: filesDir, "osmdroid")
        configuracion.osmdroidBasePath = baseOsmdroid
        configuracion.osmdroidTileCache = File(baseOsmdroid, "tiles")

        setContent {
            Practica14Theme {
                MapaOSMScreen()
            }
        }
    }
}
