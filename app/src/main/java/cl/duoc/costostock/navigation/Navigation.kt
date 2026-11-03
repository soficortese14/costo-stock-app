package cl.duoc.costostock.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cl.duoc.costostock.data.AppStateProveedor
import cl.duoc.costostock.data.UsuarioRepository
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.PreferenciasManager
import cl.duoc.costostock.ui.ui.view.LoginProveedor
import cl.duoc.costostock.ui.ui.view.RegistroProveedor
import cl.duoc.costostock.ui.viewmodel.LoginViewModel
import cl.duoc.costostock.ui.viewmodel.RegistroViewModel
import cl.duoc.costostock.ui.views.*

private const val DURACION = 300

@Composable
fun AppNavigation(navController: NavHostController, appStateProveedor: AppStateProveedor) {
    val context = LocalContext.current

    val preferenciasManager = remember { PreferenciasManager(context) }
    val usuarioDao = remember { AppDatabase.getInstance(context).UsuarioDAO() }
    val usuarioRepository = remember { UsuarioRepository(usuarioDao) }

    val viewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    return LoginViewModel(usuarioRepository, preferenciasManager) as T
                }
                if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
                    return RegistroViewModel(usuarioRepository) as T
                }
                throw IllegalArgumentException("Clase ViewModel desconocida")
            }
        }
    }

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
            val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = loginViewModel,
                navController = navController,
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
            val registroViewModel: RegistroViewModel = viewModel(factory = viewModelFactory)
            RegistroScreen(
                navController = navController,
                viewModel = registroViewModel
            )
        }

        composable(Rutas.POSTULACION_PROVEEDOR) {
            LoginProveedor(navController, appStateProveedor)
        }

        composable("registro_proveedor") {
            RegistroProveedor(navController, appStateProveedor)
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
