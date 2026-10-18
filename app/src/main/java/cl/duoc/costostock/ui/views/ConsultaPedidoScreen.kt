package cl.duoc.costostock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.EstadoPedido
import cl.duoc.costostock.data.PasarelaPago
import cl.duoc.costostock.data.PedidoRepository
import cl.duoc.costostock.data.room.PedidoEntity
import cl.duoc.costostock.ui.viewmodel.ConsultaPedidoViewModel
import cl.duoc.costostock.ui.viewmodel.EstadoConsulta

/**
 * CO-12 Consulta de pedido.
 *
 * Se entra con el codigo de retiro, sin necesidad de sesion: el codigo es
 * la credencial. Es coherente con la regla del cliente de que el catalogo
 * se navegue sin cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaPedidoScreen(
    navController: NavHostController,
    codigoInicial: String? = null,
    viewModel: ConsultaPedidoViewModel = viewModel()
) {
    val codigo by viewModel.codigo.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val ahora by viewModel.ahora.collectAsState()

    // Si se llega desde el pago, el codigo viene listo y se busca solo
    LaunchedEffect(codigoInicial) {
        if (!codigoInicial.isNullOrBlank()) viewModel.buscarDirecto(codigoInicial)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Consulta tu pedido") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
            CampoTextoValidado(
                valor = codigo,
                onValorCambia = { viewModel.cambiarCodigo(it) },
                etiqueta = "Codigo de retiro",
                placeholder = "CS-1234-ABCD",
                iconoInicial = Icons.Filled.ConfirmationNumber,
                soporte = "Lo recibiste al pagar"
            )

            Button(
                onClick = { viewModel.buscar() },
                enabled = codigo.isNotBlank() && estado !is EstadoConsulta.Buscando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Consultar", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))

            when (val actual = estado) {
                is EstadoConsulta.Inicial -> Unit

                is EstadoConsulta.Buscando -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                is EstadoConsulta.NoEncontrado -> NoEncontrado()

                is EstadoConsulta.Encontrado -> DetallePedido(
                    pedido = actual.pedido,
                    ahora = ahora
                )
            }
        }
    }
}

@Composable
private fun NoEncontrado() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No encontramos ese pedido",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Revisa que el codigo este completo, con el formato " +
                "CS-1234-ABCD.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DetallePedido(pedido: PedidoEntity, ahora: Long) {
    // El estado se recalcula con el reloj, asi que avanza en pantalla
    val estadoPedido = PedidoRepository.estadoDe(pedido.fechaPago, ahora)

    AnimatedVisibility(visible = true, enter = fadeIn(tween(400))) {
        Column {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pedido.codigo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = estadoPedido.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = estadoPedido.detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { estadoPedido.avance },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EtapaTexto("Pagado", estadoPedido.avance >= 0.33f)
                        EtapaTexto("Preparando", estadoPedido.avance >= 0.66f)
                        EtapaTexto("Listo", estadoPedido.avance >= 1f)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Tu pedido", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = pedido.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total pagado", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = PasarelaPago.formatearPesos(pedido.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${pedido.unidades} unidades · tarjeta terminada en " +
                    pedido.ultimosCuatro,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Punto de retiro",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pedido.puntosRetiro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Retiro presencial, sin despacho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "🌱 Con esta compra evitaste " +
                    "${"%.1f".format(pedido.co2Evitado).replace('.', ',')} kg de CO2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Una de las tres etapas bajo la barra. La alcanzada se marca. */
@Composable
private fun EtapaTexto(texto: String, alcanzada: Boolean) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (alcanzada) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
