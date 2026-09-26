package com.example.registroestudiantes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DetailScreen(
    matricula: String,
    nombre: String,
    carrera: String,
    turno: String,
    estatusActivo: Boolean,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Confirmación de Registro", style = MaterialTheme.typography.headlineMedium)

        DatoRegistro("Matrícula", matricula)
        DatoRegistro("Nombre completo", nombre)
        DatoRegistro("Carrera", carrera)
        DatoRegistro("Turno", turno)
        DatoRegistro("Estatus", if (estatusActivo) "Activo" else "Inactivo")

        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al formulario")
        }
    }
}

@Composable
private fun DatoRegistro(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, fontWeight = FontWeight.Bold)
        Text(text = valor.ifBlank { "—" })
    }
}
