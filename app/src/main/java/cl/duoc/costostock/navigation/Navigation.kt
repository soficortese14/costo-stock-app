package cl.duoc.costostock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.costostock.data.AppState
import cl.duoc.costostock.ui.ui.view.LoginProveedor

@Composable
fun AppNavigation(navController: NavHostController, appState : AppState) {
    NavHost(navController = navController, startDestination = "login"){
        composable("login") {LoginProveedor(navController, appState)}
        composable("registro") {RegistroScreen(navController, appState)}
        composable("notas") { NotasScreen(navController, appState) }
    }
}