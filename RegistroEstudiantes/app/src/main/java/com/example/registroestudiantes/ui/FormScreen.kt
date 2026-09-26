package com.example.registroestudiantes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.registroestudiantes.data.PreferencesManager

private val carreras = listOf(
    "Ingeniería de Software",
    "Ingeniería en Sistemas",
    "Licenciatura en Enfermería",
    "Ingeniería Industrial"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(onRegistrar: (String, String, String, String, Boolean) -> Unit) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    // Estados del formulario, precargados desde SharedPreferences
    var matricula by remember { mutableStateOf(preferencesManager.getMatricula()) }
    var nombre by remember { mutableStateOf(preferencesManager.getNombre()) }
    var carrera by remember { mutableStateOf(preferencesManager.getCarrera().ifBlank { carreras[0] }) }
    var turno by remember { mutableStateOf(preferencesManager.getTurno()) }
    var estatusActivo by remember { mutableStateOf(preferencesManager.getEstatus()) }
    var carreraExpandida by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Registro de Estudiante", style = MaterialTheme.typography.headlineMedium)

        // Matrícula
        OutlinedTextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Nombre completo
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Carrera (Exposed Dropdown Menu)
        ExposedDropdownMenuBox(
            expanded = carreraExpandida,
            onExpandedChange = { carreraExpandida = it }
        ) {
            OutlinedTextField(
                value = carrera,
                onValueChange = {},
                readOnly = true,
                label = { Text("Carrera") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carreraExpandida) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = carreraExpandida,
                onDismissRequest = { carreraExpandida = false }
            ) {
                carreras.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            carrera = opcion
                            carreraExpandida = false
                        }
                    )
                }
            }
        }

        // Turno (RadioButton)
        Text(text = "Turno")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = turno == "Matutino", onClick = { turno = "Matutino" })
            Text("Matutino", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = turno == "Vespertino", onClick = { turno = "Vespertino" })
            Text("Vespertino")
        }

        // Estatus (Switch)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (estatusActivo) "Estatus: Activo" else "Estatus: Inactivo")
            Switch(checked = estatusActivo, onCheckedChange = { estatusActivo = it })
        }

        // Botón Registrar: guarda en SharedPreferences y navega a Detalle
        Button(
            onClick = {
                preferencesManager.saveStudent(matricula, nombre, carrera, turno, estatusActivo)
                onRegistrar(matricula, nombre, carrera, turno, estatusActivo)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }
    }
}
