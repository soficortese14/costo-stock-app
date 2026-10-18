package cl.duoc.costostock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.Ubicacion
import cl.duoc.costostock.ui.viewmodel.EstadoUbicacion
import cl.duoc.costostock.ui.viewmodel.OfertasViewModel

/**
 * CO-01 Ofertas. Muestra la ubicacion del usuario obtenida por GPS
 * y, mas adelante, el listado de ofertas cercanas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfertasScreen(
    navController: NavHostController,
    viewModel: OfertasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val radioKm by viewModel.radioKm.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Costo Stock") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TarjetaUbicacion(
                estado = estado,
                radioKm = radioKm,
                onReintentar = { viewModel.cargarUbicacion() }
            )

            Spacer(Modifier.height(24.dp))

            // Aqui va el listado de ofertas cuando este construido
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aun no hay ofertas publicadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Cuando los proveedores publiquen sus excedentes, " +
                        "apareceran aqui ordenados por cercania.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Tarjeta con la ubicacion. Dibuja un contenido distinto por cada
 * estado posible: el when es exhaustivo porque EstadoUbicacion es sealed.
 */
@Composable
private fun TarjetaUbicacion(
    estado: EstadoUbicacion,
    radioKm: Int,
    onReintentar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Ubicacion",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                when (estado) {
                    is EstadoUbicacion.Cargando -> ContenidoCargando()

                    is EstadoUbicacion.Disponible ->
                        ContenidoDisponible(estado.ubicacion, radioKm)

                    is EstadoUbicacion.SinPermiso ->
                        ContenidoSinUbicacion(
                            titulo = "Sin acceso a tu ubicacion",
                            detalle = "Mostramos todas las ofertas de la region.",
                            onReintentar = onReintentar
                        )

                    is EstadoUbicacion.NoDisponible ->
                        ContenidoSinUbicacion(
                            titulo = "No pudimos ubicarte",
                            detalle = "Revisa que el GPS este encendido.",
                            onReintentar = onReintentar
                        )
                }
            }
        }
    }
}

@Composable
private fun ContenidoCargando() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Buscando tu ubicacion...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Ubicacion obtenida. Se muestra la comuna cuando el Geocoder la resolvio;
 * si no, las coordenadas con cuatro decimales, que es lo que el usuario
 * puede leer sin que ocupe toda la tarjeta.
 */
@Composable
private fun ContenidoDisponible(ubicacion: Ubicacion, radioKm: Int) {
    // AnimatedVisibility hace que la ubicacion aparezca con un fundido
    // en vez de reemplazar de golpe el indicador de carga
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(200))
    ) {
        Column {
            Text(
                text = ubicacion.comuna ?: "Ubicacion detectada",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Ofertas a menos de $radioKm km · " +
                    "${"%.4f".format(ubicacion.latitud)}, " +
                    "${"%.4f".format(ubicacion.longitud)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ContenidoSinUbicacion(
    titulo: String,
    detalle: String,
    onReintentar: () -> Unit
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        TextButton(
            onClick = onReintentar,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Text("Reintentar")
        }
    }
}
