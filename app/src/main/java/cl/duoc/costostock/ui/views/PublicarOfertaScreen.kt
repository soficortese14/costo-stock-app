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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.ui.viewmodel.PublicarOfertaViewModel
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
    viewModel: PublicarOfertaViewModel = viewModel()
) {
    val formulario by viewModel.formulario.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val publicada by viewModel.publicada.collectAsState()

    // Al guardar en Room, vuelve al catalogo: la oferta nueva aparece
    // sola porque el DAO devuelve un Flow
    LaunchedEffect(publicada) {
        if (publicada) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Publicar oferta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Titulo("Producto")

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
                    etiqueta = "Precio original",
                    error = formulario.errorPrecioOriginal,
                    iconoInicial = Icons.Filled.AttachMoney,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoTextoValidado(
                    valor = formulario.precioOferta,
                    onValorCambia = viewModel::cambiarPrecioOferta,
                    etiqueta = "Precio oferta",
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
                    etiqueta = "Unidades",
                    error = formulario.errorStock,
                    iconoInicial = Icons.Filled.Inventory2,
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoTextoValidado(
                    valor = formulario.horasVigencia,
                    onValorCambia = viewModel::cambiarHoras,
                    etiqueta = "Duracion (horas)",
                    error = formulario.errorHoras,
                    iconoInicial = Icons.Filled.Schedule,
                    tipoTeclado = KeyboardType.Number,
                    soporte = "Maximo 72 h",
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
                VistaPreviaCo2(kilos = viewModel.co2Estimado())
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
                    Text("Publicar oferta", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
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

/** El unico bloque verde del formulario: el impacto ambiental. */
@Composable
private fun VistaPreviaCo2(kilos: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            text = "🌱 Esta oferta evitaria " +
                "${String.format(Locale.US, "%.1f", kilos).replace('.', ',')} " +
                "kg de CO2",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun formatearMiles(monto: Int): String =
    monto.toString().reversed().chunked(3).joinToString(".").reversed()
