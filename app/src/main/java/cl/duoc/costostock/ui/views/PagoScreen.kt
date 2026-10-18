package cl.duoc.costostock.ui.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.PasarelaPago
import cl.duoc.costostock.data.ResultadoPago
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.EstadoPago
import cl.duoc.costostock.ui.viewmodel.PagoViewModel

/**
 * CO-11 Pago.
 *
 * La pasarela esta simulada: el codigo de comercio y la llave de una
 * pasarela real viven en el servidor, nunca dentro del APK, que se puede
 * abrir. Queda documentado como integracion simulada igual que el SII.
 *
 * Lo que si es real es la validacion de la tarjeta, con el algoritmo de
 * Luhn, el mismo que usan las pasarelas para descartar numeros mal
 * escritos antes de cobrar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoScreen(
    navController: NavHostController,
    viewModel: PagoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val monto by viewModel.monto.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Pago seguro") },
                navigationIcon = {
                    // Mientras la pasarela procesa no se puede salir: un
                    // cobro a medias dejaria el stock inconsistente.
                    if (estado == EstadoPago.Formulario) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val actual = estado) {
                is EstadoPago.Formulario ->
                    Formulario(viewModel = viewModel, monto = monto)

                is EstadoPago.Procesando -> Procesando(monto)

                is EstadoPago.Aprobado -> Aprobado(
                    resultado = actual.resultado,
                    onVolver = {
                        navController.popBackStack(Rutas.OFERTAS, inclusive = false)
                    }
                )

                is EstadoPago.Rechazado -> Rechazado(
                    motivo = actual.motivo,
                    onReintentar = { viewModel.reintentar() },
                    onCancelar = {
                        navController.popBackStack(Rutas.OFERTAS, inclusive = false)
                    }
                )
            }
        }
    }
}

@Composable
private fun Formulario(viewModel: PagoViewModel, monto: Int) {
    val tarjeta by viewModel.tarjeta.collectAsState()
    val errores by viewModel.errores.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total a pagar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = PasarelaPago.formatearPesos(monto),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Comision de plataforma incluida · Retiro presencial",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        CampoTextoValidado(
            valor = tarjeta.numero,
            onValorCambia = { viewModel.cambiarNumero(it) },
            etiqueta = "Numero de tarjeta",
            error = errores.numero,
            iconoInicial = Icons.Filled.CreditCard,
            tipoTeclado = KeyboardType.Number,
            transformacionVisual = AgrupadorTarjeta()
        )

        CampoTextoValidado(
            valor = tarjeta.nombre,
            onValorCambia = { viewModel.cambiarNombre(it) },
            etiqueta = "Nombre en la tarjeta",
            error = errores.nombre
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                CampoTextoValidado(
                    valor = tarjeta.vencimiento,
                    onValorCambia = { viewModel.cambiarVencimiento(it) },
                    etiqueta = "MM/AA",
                    error = errores.vencimiento,
                    tipoTeclado = KeyboardType.Number,
                    transformacionVisual = FormatoVencimiento()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                CampoTextoValidado(
                    valor = tarjeta.cvv,
                    onValorCambia = { viewModel.cambiarCvv(it) },
                    etiqueta = "CVV",
                    error = errores.cvv,
                    iconoInicial = Icons.Filled.Lock,
                    tipoTeclado = KeyboardType.Number
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Entorno de prueba",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Integracion simulada: no se cobra dinero real. " +
                        "El numero se valida con el algoritmo de Luhn, asi " +
                        "que un numero inventado no pasa. Usa estos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.usarTarjetaDePrueba(aprueba = true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Que aprueba", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = { viewModel.usarTarjetaDePrueba(aprueba = false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Que rechaza", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.pagar() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Pagar ${PasarelaPago.formatearPesos(monto)}", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Espera con el monto a la vista, para que no parezca que se colgo. */
@Composable
private fun Procesando(monto: Int) {
    val transicion = rememberInfiniteTransition(label = "procesando")
    val opacidad by transicion.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacidad"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Procesando el pago",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alpha(opacidad)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No cierres la aplicacion. Estamos confirmando " +
                "${PasarelaPago.formatearPesos(monto)} con el emisor.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Aprobado(resultado: ResultadoPago.Aprobado, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text("Pago aprobado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${PasarelaPago.formatearPesos(resultado.monto)} con la " +
                "tarjeta terminada en ${resultado.ultimosCuatro}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Codigo de retiro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = resultado.codigoOrden,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Muestra este codigo en el comercio para retirar tu compra.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Volver al catalogo", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Rechazado(motivo: String, onReintentar: () -> Unit, onCancelar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text("Pago rechazado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text(
            text = motivo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // El carrito no se toca: el comprador puede reintentar sin
        // volver a armar su compra desde cero.
        Text(
            text = "Tu carrito sigue intacto.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onReintentar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Intentar con otra tarjeta", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Volver al catalogo")
        }
    }
}
