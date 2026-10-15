package com.example.miniproyecto02.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.miniproyecto02.data.Tarea
import com.example.miniproyecto02.domain.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

private enum class Orden { FECHA, ESTADO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).tareaDao() }
    val scope = rememberCoroutineScope()

    // collectAsState() vuelve a dibujar la lista cada vez que cambia la base de datos
    val tareas by dao.obtenerTodas().collectAsState(initial = emptyList())

    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }
    var orden by remember { mutableStateOf(Orden.FECHA) }

    // Fecha: la más reciente primero. Estado: pendientes primero, luego completadas
    val tareasOrdenadas = when (orden) {
        Orden.FECHA -> tareas.sortedByDescending { it.fecha }
        Orden.ESTADO -> tareas.sortedWith(compareBy<Tarea> { it.completada }.thenByDescending { it.fecha })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ordenar por fecha") },
                            onClick = {
                                orden = Orden.FECHA
                                menuExpandido = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por estado") },
                            onClick = {
                                orden = Orden.ESTADO
                                menuExpandido = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Borrar completadas") },
                            onClick = {
                                scope.launch { dao.eliminarCompletadas() }
                                menuExpandido = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tareasOrdenadas, key = { it.id }) { tarea ->
                TareaItem(
                    tarea = tarea,
                    onCompletar = { scope.launch { dao.actualizar(tarea.copy(completada = it)) } },
                    onEditar = { tareaEditando = tarea },
                    onEliminar = { scope.launch { dao.eliminar(tarea) } }
                )
            }
        }
    }

    // Diálogo para agregar
    if (mostrarDialogo) {
        TareaDialog(
            onGuardar = { titulo ->
                scope.launch { dao.insertar(Tarea(titulo = titulo)) }
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    // Diálogo para editar (el mismo, con el título actual)
    tareaEditando?.let { tarea ->
        TareaDialog(
            tituloInicial = tarea.titulo,
            onGuardar = { titulo ->
                scope.launch { dao.actualizar(tarea.copy(titulo = titulo)) }
                tareaEditando = null
            },
            onCancelar = { tareaEditando = null }
        )
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onCompletar: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = tarea.completada, onCheckedChange = onCompletar)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null
                )
                Text(formatoFecha.format(Date(tarea.fecha)), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

// Sirve para agregar (tituloInicial vacío) y para editar
@Composable
fun TareaDialog(
    tituloInicial: String = "",
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(tituloInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (tituloInicial.isEmpty()) "Nueva tarea" else "Editar tarea") },
        text = {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (titulo.isNotBlank()) onGuardar(titulo.trim()) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
