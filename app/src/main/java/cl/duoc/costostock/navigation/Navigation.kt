package cl.duoc.costostock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.costostock.ui.theme.view.LoginProveedor

@Composable
fun AppNavigation(navController: NavHostController) {
        NavHost(
            navController = navController, startDestination = "login",
        ) {
            composable("login") { LoginProveedor(navController) }
        }
}