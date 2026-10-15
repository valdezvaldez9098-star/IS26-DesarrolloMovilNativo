package com.example.practica09

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun GaleriaScreen() {
    // Lista de URLs de imagenes de prueba (12 para probar scroll y memoria)
    val imagenes = listOf(
        "https://picsum.photos/id/1018/400/400",
        "https://picsum.photos/id/1015/400/400",
        "https://picsum.photos/id/1019/400/400",
        "https://picsum.photos/id/1020/400/400",
        "https://picsum.photos/id/1021/400/400",
        "https://picsum.photos/id/1022/400/400",
        "https://picsum.photos/id/1023/400/400",
        "https://picsum.photos/id/1024/400/400",
        "https://picsum.photos/id/10/400/400",
        "https://picsum.photos/id/11/400/400",
        "https://picsum.photos/id/12/400/400",
        "https://picsum.photos/id/13/400/400"
    )

    // Reto: estado con la imagen seleccionada (null = ninguna, dialogo cerrado)
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            text = "Mi Galería",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )

        // LazyVerticalGrid con columnas adaptativas: se ajusta al ancho de la pantalla
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(imagenes) { url ->
                ImagenItem(url = url, onClick = { imagenSeleccionada = url })
            }
        }
    }

    // Si hay una imagen seleccionada, se muestra la vista de detalle
    imagenSeleccionada?.let { url ->
        DetalleImagenDialog(url = url, onDismiss = { imagenSeleccionada = null })
    }
}

@Composable
fun ImagenItem(url: String, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }, // Reto: la imagen responde al toque
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        // AsyncImage de Coil maneja la carga de red, el cache y el ciclo de vida
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true) // transicion suave al terminar de cargar
                .build(),
            contentDescription = "Imagen de la galería",
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFFE0E0E0)), // gris mientras carga
            error = ColorPainter(Color(0xFFEF9A9A)),        // rojo claro si falla
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Vista de detalle: muestra la imagen ampliada en un cuadro de dialogo.
 * Se descarga una version mas grande y se muestra un indicador de progreso
 * mientras carga (o un mensaje si ocurre un error).
 */
@Composable
fun DetalleImagenDialog(url: String, onDismiss: () -> Unit) {
    val urlGrande = url.replace("/400/400", "/1000/1000")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SubcomposeAsyncImage(
                    model = urlGrande,
                    contentDescription = "Imagen ampliada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se pudo cargar la imagen")
                        }
                    }
                )
                TextButton(onClick = onDismiss, modifier = Modifier.padding(4.dp)) {
                    Text("Cerrar")
                }
            }
        }
    }
}
