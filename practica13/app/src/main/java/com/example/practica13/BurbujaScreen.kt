package com.example.practica13

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val SENSIBILIDAD = 50f
private const val RADIO_MAXIMO = 250f
private const val UMBRAL_SACUDIDA_G = 2.2f
private const val TIEMPO_ENTRE_SACUDIDAS_MS = 700L
private const val DURACION_SACUDIDA_MS = 600L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurbujaScreen(sensorManager: SensorManager) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var ejeX by remember { mutableFloatStateOf(0f) }
    var ejeY by remember { mutableFloatStateOf(0f) }
    var ejeZ by remember { mutableFloatStateOf(0f) }
    var sacudida by remember { mutableStateOf(false) }
    var ultimaSacudida by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val values = event?.values ?: return
                val x = values[0]
                val y = values[1]
                val z = values[2]

                ejeX = x
                ejeY = y
                ejeZ = z

                // 1. Posición de la burbuja a partir de X e Y
                val rawX = -x * SENSIBILIDAD
                val rawY = y * SENSIBILIDAD

                // 2. Distancia real al centro (Teorema de Pitágoras)
                val distancia = sqrt((rawX * rawX) + (rawY * rawY))

                // 3. Si se sale del radio, la limitamos a la orilla
                if (distancia > RADIO_MAXIMO) {
                    val proporcion = RADIO_MAXIMO / distancia
                    offsetX = rawX * proporcion
                    offsetY = rawY * proporcion
                } else {
                    offsetX = rawX
                    offsetY = rawY
                }

                // 4. Detección de sacudida: magnitud vectorial en unidades de gravedad
                val fuerzaG = sqrt((x * x) + (y * y) + (z * z)) / SensorManager.GRAVITY_EARTH
                val ahora = System.currentTimeMillis()
                if (fuerzaG > UMBRAL_SACUDIDA_G &&
                    ahora - ultimaSacudida > TIEMPO_ENTRE_SACUDIDAS_MS
                ) {
                    ultimaSacudida = ahora
                    sacudida = true
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (acelerometro != null) {
            sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(sacudida) {
        if (sacudida) {
            delay(DURACION_SACUDIDA_MS)
            sacudida = false
        }
    }

    BurbujaContent(
        offsetX = offsetX,
        offsetY = offsetY,
        ejeX = ejeX,
        ejeY = ejeY,
        ejeZ = ejeZ,
        sacudida = sacudida
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BurbujaContent(
    offsetX: Float,
    offsetY: Float,
    ejeX: Float,
    ejeY: Float,
    ejeZ: Float,
    sacudida: Boolean
) {
    val colorFondo by animateColorAsState(
        targetValue = if (sacudida) Color(0xFFB71C1C) else Color.DarkGray,
        animationSpec = tween(300),
        label = "colorFondo"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de Burbuja") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorFondo),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(50.dp).border(2.dp, Color.Red, CircleShape))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(40.dp)
                        .background(Color(0xFF00FF00), CircleShape)
                )
            }

            AnimatedVisibility(
                visible = sacudida,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                Surface(
                    color = Color(0xFFFFEB3B),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = "¡Sacudida detectada!",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Eje X: ${"%.2f".format(ejeX)}", color = Color.White, fontSize = 20.sp)
                Text("Eje Y: ${"%.2f".format(ejeY)}", color = Color.White, fontSize = 20.sp)
                Text("Eje Z: ${"%.2f".format(ejeZ)}", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BurbujaContentPreview() {
    MaterialTheme {
        BurbujaContent(
            offsetX = 0f,
            offsetY = 0f,
            ejeX = 0f,
            ejeY = 9.81f,
            ejeZ = 0f,
            sacudida = false
        )
    }
}
