package cl.duoc.costostock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.costostock.ui.views.PantallaEnConstruccion
import cl.duoc.costostock.ui.views.SplashScreen

/**
 * navegacion de la aplicacion.
 *
 * Cada composable (ruta) asocia una ruta con la pantalla que se dibuja
 */
@Composable
fun AppNavigation(navController: NavHostController) {

    //navhost es el contenedor
    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH //cual pantalla se muestra al abrir la app
    ) {
        composable(Rutas.SPLASH) {
            SplashScreen(navController)
        }
        composable(Rutas.ONBOARDING) {
            PantallaEnConstruccion("Onboarding y permisos", "Sofia")
        }
        composable(Rutas.LOGIN) {
            PantallaEnConstruccion("Login", "Integrante 2")
        }
        composable(Rutas.REGISTRO) {
            PantallaEnConstruccion("Registro de comprador", "Integrante 3")
        }
        composable(Rutas.POSTULACION_PROVEEDOR) {
            PantallaEnConstruccion("Postulacion de proveedor", "Integrante 4")
        }
        composable(Rutas.OFERTAS) {
            PantallaEnConstruccion("Ofertas", "Por asignar")
        }
    }
}
