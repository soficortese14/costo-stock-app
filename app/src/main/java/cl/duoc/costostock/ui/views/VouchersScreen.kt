package cl.duoc.costostock.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.GeneradorQR
import cl.duoc.costostock.data.PasarelaPago
import cl.duoc.costostock.data.PedidoRepository
import cl.duoc.costostock.data.room.PedidoEntity
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.EstadoVouchers
import cl.duoc.costostock.ui.viewmodel.VouchersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fecha legible del pago. Se declara una sola vez a nivel de archivo
 * porque construir un SimpleDateFormat es caro y acá se usa en cada
 * tarjeta de la lista.
 *
 * SimpleDateFormat y no java.time porque minSdk es 24 y java.time
 * necesitaría activar desugaring en el build.
 */
private val FORMATO_FECHA =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-CL"))

/**
 * CO-13 Vouchers.
 *
 * El historial de compras, cada una con su código de retiro y su QR.
 *
 * Antes de esta pantalla el código solo se veía una vez, en el momento
 * de terminar de pagar: quedaba guardado en la tabla de pedidos pero
 * ninguna pantalla lo leía, así que para consultarlo había que
 * recordarlo y escribirlo a mano.
 *
 * Tocar un voucher abre la consulta de pedido con el código ya puesto,
 * reutilizando CO-12 en vez de repetir acá el detalle completo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    navController: NavHostController,
    viewModel: VouchersViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val ahora by viewModel.ahora.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Mis vouchers") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // El when es exhaustivo sobre la sealed class: si mañana se
            // agrega un estado, el compilador obliga a dibujarlo.
            when (val e = estado) {

                is EstadoVouchers.Cargando -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                is EstadoVouchers.Vacio -> VouchersVacio(
                    onVerOfertas = { navController.navigate(Rutas.OFERTAS) },
                    modifier = Modifier.align(Alignment.Center)
                )

                is EstadoVouchers.ConVouchers -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // key por código: es la clave primaria del pedido, así
                    // Compose reusa la tarjeta correcta al llegar una
                    // compra nueva en vez de redibujar la lista entera.
                    items(e.pedidos, key = { it.codigo }) { pedido ->
                        TarjetaVoucher(
                            pedido = pedido,
                            ahora = ahora,
                            onClick = {
                                navController.navigate(
                                    Rutas.consultaPedido(pedido.codigo)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Nunca se ha comprado nada. */
@Composable
private fun VouchersVacio(
    onVerOfertas: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.ConfirmationNumber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Todavía no tienes vouchers",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cuando compres una oferta, acá va a quedar tu código " +
                "de retiro con su QR para mostrar en el comercio.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onVerOfertas) {
            Text("Ver ofertas")
        }
    }
}

/**
 * Un voucher: el QR, el código y lo esencial de la compra.
 *
 * El detalle completo no va acá a propósito. Esta pantalla es para
 * encontrar el código rápido en el mostrador; el desglose está en CO-12,
 * a un toque de distancia.
 */
@Composable
private fun TarjetaVoucher(
    pedido: PedidoEntity,
    ahora: Long,
    onClick: () -> Unit
) {
    val etapa = PedidoRepository.estadoDe(pedido.fechaPago, ahora)

    /*
     * El QR se genera una vez por tarjeta y queda en caché mientras el
     * código no cambie. Son unos milisegundos y el código es inmutable,
     * así que no hace falta sacarlo a una corrutina.
     */
    val qr = remember(pedido.codigo) { GeneradorQR.generar(pedido.codigo) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (qr != null) {
                /*
                 * El QR va sobre una Surface blanca fija y no sobre el
                 * color del tema: un lector necesita contraste máximo y
                 * el padding blanco hace de zona de silencio, que es
                 * parte del estándar y no decoración.
                 */
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Image(
                        bitmap = qr,
                        contentDescription = "Código QR del voucher ${pedido.codigo}",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(180.dp)
                    )
                }
            } else {
                // Si ZXing falló, el código en texto sigue sirviendo:
                // la pantalla no se cae ni queda vacía.
                Text(
                    text = "No se pudo dibujar el QR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(12.dp))

            /*
             * Monospace y letras separadas: el código se lee en voz alta
             * o se escribe a mano en el mostrador, y así no se confunden
             * caracteres parecidos.
             */
            Text(
                text = pedido.codigo,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = etapa.titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = FORMATO_FECHA.format(Date(pedido.fechaPago)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (pedido.unidades == 1) "1 unidad"
                        else "${pedido.unidades} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = PasarelaPago.formatearPesos(pedido.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = pedido.puntosRetiro.lineSequence().first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(12.dp))

            /*
             * Boton explicito al seguimiento.
             *
             * La tarjeta entera ya es tocable y lleva al mismo lugar, pero
             * eso no se ve: nada en pantalla avisa que el QR es un boton.
             * Con el boton queda claro que desde aca se llega al estado, y
             * el voucher deja de parecer una pantalla sin salida.
             */
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Ver estado del pedido", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
