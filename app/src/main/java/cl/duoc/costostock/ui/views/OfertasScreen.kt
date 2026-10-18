package cl.duoc.costostock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.data.Ubicacion
import cl.duoc.costostock.data.distanciaKm
import cl.duoc.costostock.ui.viewmodel.EstadoUbicacion
import cl.duoc.costostock.ui.viewmodel.OfertasViewModel
import cl.duoc.costostock.ui.viewmodel.OrdenCatalogo

/**
 * CO-01 Ofertas. Muestra la ubicacion obtenida por GPS y el catalogo
 * de excedentes guardado en Room, filtrable por categoria.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfertasScreen(
    navController: NavHostController,
    viewModel: OfertasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val radioKm by viewModel.radioKm.collectAsState()
    val ofertas by viewModel.ofertas.collectAsState()
    val categoria by viewModel.categoria.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val comuna by viewModel.comuna.collectAsState()
    val comunas by viewModel.comunas.collectAsState()
    val ahora by viewModel.ahora.collectAsState()
    val perfil by viewModel.perfilActivo.collectAsState()
    val orden by viewModel.orden.collectAsState()

    // Solo hay distancia que mostrar si se conoce donde esta el usuario.
    val ubicacion = (estado as? EstadoUbicacion.Disponible)?.ubicacion

    /*
     * Al cambiar el criterio de orden la lista se reordena entera, pero
     * la posicion del scroll se mantiene: el usuario quedaria mirando la
     * mitad de una lista nueva sin saber que paso. Se vuelve al inicio.
     */
    val estadoLista = rememberLazyListState()

    LaunchedEffect(orden) {
        estadoLista.animateScrollToItem(0)
    }

    val esProveedor = perfil == Perfil.PROVEEDOR

    /*
     * Restaurar el catalogo es destructivo: descarta las compras y las
     * ofertas publicadas en esta sesion. Por eso se pregunta antes, para
     * no perder una demostracion por un toque accidental.
     */
    var confirmarReinicio by remember { mutableStateOf(false) }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("Restaurar catalogo") },
            text = {
                Text(
                    "Las ofertas vuelven a quedar como al inicio. Se descartan " +
                        "las compras y las publicaciones de esta sesion."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reiniciarCatalogo()
                        confirmarReinicio = false
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarReinicio = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Text(
                        if (esProveedor) "Mis publicaciones" else "Costo Stock"
                    )
                },
                actions = {
                    IconButton(onClick = { confirmarReinicio = true }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Restaurar catalogo",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Rutas.PERFIL) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Mi perfil",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Publicar ofertas es una accion del proveedor: un comprador
            // no deberia poder cargar productos
            if (esProveedor) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.PUBLICAR_OFERTA) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Publicar")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TarjetaUbicacion(
                estado = estado,
                radioKm = radioKm,
                onCambiarRadio = { viewModel.cambiarRadio(it) },
                onReintentar = { viewModel.cargarUbicacion() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            CampoBusqueda(
                texto = busqueda,
                onCambiar = { viewModel.cambiarBusqueda(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            FilaCategorias(
                seleccionada = categoria,
                onSeleccionar = { viewModel.cambiarCategoria(it) }
            )

            Spacer(Modifier.height(8.dp))

            FilaComunas(
                comunas = comunas,
                seleccionada = comuna,
                onSeleccionar = { viewModel.cambiarComuna(it) }
            )

            Spacer(Modifier.height(4.dp))

            FilaOrden(
                seleccionado = orden,
                hayUbicacion = ubicacion != null,
                onSeleccionar = { viewModel.cambiarOrden(it) }
            )

            Spacer(Modifier.height(4.dp))

            if (ofertas.isEmpty()) {
                SinOfertas(busqueda)
            } else {
                LazyColumn(
                    state = estadoLista,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = ofertas,
                        key = { it.id }
                    ) { oferta ->
                        OfertaCard(
                            oferta = oferta,
                            ahora = ahora,
                            distanciaKm = ubicacion?.let {
                                distanciaKm(
                                    it.latitud,
                                    it.longitud,
                                    oferta.latitud,
                                    oferta.longitud
                                )
                            },
                            onClick = {
                                navController.navigate(Rutas.detalle(oferta.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Buscador por palabra clave.
 *
 * La pantalla solo reporta lo que se escribio: quien decide que ofertas
 * calzan es el ViewModel. Asi la regla se puede probar sin la interfaz.
 */
@Composable
private fun CampoBusqueda(
    texto: String,
    onCambiar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onCambiar,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Buscar producto o comercio") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            // La X solo aparece cuando hay algo que borrar.
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onCambiar("") }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Limpiar busqueda"
                    )
                }
            }
        }
    )
}

/** Chips de categoria. El seleccionado se pinta con el verde principal. */
@Composable
private fun FilaCategorias(
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(OfertaRepository.CATEGORIAS) { categoria ->
            FilterChip(
                selected = categoria == seleccionada,
                onClick = { onSeleccionar(categoria) },
                label = { Text(categoria) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Chips de comuna. La lista llega desde Room, no esta escrita a mano:
 * el cliente definio cobertura en la V Region y las comunas dependen de
 * donde haya ofertas publicadas.
 *
 * Se dibujan con el borde marcado en vez del relleno para que no compitan
 * con los chips de categoria, que son el filtro principal.
 */
@Composable
private fun FilaComunas(
    comunas: List<String>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(comunas) { comuna ->
            FilterChip(
                selected = comuna == seleccionada,
                onClick = { onSeleccionar(comuna) },
                label = { Text(comuna) },
                leadingIcon = {
                    if (comuna == seleccionada) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

/**
 * Criterio de orden del catalogo.
 *
 * Por defecto manda la vigencia, que es el gancho del negocio: lo que
 * esta por vencer primero. Ordenar por cercania solo tiene sentido si se
 * sabe donde esta el usuario, asi que sin ubicacion la opcion se ve pero
 * queda desactivada, en vez de desaparecer sin explicacion.
 */
@Composable
private fun FilaOrden(
    seleccionado: OrdenCatalogo,
    hayUbicacion: Boolean,
    onSeleccionar: (OrdenCatalogo) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ordenar:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilterChip(
            selected = seleccionado == OrdenCatalogo.VIGENCIA,
            onClick = { onSeleccionar(OrdenCatalogo.VIGENCIA) },
            label = { Text("Termina antes") }
        )

        FilterChip(
            selected = seleccionado == OrdenCatalogo.CERCANIA,
            onClick = { onSeleccionar(OrdenCatalogo.CERCANIA) },
            enabled = hayUbicacion,
            label = { Text("Mas cerca") },
            leadingIcon = {
                if (seleccionado == OrdenCatalogo.CERCANIA) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun SinOfertas(busqueda: String) {
    val buscando = busqueda.isNotBlank()

    // Ocupa solo el alto que necesita, no la pantalla completa: si se
    // centrara verticalmente, el teclado abierto taparia el mensaje justo
    // cuando el usuario esta escribiendo en el buscador.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (buscando) "Sin resultados para \"$busqueda\""
            else "No hay ofertas en esta categoria",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (buscando) "Revisa como esta escrito o prueba con otra palabra."
            else "Prueba con otra categoria o amplia el radio de busqueda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
    onCambiarRadio: (Int) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                        ContenidoDisponible(
                            ubicacion = estado.ubicacion,
                            radioKm = radioKm,
                            onCambiarRadio = onCambiarRadio
                        )

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
private fun ContenidoDisponible(
    ubicacion: Ubicacion,
    radioKm: Int,
    onCambiarRadio: (Int) -> Unit
) {
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

            /*
             * El cliente definio un radio de 1 a 30 km, con 15 por defecto.
             * Son 29 pasos para que el deslizador se detenga en kilometros
             * enteros y no en valores como 17,4 km.
             */
            Slider(
                value = radioKm.toFloat(),
                onValueChange = { onCambiarRadio(it.toInt()) },
                valueRange = 1f..30f,
                steps = 28
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
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Reintentar")
        }
    }
}
