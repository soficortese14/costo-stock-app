package cl.duoc.costostock.ui.views

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

/**
 * Una lamina del onboarding. Es un simple contenedor de textos:
 * no tiene logica propia, por eso recibe todo por parametro.
 */
private data class Lamina(
    val titulo: String,
    val descripcion: String
)

private val laminas = listOf(
    Lamina(
        titulo = "Rescata lo que sobra",
        descripcion = "Productos de supermercados y comercios cercanos, " +
            "con descuentos de hasta 70% por estar proximos a vencer."
    ),
    Lamina(
        titulo = "Cerca de ti",
        descripcion = "Vemos las ofertas disponibles en tu zona y te mostramos " +
            "cuales puedes retirar sin moverte de tu comuna."
    ),
    Lamina(
        titulo = "Reduce tu huella",
        descripcion = "Cada compra evita que un producto termine en la basura. " +
            "Te mostramos cuantos kilos de CO2 ahorraste."
    )
)

/**
 * AC-02 Onboarding. Tres laminas deslizables y, en la ultima,
 * la solicitud del permiso de ubicacion.
 */
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val terminado by viewModel.terminado.collectAsState()
    val permisoConcedido by viewModel.permisoConcedido.collectAsState()

    // Controla en que lamina estamos y permite animar el cambio
    val estadoPager = rememberPagerState(pageCount = { laminas.size })
    val scope = rememberCoroutineScope()

    val esUltima = estadoPager.currentPage == laminas.size - 1

    // Abre el dialogo de permisos del sistema y devuelve la respuesta.
    // Android exige pedirlo asi: el usuario siempre puede decir que no.
    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado.values.any { it }
        viewModel.registrarRespuestaPermiso(concedido)
        // Concedido o no, el onboarding termina: la app funciona igual
        viewModel.terminarOnboarding()
    }

    // Cuando el ViewModel confirma que ya guardo la preferencia, navegamos
    LaunchedEffect(terminado) {
        if (terminado) {
            navController.navigate(Rutas.OFERTAS) {
                popUpTo(Rutas.ONBOARDING) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Boton de saltar, visible solo mientras no sea la ultima lamina.
            // AnimatedVisibility hace que aparezca y desaparezca con un fundido.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = !esUltima,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    TextButton(onClick = { viewModel.terminarOnboarding() }) {
                        Text("Saltar")
                    }
                }
            }

            // Las laminas. HorizontalPager ya trae la animacion de deslizamiento.
            HorizontalPager(
                state = estadoPager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pagina ->
                LaminaOnboarding(laminas[pagina])
            }

            IndicadorDePaginas(
                total = laminas.size,
                actual = estadoPager.currentPage
            )

            Spacer(Modifier.height(24.dp))

            // Aviso del permiso, solo en la ultima lamina
            AnimatedVisibility(
                visible = esUltima,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "Para mostrarte ofertas cercanas necesitamos " +
                        "acceder a tu ubicacion. Puedes usar la app sin concederlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (esUltima) {
                        // Ultima lamina: pedimos el permiso de ubicacion
                        solicitarPermiso.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    } else {
                        // Avanzamos a la siguiente lamina con animacion
                        scope.launch {
                            estadoPager.animateScrollToPage(estadoPager.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (esUltima) "Activar ubicacion y comenzar" else "Siguiente")
            }
        }
    }
}

/** El contenido de una lamina: titulo y descripcion centrados. */
@Composable
private fun LaminaOnboarding(lamina: Lamina) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = lamina.titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = lamina.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Los puntos de abajo. El punto activo se ensancha con una animacion,
 * para que el usuario vea en que lamina esta.
 */
@Composable
private fun IndicadorDePaginas(total: Int, actual: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { indice ->
            val activo = indice == actual

            // animateDpAsState interpola el ancho entre un valor y otro
            val ancho by animateDpAsState(
                targetValue = if (activo) 24.dp else 8.dp,
                animationSpec = tween(300),
                label = "anchoPunto"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = ancho, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (activo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}
