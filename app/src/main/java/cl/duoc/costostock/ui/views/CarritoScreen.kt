package cl.duoc.costostock.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.PasarelaPago
import cl.duoc.costostock.data.ResumenCompra
import cl.duoc.costostock.data.room.ItemCarrito
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.CarritoViewModel

/**
 * CO-09 Carrito.
 *
 * Muestra lo que el comprador va a retirar y lo que va a pagar. No hay
 * despacho: el cliente definio que Costo Stock es intermediario y el
 * retiro es siempre presencial, asi que no se pide direccion de entrega.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavHostController,
    viewModel: CarritoViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val ahora by viewModel.ahora.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Mi carrito") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { viewModel.vaciar() }) {
                            Text(
                                "Vaciar",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                BarraResumen(
                    resumen = resumen,
                    onPagar = { navController.navigate(Rutas.PAGO) }
                )
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            CarritoVacio(
                modifier = Modifier.padding(padding),
                onVerOfertas = { navController.popBackStack() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.oferta.id }) { item ->
                    LineaCarrito(
                        item = item,
                        ahora = ahora,
                        onSumar = { viewModel.sumarUno(item) },
                        onRestar = { viewModel.restarUno(item) },
                        onQuitar = { viewModel.quitar(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CarritoVacio(modifier: Modifier = Modifier, onVerOfertas: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tu carrito esta vacio",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Agrega excedentes cercanos antes de que se acaben.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onVerOfertas) { Text("Ver ofertas") }
    }
}

/** Una linea: foto, datos, cantidad y subtotal. */
@Composable
private fun LineaCarrito(
    item: ItemCarrito,
    ahora: Long,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onQuitar: () -> Unit
) {
    val vigente = item.oferta.estaVigente(ahora)
    val precio = item.oferta.precioVigente(ahora)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.oferta.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.oferta.imagenUrl,
                        contentDescription = item.oferta.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.oferta.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.oferta.comercio} · ${item.oferta.comuna}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Si la oferta vencio mientras estaba en el carrito, se
                // avisa: el precio subio y el comprador tiene que saberlo.
                if (!vigente) {
                    Text(
                        text = "Oferta expirada, precio normal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Selector(
                        cantidad = item.cantidad,
                        onSumar = onSumar,
                        onRestar = onRestar
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = PasarelaPago.formatearPesos(precio * item.cantidad),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onQuitar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Quitar del carrito",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Los botones de menos y mas con la cantidad al medio. */
@Composable
private fun Selector(cantidad: Int, onSumar: () -> Unit, onRestar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onRestar, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Quitar una unidad",
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = "$cantidad",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        IconButton(onClick = onSumar, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Agregar una unidad",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Totales y boton de pagar.
 *
 * La comision de la plataforma se muestra como linea aparte y no
 * escondida dentro del precio: el cliente pidio que el cobro sea
 * transparente para el comprador.
 */
@Composable
private fun BarraResumen(resumen: ResumenCompra, onPagar: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FilaTotal("Subtotal", PasarelaPago.formatearPesos(resumen.subtotal))

            FilaTotal(
                etiqueta = "Comision de plataforma " +
                    "(${CarritoRepository.PORCENTAJE_COMISION}%)",
                valor = PasarelaPago.formatearPesos(resumen.comision)
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = PasarelaPago.formatearPesos(resumen.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "🌱 Evitas ${"%.1f".format(resumen.co2Evitado)
                    .replace('.', ',')} kg de CO2 · Retiro presencial",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onPagar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Pagar ${PasarelaPago.formatearPesos(resumen.total)}",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}
