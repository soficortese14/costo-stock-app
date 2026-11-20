package cl.duoc.costostock.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.IconButton
import coil.compose.AsyncImage
import cl.duoc.costostock.data.room.OfertaEntity
import cl.duoc.costostock.ui.theme.TerracotaTexto
import java.util.Locale

/**
 * Tarjeta de una oferta. Se usa en el listado de ofertas y, mas adelante,
 * en el mapa y en el panel del proveedor.
 *
 * Recibe "ahora" desde fuera en vez de leer el reloj por su cuenta: asi
 * todas las tarjetas de la lista marcan el mismo segundo y solo se
 * redibuja el texto del contador cuando ese valor cambia.
 */
@Composable
fun OfertaCard(
    oferta: OfertaEntity,
    ahora: Long,
    /** Distancia hasta el punto de retiro, o null si no hay ubicacion. */
    distanciaKm: Double? = null,
    /** Si esta guardada en favoritos. Decide si el corazon va relleno. */
    esFavorito: Boolean = false,
    /** null oculta el corazon: el proveedor no guarda sus propias ofertas. */
    onAlternarFavorito: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val vigente = oferta.estaVigente(ahora)
    val precio = oferta.precioVigente(ahora)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ImagenConTag(
                oferta = oferta,
                vigente = vigente,
                esFavorito = esFavorito,
                onAlternarFavorito = onAlternarFavorito
            )

            Column(modifier = Modifier.padding(12.dp)) {

                Text(
                    text = oferta.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        // La distancia solo aparece si hay ubicacion. Sin
                        // ella no hay desde donde medir, y una tarjeta que
                        // dijera "a 0 km" seria peor que no decir nada.
                        text = "${oferta.comercio} · ${oferta.comuna}" +
                            (distanciaKm?.let { " · ${formatearDistancia(it)}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                PrecioDual(
                    precioVigente = precio,
                    precioOriginal = oferta.precioOriginal,
                    vigente = vigente
                )

                Spacer(Modifier.height(8.dp))

                ContadorRegresivo(
                    finVigencia = oferta.finVigencia,
                    ahora = ahora
                )

                Spacer(Modifier.height(8.dp))

                BarraStock(stock = oferta.stockActual)

                Spacer(Modifier.height(8.dp))

                BadgeCO2(kilos = oferta.co2Evitado())
            }
        }
    }
}

/** Imagen del producto, con el tag de descuento y el corazon encima. */
@Composable
private fun ImagenConTag(
    oferta: OfertaEntity,
    vigente: Boolean,
    esFavorito: Boolean,
    onAlternarFavorito: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
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

        // El tag solo tiene sentido mientras la oferta esta vigente:
        // al expirar, el precio vuelve al original y ya no hay descuento.
        if (vigente && oferta.descuento > 0) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "-${oferta.descuento}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }

        /*
         * El corazon va arriba a la derecha, en la esquina opuesta al
         * tag de descuento, para que nunca se tapen.
         *
         * Lleva un circulo semitransparente detras porque se dibuja
         * sobre la foto del producto: sin ese fondo, un corazon blanco
         * sobre una imagen clara desaparece.
         */
        if (onAlternarFavorito != null) {
            IconButton(
                onClick = onAlternarFavorito,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Filled.Favorite
                    else Icons.Filled.FavoriteBorder,
                    // El texto cambia con el estado: un lector de
                    // pantalla tiene que poder anunciar que hace el
                    // boton ahora, no como se llama.
                    contentDescription = if (esFavorito) "Quitar de favoritos"
                    else "Guardar en favoritos",
                    tint = if (esFavorito) MaterialTheme.colorScheme.error
                    else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Precio de oferta junto al original tachado.
 *
 * Si la oferta expiro se muestra un solo precio, el original, porque
 * el cliente pidio que el producto vuelva a su valor normal.
 */
@Composable
private fun PrecioDual(precioVigente: Int, precioOriginal: Int, vigente: Boolean) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatearPesos(precioVigente),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (vigente && precioOriginal > precioVigente) {
            Spacer(Modifier.size(8.dp))
            Text(
                text = formatearPesos(precioOriginal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

/**
 * Tiempo restante en formato HHh MMm SSs.
 *
 * El color pasa de naranja a rojo cuando quedan menos de 15 minutos,
 * y la transicion es animada para que el cambio se note sin sobresaltar.
 */
@Composable
private fun ContadorRegresivo(finVigencia: Long, ahora: Long) {
    val restante = finVigencia - ahora
    val urgente = restante in 1 until 15 * 60_000L

    val color by animateColorAsState(
        targetValue = when {
            restante <= 0 -> MaterialTheme.colorScheme.error
            urgente -> MaterialTheme.colorScheme.error
            else -> TerracotaTexto
        },
        animationSpec = tween(600),
        label = "colorContador"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = if (restante <= 0) "Oferta expirada, precio normal"
            else "Termina en ${formatearTiempo(restante)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/** Barra de stock. Pasa a rojo cuando quedan 3 unidades o menos. */
@Composable
private fun BarraStock(stock: Int) {
    val bajo = stock in 1..3
    val color =
        if (bajo) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    Column {
        LinearProgressIndicator(
            progress = { (stock.coerceAtMost(20) / 20f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = when {
                stock <= 0 -> "Sin stock"
                stock == 1 -> "Queda solo 1 unidad"
                bajo -> "Quedan solo $stock unidades"
                else -> "Quedan $stock unidades"
            },
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/** Chip verde con los kilos de CO2 evitados. */
@Composable
private fun BadgeCO2(kilos: Double) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "🌱 ${formatearKilos(kilos)} kg de CO₂ evitado",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

/** 2390 -> "$2.390", que es como se escriben los precios en Chile. */
private fun formatearPesos(monto: Int): String {
    val texto = monto.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$$texto"
}

/** 1.25 -> "1,2" */
private fun formatearKilos(kilos: Double): String =
    String.format(Locale.US, "%.1f", kilos).replace('.', ',')

/** Milisegundos -> "02h 15m 40s" */
/**
 * Distancia legible: bajo 1 km se muestra en metros, porque "0,4 km"
 * se entiende peor que "400 m" cuando el punto de retiro esta al lado.
 */
private fun formatearDistancia(km: Double): String =
    if (km < 1) "${(km * 1000).toInt()} m"
    else String.format(Locale.US, "%.1f km", km).replace('.', ',')

private fun formatearTiempo(milisegundos: Long): String {
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
