package cl.duoc.costostock.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import cl.duoc.costostock.data.room.OfertaEntity
import cl.duoc.costostock.ui.theme.TerracotaTexto
import cl.duoc.costostock.ui.viewmodel.DetalleViewModel
import cl.duoc.costostock.ui.viewmodel.EstadoDetalle
import java.util.Locale

/**
 * CO-05 Detalle de producto.
 *
 * Recibe el id por la ruta, pide la oferta al ViewModel y muestra toda
 * la informacion con una barra fija abajo para comprar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    navController: NavHostController,
    ofertaId: Long,
    viewModel: DetalleViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val ahora by viewModel.ahora.collectAsState()
    val comprando by viewModel.comprando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val snackbar = remember { SnackbarHostState() }

    // Se carga una sola vez por id: si el usuario entra a otro producto,
    // la llave cambia y se vuelve a ejecutar.
    LaunchedEffect(ofertaId) {
        viewModel.cargar(ofertaId)
    }

    // Muestra el mensaje de compra y lo limpia, para que no reaparezca
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val actual = estado) {
            is EstadoDetalle.Cargando -> CargandoDetalle(Modifier.padding(padding))

            is EstadoDetalle.NoEncontrada -> NoEncontrada(Modifier.padding(padding))

            is EstadoDetalle.Listo -> ContenidoDetalle(
                oferta = actual.oferta,
                ahora = ahora,
                comprando = comprando,
                onComprar = { viewModel.comprar(actual.oferta) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun CargandoDetalle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun NoEncontrada(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No encontramos esta oferta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Puede que haya sido retirada del catalogo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * El contenido real. La informacion va en una columna con scroll y el
 * boton de comprar queda fijo abajo, siempre visible.
 */
@Composable
private fun ContenidoDetalle(
    oferta: OfertaEntity,
    ahora: Long,
    comprando: Boolean,
    onComprar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vigente = oferta.estaVigente(ahora)
    val precio = oferta.precioVigente(ahora)

    Column(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ImagenGrande(oferta = oferta, vigente = vigente)

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = oferta.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(12.dp))

                PrecioDetalle(
                    precioVigente = precio,
                    precioOriginal = oferta.precioOriginal,
                    vigente = vigente,
                    descuento = oferta.descuento
                )

                Spacer(Modifier.height(16.dp))

                ContadorGrande(finVigencia = oferta.finVigencia, ahora = ahora)

                Spacer(Modifier.height(16.dp))

                StockDetalle(stock = oferta.stockActual)

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Descripcion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = oferta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                TarjetaRetiro(oferta = oferta)

                Spacer(Modifier.height(16.dp))

                TarjetaImpacto(kilos = oferta.co2Evitado)

                Spacer(Modifier.height(16.dp))
            }
        }

        BarraComprar(
            precio = precio,
            vigente = vigente,
            sinStock = oferta.stockActual <= 0,
            comprando = comprando,
            onComprar = onComprar
        )
    }
}

@Composable
private fun ImagenGrande(oferta: OfertaEntity, vigente: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (oferta.imagenUrl.isNotBlank()) {
            AsyncImage(
                model = oferta.imagenUrl,
                contentDescription = oferta.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (vigente && oferta.descuento > 0) {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "-${oferta.descuento}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun PrecioDetalle(
    precioVigente: Int,
    precioOriginal: Int,
    vigente: Boolean,
    descuento: Int
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatearPesosDetalle(precioVigente),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (vigente && precioOriginal > precioVigente) {
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = formatearPesosDetalle(precioOriginal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = "Ahorras ${formatearPesosDetalle(precioOriginal - precioVigente)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun ContadorGrande(finVigencia: Long, ahora: Long) {
    val restante = finVigencia - ahora
    val urgente = restante in 1 until 15 * 60_000L

    val color by animateColorAsState(
        targetValue =
            if (restante <= 0 || urgente) MaterialTheme.colorScheme.error
            else TerracotaTexto,
        animationSpec = tween(600),
        label = "colorContadorDetalle"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = if (restante <= 0) "Oferta expirada, precio normal"
            else "Termina en ${formatearTiempoDetalle(restante)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun StockDetalle(stock: Int) {
    val bajo = stock in 1..3
    val color =
        if (bajo || stock <= 0) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    Column {
        LinearProgressIndicator(
            progress = { stock.coerceAtMost(20) / 20f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                stock <= 0 -> "Sin stock disponible"
                stock == 1 -> "Queda solo 1 unidad"
                bajo -> "Quedan solo $stock unidades"
                else -> "Quedan $stock unidades"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

/** Datos del comercio y del punto de retiro. */
@Composable
private fun TarjetaRetiro(oferta: OfertaEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Punto de retiro",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            FilaConIcono(
                icono = { tint ->
                    Icon(
                        Icons.Filled.Storefront,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                },
                texto = oferta.comercio
            )

            Spacer(Modifier.height(6.dp))

            FilaConIcono(
                icono = { tint ->
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                },
                texto = "${oferta.direccion}, ${oferta.comuna}"
            )

            Spacer(Modifier.height(6.dp))

            FilaConIcono(
                icono = { tint ->
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                },
                texto = "Retiro presencial, sin despacho"
            )
        }
    }
}

@Composable
private fun FilaConIcono(
    icono: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icono(MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** El unico bloque verde de la pantalla: el impacto ambiental. */
@Composable
private fun TarjetaImpacto(kilos: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌱 Impacto ambiental",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Al rescatar este producto evitas " +
                    "${formatearKilosDetalle(kilos)} kg de CO2 " +
                    "y lo sacas de la basura.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/** Barra fija inferior con el total y el boton de compra. */
@Composable
private fun BarraComprar(
    precio: Int,
    vigente: Boolean,
    sinStock: Boolean,
    comprando: Boolean,
    onComprar: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatearPesosDetalle(precio),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onComprar,
                enabled = !sinStock && !comprando,
                modifier = Modifier.height(50.dp)
            ) {
                if (comprando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when {
                            sinStock -> "Sin stock"
                            vigente -> "Comprar ahora"
                            else -> "Comprar a precio normal"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatearPesosDetalle(monto: Int): String {
    val texto = monto.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$$texto"
}

private fun formatearKilosDetalle(kilos: Double): String =
    String.format(Locale.US, "%.1f", kilos).replace('.', ',')

private fun formatearTiempoDetalle(milisegundos: Long): String {
    val totalSegundos = milisegundos / 1000
    val dias = totalSegundos / 86_400
    val horas = (totalSegundos % 86_400) / 3600
    val minutos = (totalSegundos % 3600) / 60
    val segundos = totalSegundos % 60

    /*
     * La unidad mas grande manda. Una oferta a varios dias mostraba antes
     * "119h 53m 52s", que no se lee: a esa escala los segundos no aportan
     * y lo que importa es si quedan dias u horas.
     */
    return when {
        dias > 0 -> String.format(Locale.US, "%dd %02dh", dias, horas)
        horas > 0 -> String.format(Locale.US, "%02dh %02dm %02ds", horas, minutos, segundos)
        else -> String.format(Locale.US, "%02dm %02ds", minutos, segundos)
    }
}
