package com.example.practica03

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla de Destino: recibe el nombre y la edad enviados desde
 * PantallaPrincipal a traves del Intent, y los despliega en pantalla.
 */
@Composable
fun PantallaDestino(nombre: String, edad: String) {
    Scaffold(
        containerColor = Color(0xFFF3E5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3E5F5))
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Pantalla de Destino", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Nombre recibido:", fontSize = 16.sp)
            Text(text = nombre, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Edad recibida:", fontSize = 16.sp)
            Text(text = edad, fontSize = 24.sp)
        }
    }
}
