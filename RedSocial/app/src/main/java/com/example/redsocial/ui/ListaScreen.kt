package com.example.redsocial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.redsocial.data.Publicacion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

@Composable
fun ListaScreen(publicaciones: List<Publicacion>, onNueva: () -> Unit) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNueva) { Text("+") }
        }
    ) { padding ->
        if (publicaciones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no hay publicaciones")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(publicaciones, key = { it.id }) { PublicacionItem(it) }
            }
        }
    }
}

@Composable
fun PublicacionItem(p: Publicacion) {
    Card(Modifier.fillMaxWidth()) {
        AsyncImage(
            model = p.fotoUri,
            contentDescription = p.titulo,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Column(Modifier.padding(12.dp)) {
            Text(p.titulo, style = MaterialTheme.typography.titleMedium)
            Text(p.descripcion)
            Text(formatearFecha(p.fecha), style = MaterialTheme.typography.bodySmall)
        }
    }
}