package cl.duoc.costostock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import cl.duoc.costostock.data.ProductoExterno
import cl.duoc.costostock.data.ResultadoBusqueda
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import cl.duoc.costostock.data.AlmacenImagenes
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.PublicarOfertaViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * PR-03 Publicar oferta.
 *
 * Formulario del proveedor para cargar un excedente. Toda la validacion
 * la calcula el ViewModel: esta pantalla solo dibuja los errores que
 * recibe, sin decidir nada por su cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarOfertaScreen(
    navController: NavHostController,
    ofertaId: Long? = null,
    viewModel: PublicarOfertaViewModel = viewModel()
) {
    val formulario by viewModel.formulario.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val publicada by viewModel.publicada.collectAsState()
    val eliminada by viewModel.eliminada.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val codigoBarras by viewModel.codigoBarras.collectAsState()
    val resultadoApi by viewModel.resultadoApi.collectAsState()
    val buscandoEnApi by viewModel.buscandoEnApi.collectAsState()

    // Un id significa que se esta editando. Es lo unico que separa los dos
    // modos: los campos, las validaciones y el guardado son los mismos.
    val editando = ofertaId != null

    var confirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(ofertaId) {
        if (ofertaId != null) viewModel.cargarParaEditar(ofertaId)
    }

    val snackbar = remember { SnackbarHostState() }

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    /*
     * Ruta donde la camara va a escribir. Se guarda aparte porque el
     * resultado de TakePicture solo dice si salio bien o no: el archivo
     * lo elegimos nosotros antes de lanzarla.
     */
    var rutaPendiente by remember { mutableStateOf<String?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        when {
            exito && ruta != null -> viewModel.cambiarImagen(ruta)
            // Si el usuario cancela, el archivo vacio queda ocupando espacio
            ruta != null -> AlmacenImagenes.borrar(ruta)
        }
        rutaPendiente = null
    }

    fun abrirCamara() {
        val (uri, ruta) = AlmacenImagenes.prepararFotoDeCamara(contexto)
        rutaPendiente = ruta
        lanzadorCamara.launch(uri)
    }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else viewModel.avisar("Sin permiso de camara no se puede tomar la foto")
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            alcance.launch {
                val ruta = AlmacenImagenes.copiarDesdeGaleria(contexto, uri)
                if (ruta != null) viewModel.cambiarImagen(ruta)
                else viewModel.avisar("No se pudo leer esa imagen")
            }
        }
    }

    // Avisa de campos pendientes o producto duplicado
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    // Al guardar en Room, vuelve al catalogo: la oferta nueva aparece
    // sola porque el DAO devuelve un Flow
    LaunchedEffect(publicada) {
        if (publicada) navController.popBackStack()
    }

    // Al borrar, la oferta ya no existe: hay que salir de su detalle
    // tambien, o quedaria mostrando algo que no esta en la base.
    LaunchedEffect(eliminada) {
        if (eliminada) {
            navController.popBackStack(Rutas.OFERTAS, inclusive = false)
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar oferta") },
            text = {
                Text(
                    "La oferta se retira del catalogo y no se puede recuperar."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar()
                        confirmarEliminar = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
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
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text(if (editando) "Editar oferta" else "Publicar oferta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Titulo("Foto del producto")

            SeccionFoto(
                imagenUrl = formulario.imagenUrl,
                onCamara = {
                    val concedido = ContextCompat.checkSelfPermission(
                        contexto,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (concedido) abrirCamara()
                    else lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                },
                onGaleria = {
                    lanzadorGaleria.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onQuitar = { viewModel.quitarImagen() }
            )

            Spacer(Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(20.dp))

            Titulo("Producto")

            /*
             * Busqueda en la API externa.
             *
             * Va antes del titulo porque es el atajo: si el producto
             * esta en Open Food Facts, rellena titulo, peso y foto de
             * una vez. Si no esta, o no hay internet, el formulario
             * sigue funcionando a mano exactamente como antes.
             */
            BuscadorApiExterna(
                codigo = codigoBarras,
                buscando = buscandoEnApi,
                resultado = resultadoApi,
                onCodigoCambia = viewModel::cambiarCodigoBarras,
                onBuscar = viewModel::buscarEnApi,
                onUsar = viewModel::usarProductoDeApi,
                onDescartar = viewModel::descartarResultadoApi
            )

            Spacer(Modifier.height(16.dp))

            CampoTextoValidado(
                valor = formulario.titulo,
                onValorCambia = viewModel::cambiarTitulo,
                etiqueta = "Titulo del producto",
                error = formulario.errorTitulo,
                iconoInicial = Icons.Filled.Title,
                placeholder = "Pack 6 yogures naturales"
            )

            Spacer(Modifier.height(12.dp))

            CampoTextoValidado(
                valor = formulario.descripcion,
                onValorCambia = viewModel::cambiarDescripcion,
                etiqueta = "Descripcion",
                error = formulario.errorDescripcion,
                placeholder = "Por que esta en liquidacion",
                lineaUnica = false,
                maxLineas = 3
            )

            Spacer(Modifier.height(16.dp))

            Etiqueta("Categoria")
            FilaDeChips(
                opciones = PublicarOfertaViewModel.CATEGORIAS,
                seleccionada = formulario.categoria,
                onSeleccionar = viewModel::cambiarCategoria
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(20.dp))

            Titulo("Precio y stock")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTextoValidado(
                    valor = formulario.precioOriginal,
                    onValorCambia = viewModel::cambiarPrecioOriginal,
                    etiqueta = "P. original",
                    error = formulario.errorPrecioOriginal,
                    iconoInicial = Icons.Filled.AttachMoney,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoTextoValidado(
                    valor = formulario.precioOferta,
                    onValorCambia = viewModel::cambiarPrecioOferta,
                    etiqueta = "P. oferta",
                    error = formulario.errorPrecioOferta,
                    iconoInicial = Icons.Filled.AttachMoney,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            // Vista previa del descuento, solo si los dos precios son validos
            AnimatedVisibility(
                visible = formulario.errorPrecioOriginal == null &&
                    formulario.errorPrecioOferta == null &&
                    formulario.precioOriginal.isNotBlank() &&
                    formulario.precioOferta.isNotBlank(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                VistaPreviaDescuento(
                    original = formulario.precioOriginal.toIntOrNull() ?: 0,
                    oferta = formulario.precioOferta.toIntOrNull() ?: 0
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTextoValidado(
                    valor = formulario.stock,
                    onValorCambia = viewModel::cambiarStock,
                    etiqueta = "Stock",
                    error = formulario.errorStock,
                    iconoInicial = Icons.Filled.Inventory2,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoTextoValidado(
                    valor = formulario.horasVigencia,
                    onValorCambia = viewModel::cambiarHoras,
                    etiqueta = "Horas",
                    error = formulario.errorHoras,
                    iconoInicial = Icons.Filled.Schedule,
                    tipoTeclado = KeyboardType.Number,
                    soporte = "Max 72",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(20.dp))

            Titulo("Punto de retiro")

            CampoTextoValidado(
                valor = formulario.comercio,
                onValorCambia = viewModel::cambiarComercio,
                etiqueta = "Nombre del comercio",
                error = formulario.errorComercio,
                iconoInicial = Icons.Filled.Storefront
            )

            Spacer(Modifier.height(12.dp))

            CampoTextoValidado(
                valor = formulario.direccion,
                onValorCambia = viewModel::cambiarDireccion,
                etiqueta = "Direccion",
                error = formulario.errorDireccion,
                iconoInicial = Icons.Filled.LocationOn,
                placeholder = "Av. Valparaiso 782"
            )

            Spacer(Modifier.height(16.dp))

            Etiqueta("Comuna")
            FilaDeChips(
                opciones = PublicarOfertaViewModel.COMUNAS,
                seleccionada = formulario.comuna,
                onSeleccionar = viewModel::cambiarComuna
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(20.dp))

            Titulo("Impacto ambiental")

            CampoTextoValidado(
                valor = formulario.pesoKg,
                onValorCambia = viewModel::cambiarPeso,
                etiqueta = "Peso aproximado (kg)",
                error = formulario.errorPeso,
                iconoInicial = Icons.Filled.Scale,
                tipoTeclado = KeyboardType.Decimal,
                soporte = "Con esto calculamos el CO2 evitado"
            )

            AnimatedVisibility(
                visible = formulario.errorPeso == null && formulario.pesoKg.isNotBlank(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                VistaPreviaCo2(
                    kilos = viewModel.co2Estimado(),
                    cuenta = viewModel.co2Explicado(),
                    fuente = viewModel.fuenteDelFactor()
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { viewModel.publicar() },
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (editando) "Guardar cambios" else "Publicar oferta",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Eliminar solo tiene sentido sobre una oferta que ya existe.
            // Va aparte y en rojo: es destructivo y no debe confundirse
            // con el boton de guardar.
            if (editando) {
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { confirmarEliminar = true },
                    enabled = !guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Eliminar oferta", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Foto del producto: vista previa y las dos formas de conseguirla.
 *
 * La foto es opcional. El cliente pidio que publicar sea rapido, y
 * obligar a una imagen frenaria a un comercio que esta cerrando y quiere
 * liquidar lo que le quedo.
 */
@Composable
private fun SeccionFoto(
    imagenUrl: String,
    onCamara: () -> Unit,
    onGaleria: () -> Unit,
    onQuitar: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = "Foto del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Sin foto (opcional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCamara,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text("Camara")
            }

            OutlinedButton(
                onClick = onGaleria,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text("Galeria")
            }
        }

        // Quitar solo aparece si hay algo que quitar
        if (imagenUrl.isNotBlank()) {
            TextButton(onClick = onQuitar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text("Quitar foto")
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/** Chips de seleccion unica, para categoria y comuna. */
@Composable
private fun FilaDeChips(
    opciones: List<String>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                label = { Text(opcion) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/** Muestra el descuento que resulta de los dos precios escritos. */
@Composable
private fun VistaPreviaDescuento(original: Int, oferta: Int) {
    val descuento = if (original > 0) ((original - oferta) * 100) / original else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Descuento: $descuento%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "El comprador ahorra $${formatearMiles(original - oferta)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * El unico bloque verde del formulario: el impacto ambiental.
 *
 * Muestra tres niveles y no solo el numero: el resultado, la
 * multiplicacion que lo produce y de donde sale el factor. Antes
 * aparecia unicamente el resultado y no habia forma de saber si era un
 * calculo o un valor escrito a mano, que fue justamente la critica.
 */
@Composable
private fun VistaPreviaCo2(kilos: Double, cuenta: String, fuente: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🌱 Esta oferta evitaria " +
                    "${String.format(Locale.US, "%.1f", kilos).replace('.', ',')} " +
                    "kg de CO2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            if (cuenta.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = cuenta,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (fuente.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Factor: $fuente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

private fun formatearMiles(monto: Int): String =
    monto.toString().reversed().chunked(3).joinToString(".").reversed()

/**
 * Buscador de productos en Open Food Facts.
 *
 * Es el unico punto de la app que consume la API externa. Muestra lo
 * que encontro y espera: no rellena el formulario solo, porque los
 * datos de Open Food Facts son colaborativos y pueden estar
 * incompletos o equivocados. El proveedor decide si los usa.
 */
@Composable
private fun BuscadorApiExterna(
    codigo: String,
    buscando: Boolean,
    resultado: ResultadoBusqueda?,
    onCodigoCambia: (String) -> Unit,
    onBuscar: () -> Unit,
    onUsar: (ProductoExterno) -> Unit,
    onDescartar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            CampoTextoValidado(
                valor = codigo,
                onValorCambia = onCodigoCambia,
                etiqueta = "Codigo de barras",
                placeholder = "7802920777542",
                iconoInicial = Icons.Filled.QrCode2,
                soporte = "Opcional: trae los datos del producto",
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.size(8.dp))

            Button(
                onClick = onBuscar,
                enabled = codigo.isNotBlank() && !buscando,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(56.dp)
            ) {
                if (buscando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar")
                }
            }
        }

        // El when es exhaustivo sobre la sealed class: si manana se
        // agrega un caso, el compilador obliga a dibujarlo.
        when (resultado) {
            null -> Unit

            is ResultadoBusqueda.Encontrado -> TarjetaProductoApi(
                producto = resultado.producto,
                onUsar = { onUsar(resultado.producto) },
                onDescartar = onDescartar
            )

            is ResultadoBusqueda.NoEncontrado -> AvisoApi(
                texto = "Ese codigo no esta en Open Food Facts. " +
                    "Puedes llenar el formulario a mano.",
                esError = false
            )

            is ResultadoBusqueda.SinConexion -> AvisoApi(
                texto = "Sin conexion. Revisa tu internet o llena el " +
                    "formulario a mano.",
                esError = true
            )

            is ResultadoBusqueda.Error -> AvisoApi(
                texto = "No se pudo consultar: ${resultado.motivo}",
                esError = true
            )
        }
    }
}

/** El producto que devolvio la API, con la opcion de usarlo. */
@Composable
private fun TarjetaProductoApi(
    producto: ProductoExterno,
    onUsar: () -> Unit,
    onDescartar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = "Encontrado en Open Food Facts",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (producto.imagenUrl != null) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.size(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.tituloSugerido.ifBlank { "Sin nombre" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    /*
                     * El peso se destaca porque es el dato que de
                     * verdad importa: alimenta el calculo de CO2, que
                     * hasta ahora dependia de lo que el proveedor
                     * escribiera a mano.
                     */
                    Text(
                        text = producto.pesoKg?.let { "Peso: $it kg" }
                            ?: "Sin peso declarado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    if (producto.notaAmbiental != null) {
                        Text(
                            text = "Nota ambiental: ${producto.notaAmbiental}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onUsar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Usar estos datos")
                }
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = onDescartar) {
                    Text("Descartar")
                }
            }
        }
    }
}

/** Aviso corto cuando la consulta no entrego un producto. */
@Composable
private fun AvisoApi(texto: String, esError: Boolean) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = if (esError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}
