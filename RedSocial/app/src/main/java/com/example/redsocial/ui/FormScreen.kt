package com.example.redsocial.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun FormScreen(
    onGuardar: (titulo: String, descripcion: String, fotoUri: String, fecha: Long) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var rutaCamara by rememberSaveable { mutableStateOf<String?>(null) }
    val fecha = rememberSaveable { System.currentTimeMillis() } // fecha automática

    // Cámara: la foto se guarda en un archivo de la app
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) rutaCamara?.let { fotoUri = Uri.fromFile(File(it)).toString() }
    }

    fun tomarFoto() {
        val archivo = File(context.filesDir, "foto_${System.currentTimeMillis()}.jpg")
        rutaCamara = archivo.absolutePath
        camara.launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo))
    }

    // Permiso de cámara en tiempo de ejecución
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tomarFoto()
        else Toast.makeText(context, "Se necesita el permiso de cámara", Toast.LENGTH_SHORT).show()
    }

    // Galería: el selector de fotos del sistema no requiere permisos.
    // Se copia la imagen a la app para que siga disponible al reiniciar.
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val archivo = File(context.filesDir, "foto_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                archivo.outputStream().use { entrada.copyTo(it) }
            }
            fotoUri = Uri.fromFile(archivo).toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva publicación", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it },
            label = { Text("Título") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion, onValueChange = { descripcion = it },
            label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth()
        )

        Text("Fecha: ${formatearFecha(fecha)}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                if (permitido) tomarFoto() else permisoCamara.launch(Manifest.permission.CAMERA)
            }) { Text("Tomar foto") }

            Button(onClick = {
                galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Galería") }
        }

        fotoUri?.let {
            AsyncImage(
                model = it, contentDescription = "Foto seleccionada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
            Button(
                onClick = { onGuardar(titulo, descripcion, fotoUri!!, fecha) },
                enabled = titulo.isNotBlank() && descripcion.isNotBlank() && fotoUri != null
            ) { Text("Guardar") }
        }
    }
}