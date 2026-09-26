package com.example.registroestudiantes.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.registroestudiantes.ui.DetailScreen
import com.example.registroestudiantes.ui.FormScreen

private const val ROUTE_FORM = "form"
private const val ROUTE_DETAIL = "detail/{matricula}/{nombre}/{carrera}/{turno}/{estatus}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_FORM) {

        // Pantalla 1: Formulario de Registro
        composable(ROUTE_FORM) {
            FormScreen { matricula, nombre, carrera, turno, estatusActivo ->
                val ruta = "detail/" +
                    "${Uri.encode(matricula)}/" +
                    "${Uri.encode(nombre)}/" +
                    "${Uri.encode(carrera)}/" +
                    "${Uri.encode(turno)}/" +
                    estatusActivo
                navController.navigate(ruta)
            }
        }

        // Pantalla 2: Confirmación / Detalle
        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(
                navArgument("matricula") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType },
                navArgument("carrera") { type = NavType.StringType },
                navArgument("turno") { type = NavType.StringType },
                navArgument("estatus") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            DetailScreen(
                matricula = args?.getString("matricula") ?: "",
                nombre = args?.getString("nombre") ?: "",
                carrera = args?.getString("carrera") ?: "",
                turno = args?.getString("turno") ?: "",
                estatusActivo = args?.getBoolean("estatus") ?: false,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
