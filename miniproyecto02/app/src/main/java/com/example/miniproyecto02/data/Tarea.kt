package com.example.miniproyecto02.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cada objeto Tarea es una fila de la tabla "tareas"
@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val completada: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
