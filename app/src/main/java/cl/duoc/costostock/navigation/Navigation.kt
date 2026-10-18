package cl.duoc.costostock.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cl.duoc.costostock.ui.views.CarritoScreen
import cl.duoc.costostock.ui.views.ConsultaPedidoScreen
import cl.duoc.costostock.ui.views.DetalleScreen
import cl.duoc.costostock.ui.views.OfertasScreen
import cl.duoc.costostock.ui.views.OnboardingScreen
import cl.duoc.costostock.ui.views.PerfilScreen
import cl.duoc.costostock.ui.views.PublicarOfertaScreen
import cl.duoc.costostock.ui.views.PagoScreen
import cl.duoc.costostock.ui.views.PantallaEnConstruccion
import cl.duoc.costostock.ui.views.SplashScreen

/** Duracion de las transiciones, en milisegundos. */
private const val DURACION = 300

/**
 * navegacion de la aplicacion.
 *
 * Cada composable (ruta) asocia una ruta con la pantalla que se dibuja.
 *
 * Las cuatro transiciones se definen una sola vez aqui y aplican a todas
 * las pantallas: al avanzar, la nueva entra desde la derecha y la anterior
 * sale hacia la izquierda; con el boton atras ocurre al reves. Eso le da
 * al usuario la sensacion de avanzar y de volver dentro de la app.
 */
@Composable
fun AppNavigation(navController: NavHostController) {

    //navhost es el contenedor
    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH, //cual pantalla se muestra al abrir la app

        // Entra la pantalla nueva: se desliza desde la derecha
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeIn(tween(DURACION))
        },
        // Sale la pantalla anterior: se va hacia la izquierda
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeOut(tween(DURACION))
        },
        // Boton atras: la anterior vuelve a entrar desde la izquierda
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeIn(tween(DURACION))
        },
        // Boton atras: la actual sale hacia la derecha
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeOut(tween(DURACION))
        }
    ) {
        // El splash es la excepcion: se funde en vez de deslizarse.
        // Deslizarlo haria parecer que el usuario navego, cuando en
        // realidad la app recien esta decidiendo a donde entrar.
        composable(
            route = Rutas.SPLASH,
            exitTransition = { fadeOut(tween(DURACION)) }
        ) {
            SplashScreen(navController)
        }
        composable(Rutas.ONBOARDING) {
            OnboardingScreen(navController)
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
            OfertasScreen(navController)
        }
        composable(Rutas.PERFIL) {
            PerfilScreen(navController)
        }
        composable(Rutas.CARRITO) {
            CarritoScreen(navController)
        }
        composable(Rutas.PAGO) {
            PagoScreen(navController)
        }
        // El codigo viaja como parametro opcional en la ruta.
        composable(
            route = Rutas.CONSULTA_PEDIDO,
            arguments = listOf(
                navArgument(Rutas.ARG_CODIGO) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entrada ->
            ConsultaPedidoScreen(
                navController = navController,
                codigoInicial = entrada.arguments?.getString(Rutas.ARG_CODIGO)
            )
        }
        composable(Rutas.PUBLICAR_OFERTA) {
            PublicarOfertaScreen(navController)
        }
        // Editar reusa la misma pantalla: la diferencia es el id que llega.
        composable(
            route = Rutas.EDITAR_OFERTA,
            arguments = listOf(
                navArgument(Rutas.ARG_OFERTA_ID) { type = NavType.LongType }
            )
        ) { entrada ->
            val ofertaId = entrada.arguments?.getLong(Rutas.ARG_OFERTA_ID) ?: 0L
            PublicarOfertaScreen(navController = navController, ofertaId = ofertaId)
        }
        // El detalle recibe el id de la oferta dentro de la ruta.
        // navArgument declara el tipo, para que llegue como Long y no texto.
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(
                navArgument(Rutas.ARG_OFERTA_ID) { type = NavType.LongType }
            )
        ) { entrada ->
            val ofertaId = entrada.arguments?.getLong(Rutas.ARG_OFERTA_ID) ?: 0L
            DetalleScreen(navController = navController, ofertaId = ofertaId)
        }
    }
}
