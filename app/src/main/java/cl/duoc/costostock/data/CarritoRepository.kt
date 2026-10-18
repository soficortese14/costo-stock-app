package cl.duoc.costostock.data

import android.content.Context
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.room.CarritoEntity
import cl.duoc.costostock.data.room.ItemCarrito
import kotlinx.coroutines.flow.Flow

/**
 * Resumen de lo que hay que pagar.
 *
 * Se calcula aparte y no dentro de la pantalla para poder probarlo sin
 * levantar la interfaz, igual que las validaciones del formulario.
 */
data class ResumenCompra(
    val subtotal: Int,
    val comision: Int,
    val total: Int,
    val unidades: Int,
    val co2Evitado: Double
)

/**
 * Unico punto de acceso al carrito.
 *
 * El carrito vive en Room y no en memoria: si la app se cierra con
 * productos dentro, siguen ahi al volver. Es el mismo criterio que el
 * resto del proyecto y ademas se puede demostrar en la defensa.
 */
class CarritoRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).carritoDAO()

    fun observarItems(): Flow<List<ItemCarrito>> = dao.observarItems()

    fun observarUnidades(): Flow<Int> = dao.observarUnidades()

    /**
     * Agrega unidades al carrito, sin pasarse del stock disponible.
     *
     * Devuelve true si algo se agrego. false significa que la linea ya
     * estaba en el tope del stock y la pantalla debe avisarlo.
     */
    suspend fun agregar(ofertaId: Long, cantidad: Int, stockDisponible: Int): Boolean {
        val actual = dao.cantidadDe(ofertaId) ?: 0
        val nueva = (actual + cantidad).coerceAtMost(stockDisponible)

        if (nueva == actual) return false

        dao.guardar(CarritoEntity(ofertaId = ofertaId, cantidad = nueva))
        return true
    }

    /** Fija la cantidad de una linea. En cero, la saca del carrito. */
    suspend fun cambiarCantidad(ofertaId: Long, cantidad: Int, stockDisponible: Int) {
        val ajustada = cantidad.coerceIn(0, stockDisponible)

        if (ajustada == 0) dao.quitar(ofertaId)
        else dao.guardar(CarritoEntity(ofertaId, ajustada))
    }

    suspend fun quitar(ofertaId: Long) = dao.quitar(ofertaId)

    suspend fun vaciar() = dao.vaciar()

    companion object {
        /**
         * Comision de la plataforma, que el cliente pidio sumar al precio
         * final. El porcentaje es referencial para el prototipo academico:
         * en produccion vendria del backend, porque cambia por volumen de
         * ventas segun el modelo de negocio de Costo Stock.
         */
        const val PORCENTAJE_COMISION = 8

        /**
         * Arma el resumen de la compra a los precios que corren ahora.
         *
         * Se recalcula con la hora actual y no con una copia guardada,
         * porque una oferta puede vencer mientras esta en el carrito: en
         * ese caso vuelve a su precio original y el total sube.
         */
        fun resumir(
            items: List<ItemCarrito>,
            ahora: Long = System.currentTimeMillis()
        ): ResumenCompra {
            val subtotal = items.sumOf { it.subtotal(ahora) }
            val comision = subtotal * PORCENTAJE_COMISION / 100

            return ResumenCompra(
                subtotal = subtotal,
                comision = comision,
                total = subtotal + comision,
                unidades = items.sumOf { it.cantidad },
                co2Evitado = items.sumOf { it.co2() }
            )
        }
    }
}
