package cl.duoc.costostock.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cl.duoc.costostock.data.PreferenciasManager

import cl.duoc.costostock.data.UsuarioRepository
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.ui.viewmodel.LoginViewModel
import cl.duoc.costostock.ui.viewmodel.RegistroViewModel
import cl.duoc.costostock.ui.views.CarritoScreen
import cl.duoc.costostock.ui.views.ConsultaPedidoScreen
import cl.duoc.costostock.ui.views.DetalleScreen
import cl.duoc.costostock.ui.views.LoginScreen
import cl.duoc.costostock.ui.views.OfertasScreen
import cl.duoc.costostock.ui.views.OnboardingScreen
import cl.duoc.costostock.ui.views.PerfilScreen
import cl.duoc.costostock.ui.views.PublicarOfertaScreen
import cl.duoc.costostock.ui.views.PagoScreen
import cl.duoc.costostock.ui.views.PantallaEnConstruccion
import cl.duoc.costostock.ui.views.RegistroScreen
import cl.duoc.costostock.ui.views.SplashScreen

private const val DURACION = 300

@Composable
fun AppNavigation(navController: NavHostController) {

    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val usuarioDao = database.usuarioDAO()
    val repository = UsuarioRepository(usuarioDao)
    val preferencias = PreferenciasManager(context)

    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeIn(tween(DURACION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeOut(tween(DURACION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeIn(tween(DURACION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeOut(tween(DURACION))
        }
    ) {
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
            val viewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return LoginViewModel(repository, preferencias) as T
                    }
                }
            )

            LoginScreen(
                viewModel = viewModel,
                _loginExitoso = {
                    navController.navigate(Rutas.OFERTAS) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onNavegarRegistro = {
                    navController.navigate(Rutas.REGISTRO)
                }
            )
        }

        composable(Rutas.REGISTRO) {
            val registroViewModel: RegistroViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RegistroViewModel(repository) as T
                    }
                }
            )

            RegistroScreen(
                viewModel = registroViewModel,
                onRegistroExitoso = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.REGISTRO) { inclusive = true }
                    }
                }
            )
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
        composable(
            route = Rutas.EDITAR_OFERTA,
            arguments = listOf(
                navArgument(Rutas.ARG_OFERTA_ID) { type = NavType.LongType }
            )
        ) { entrada ->
            val ofertaId = entrada.arguments?.getLong(Rutas.ARG_OFERTA_ID) ?: 0L
            PublicarOfertaScreen(navController = navController, ofertaId = ofertaId)
        }
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
