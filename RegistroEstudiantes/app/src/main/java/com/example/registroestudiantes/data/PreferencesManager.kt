package com.example.registroestudiantes.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona el guardado y la lectura del último registro
 * usando SharedPreferences (clave-valor).
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("RegistroEstudiantesPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MATRICULA = "key_matricula"
        private const val KEY_NOMBRE = "key_nombre"
        private const val KEY_CARRERA = "key_carrera"
        private const val KEY_TURNO = "key_turno"
        private const val KEY_ESTATUS = "key_estatus"
    }

    // --- Guardado ---
    fun saveStudent(
        matricula: String,
        nombre: String,
        carrera: String,
        turno: String,
        estatusActivo: Boolean
    ) {
        prefs.edit()
            .putString(KEY_MATRICULA, matricula)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_CARRERA, carrera)
            .putString(KEY_TURNO, turno)
            .putBoolean(KEY_ESTATUS, estatusActivo)
            .apply()
    }

    // --- Lectura ---
    fun getMatricula(): String = prefs.getString(KEY_MATRICULA, "") ?: ""
    fun getNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""
    fun getCarrera(): String = prefs.getString(KEY_CARRERA, "") ?: ""
    fun getTurno(): String = prefs.getString(KEY_TURNO, "Matutino") ?: "Matutino"
    fun getEstatus(): Boolean = prefs.getBoolean(KEY_ESTATUS, true)
}
