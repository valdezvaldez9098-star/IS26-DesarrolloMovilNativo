package com.example.practica08.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): define las operaciones CRUD sobre la tabla.
 * - Las lecturas devuelven un Flow, asi Compose se entera cuando los datos cambian.
 * - Las escrituras son "suspend" para ejecutarse en segundo plano (corrutinas).
 */
@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios")
    fun obtenerTodos(): Flow<List<Usuario>>

    @Insert
    suspend fun insertar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)

    // Reto: eliminar un registro
    @Delete
    suspend fun eliminar(usuario: Usuario)
}
