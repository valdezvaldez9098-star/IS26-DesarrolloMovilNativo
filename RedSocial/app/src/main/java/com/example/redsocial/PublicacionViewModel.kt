package com.example.redsocial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redsocial.data.AppDatabase
import com.example.redsocial.data.Publicacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PublicacionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.obtener(app).publicacionDao()

    // La lista se actualiza sola cada vez que cambia la base de datos
    val publicaciones: StateFlow<List<Publicacion>> = dao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardar(titulo: String, descripcion: String, fotoUri: String, fecha: Long) {
        viewModelScope.launch {
            dao.insertar(Publicacion(titulo = titulo, descripcion = descripcion, fotoUri = fotoUri, fecha = fecha))
        }
    }
}