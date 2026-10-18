package cl.duoc.costostock.data

import android.content.Context
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.room.ItemCarrito
import cl.duoc.costostock.data.room.PedidoEntity

/**
 * En que etapa va el pedido.
 *
 * sealed class para que el when de la pantalla sea exhaustivo, igual que
 * EstadoUbicacion y EstadoPago.
 */
sealed class EstadoPedido(
    val titulo: String,
    val detalle: String,
    /** De 0 a 1, para la barra de progreso. */
    val avance: Float
) {
    object PagoConfirmado : EstadoPedido(
        titulo = "Pago confirmado",
        detalle = "Avisamos al comercio. En unos minutos preparan tu pedido.",
        avance = 0.33f
    )

    object Preparando : EstadoPedido(
        titulo = "Preparando tu pedido",
        detalle = "El comercio esta apartando tus productos.",
        avance = 0.66f
    )

    object ListoParaRetirar : EstadoPedido(
        titulo = "Listo para retirar",
        detalle = "Ve al punto de retiro con tu codigo. Recuerda que no hay despacho.",
        avance = 1f
    )
}

/**
 * Pedidos pagados.
 *
 * El estado se deriva del tiempo transcurrido desde el pago y no se
 * guarda en la tabla. Es una simulacion y esta asumida: en produccion lo
 * informaria el comercio desde el backend cuando aparta los productos.
 * Derivarlo del reloj permite demostrar las tres etapas sin inventar una
 * pantalla de administracion que el caso no pide.
 */
class PedidoRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).pedidoDAO()

    suspend fun guardar(pedido: PedidoEntity) = dao.guardar(pedido)

    suspend fun buscar(codigo: String): PedidoEntity? =
        dao.buscarPorCodigo(codigo.trim())

    companion object {

        private const val MINUTO = 60_000L

        /** Cuanto demora el comercio en empezar a preparar. */
        private const val HASTA_PREPARANDO = 1 * MINUTO

        /** Cuanto demora en dejarlo listo. */
        private const val HASTA_LISTO = 3 * MINUTO

        fun estadoDe(
            fechaPago: Long,
            ahora: Long = System.currentTimeMillis()
        ): EstadoPedido {
            val transcurrido = ahora - fechaPago

            return when {
                transcurrido < HASTA_PREPARANDO -> EstadoPedido.PagoConfirmado
                transcurrido < HASTA_LISTO -> EstadoPedido.Preparando
                else -> EstadoPedido.ListoParaRetirar
            }
        }

        /**
         * Convierte el carrito en el texto que queda guardado en el pedido.
         * Se resuelve aca, al momento de pagar, justamente para que no
         * dependa despues de las ofertas.
         */
        fun detalleDe(items: List<ItemCarrito>, ahora: Long): String =
            items.joinToString("\n") { item ->
                val precio = item.oferta.precioVigente(ahora) * item.cantidad
                "${item.cantidad} x ${item.oferta.titulo} — " +
                    PasarelaPago.formatearPesos(precio)
            }

        /** Los comercios donde hay que retirar, sin repetir. */
        fun puntosRetiroDe(items: List<ItemCarrito>): String =
            items
                .map { "${it.oferta.comercio}, ${it.oferta.direccion}, ${it.oferta.comuna}" }
                .distinct()
                .joinToString("\n")
    }
}
