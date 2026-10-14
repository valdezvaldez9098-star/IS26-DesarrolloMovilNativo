package com.example.practica08.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad: cada objeto Usuario es una fila de la tabla "usuarios"
 * en la base de datos SQLite local.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Room asigna el id automaticamente
    val nombre: String
)
