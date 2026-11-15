package com.example.practica14

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practica14.data.AppDatabase
import com.example.practica14.data.MarcadorEntity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

private const val ZOOM_INICIAL = 17.0
private const val CENTRO_INICIAL_LAT = 25.7905
private const val CENTRO_INICIAL_LON = -108.9875

private fun tienePermisoUbicacion(context: Context): Boolean {
    val fino = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val grueso = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    return fino == PackageManager.PERMISSION_GRANTED ||
        grueso == PackageManager.PERMISSION_GRANTED
}

private fun drawableABitmap(context: Context, drawableRes: Int, tamanoDp: Int = 48): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
        ?: throw IllegalArgumentException("No se encontró el drawable")
    val tamanoPx = (tamanoDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(tamanoPx, tamanoPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, tamanoPx, tamanoPx)
    drawable.draw(canvas)
    return bitmap
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaOSMScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getInstance(context) }
    val dao = remember { database.marcadorDao() }
    val marcadoresFlow = remember(dao) { dao.observarMarcadores() }
    val marcadores by marcadoresFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var permisoConcedido by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    var permisoDenegado by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf<Location?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var puntoPendiente by remember { mutableStateOf<GeoPoint?>(null) }
    var centradoInicial by remember { mutableStateOf(true) }

    val markerBitmap = remember(context) { drawableABitmap(context, R.drawable.ic_marker_red) }
    val personBitmap = remember(context) { drawableABitmap(context, R.drawable.ic_marker_red) }
    val directionBitmap = remember(context) { drawableABitmap(context, R.drawable.ic_arrow_red) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permisoConcedido = concedido
        permisoDenegado = !concedido
    }

    fun solicitarPermisos() {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion(context)) {
            solicitarPermisos()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val concedido = tienePermisoUbicacion(context)
        permisoConcedido = concedido
        if (concedido) permisoDenegado = false
    }

    DisposableEffect(permisoConcedido) {
        if (!permisoConcedido) {
            return@DisposableEffect onDispose { }
        }

        val cliente = LocationServices.getFusedLocationProviderClient(context)
        val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L)
            .setMinUpdateIntervalMillis(1_500L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                resultado.lastLocation?.let { ubicacion = it }
            }
        }

        cliente.requestLocationUpdates(solicitud, callback, Looper.getMainLooper())
        cliente.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) ubicacion = loc
        }

        onDispose {
            cliente.removeLocationUpdates(callback)
        }
    }

    LaunchedEffect(ubicacion, mapViewRef) {
        val mapView = mapViewRef ?: return@LaunchedEffect
        val loc = ubicacion ?: return@LaunchedEffect
        if (centradoInicial) {
            mapView.controller.setZoom(ZOOM_INICIAL)
            mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
            centradoInicial = false
        }
    }

    DisposableEffect(mapViewRef) {
        val mapView = mapViewRef
        mapView?.onResume()
        onDispose { mapView?.onPause() }
    }

    val eventsReceiver = remember {
        object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean = false

            override fun longPressHelper(p: GeoPoint): Boolean {
                puntoPendiente = p
                return true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Práctica 14 · GPS y Mapas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val loc = ubicacion
                    val mapView = mapViewRef
                    if (loc != null && mapView != null) {
                        mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    } else {
                        Toast.makeText(
                            context,
                            "Esperando ubicación GPS...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.OpenTopo)
                        tileProvider.clearTileCache()
                        setMultiTouchControls(true)
                        controller.setZoom(ZOOM_INICIAL)
                        controller.setCenter(GeoPoint(CENTRO_INICIAL_LAT, CENTRO_INICIAL_LON))
                        overlays.add(MapEventsOverlay(eventsReceiver))
                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    val overlayExistente = mapView.overlays
                        .filterIsInstance<MyLocationNewOverlay>()
                        .firstOrNull()

                    if (permisoConcedido && overlayExistente == null) {
                        val overlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(context),
                            mapView
                        )
                        overlay.setDirectionArrow(personBitmap, directionBitmap)

                        val activo = try {
                            overlay.enableMyLocation()
                        } catch (e: SecurityException) {
                            false
                        }

                        if (activo) {
                            overlay.enableFollowLocation()
                            overlay.runOnFirstFix {
                                mapView.post {
                                    overlay.myLocation?.let { punto ->
                                        mapView.controller.setZoom(ZOOM_INICIAL)
                                        mapView.controller.setCenter(punto)
                                    }
                                }
                            }
                            mapView.overlays.add(overlay)
                        }
                    } else if (!permisoConcedido) {
                        overlayExistente?.let {
                            it.disableMyLocation()
                            mapView.overlays.remove(it)
                        }
                    }

                    // Sincroniza los marcadores guardados en Room con el mapa
                    mapView.overlays.removeAll { it is Marker }
                    marcadores.forEach { marcador ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(marcador.latitud, marcador.longitud)
                            title = marcador.titulo
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = BitmapDrawable(context.resources, markerBitmap)
                        }
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                }
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val loc = ubicacion
                    if (loc != null) {
                        Text(
                            text = "Latitud: ${String.format(Locale.US, "%.6f", loc.latitude)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Longitud: ${String.format(Locale.US, "%.6f", loc.longitude)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Precisión: ${String.format(Locale.US, "%.0f", loc.accuracy)} m",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(text = "Obteniendo ubicación...", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mantén presionado el mapa para guardar un marcador",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!permisoConcedido) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Se requiere permiso de ubicación",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Concede el permiso para mostrar tu posición en el mapa.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { solicitarPermisos() }) {
                                Text("Reintentar")
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Ajustes")
                            }
                        }
                    }
                }
            }
        }
    }

    val punto = puntoPendiente
    if (punto != null) {
        var titulo by remember(punto) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { puntoPendiente = null },
            title = { Text("Nuevo marcador") },
            text = {
                Column {
                    Text(
                        text = "Lat: ${String.format(Locale.US, "%.6f", punto.latitude)}, " +
                            "Lon: ${String.format(Locale.US, "%.6f", punto.longitude)}",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título del punto") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nombre = titulo.ifBlank { "Marcador ${marcadores.size + 1}" }
                        scope.launch {
                            dao.insertar(
                                MarcadorEntity(
                                    latitud = punto.latitude,
                                    longitud = punto.longitude,
                                    titulo = nombre
                                )
                            )
                        }
                        Toast.makeText(context, "Marcador guardado", Toast.LENGTH_SHORT).show()
                        puntoPendiente = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { puntoPendiente = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
