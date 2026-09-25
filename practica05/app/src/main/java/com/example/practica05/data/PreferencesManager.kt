package com.example.practica05.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase encargada de gestionar el almacenamiento con SharedPreferences.
 * Recibe el Context de la aplicacion para poder acceder al archivo
 * de preferencias interno del sistema.
 */
class PreferencesManager(context: Context) {

    // Archivo XML interno donde Android guarda las preferencias
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

    // Claves (Keys)
    companion object {
        const val KEY_USERNAME = "key_username"
        const val KEY_NOTIFICATIONS = "key_notifications"
        const val KEY_DARK_THEME = "key_dark_theme"
    }

    // --- METODOS DE GUARDADO (Escritura) ---
    fun saveSettings(username: String, notifications: Boolean, darkTheme: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, username)
        editor.putBoolean(KEY_NOTIFICATIONS, notifications)
        editor.putBoolean(KEY_DARK_THEME, darkTheme)
        editor.apply() // Guarda de forma asincrona en disco
    }

    // --- METODOS DE CONSULTA (Lectura) ---
    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    fun getNotifications(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, false)
    }

    fun getDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    // Borrado de preferencias (para el boton de Reinicio)
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}
