package com.example.redsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.ui.FormScreen
import com.example.redsocial.ui.ListaScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val vm: PublicacionViewModel = viewModel()
                val publicaciones by vm.publicaciones.collectAsState()
                var enFormulario by rememberSaveable { mutableStateOf(false) }

                BackHandler(enFormulario) { enFormulario = false }

                if (enFormulario) {
                    FormScreen(
                        onGuardar = { titulo, descripcion, foto, fecha ->
                            vm.guardar(titulo, descripcion, foto, fecha)
                            enFormulario = false
                        },
                        onCancelar = { enFormulario = false }
                    )
                } else {
                    ListaScreen(publicaciones, onNueva = { enFormulario = true })
                }
            }
        }
    }
}