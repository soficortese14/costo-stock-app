package cl.duoc.costostock

import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.EstadoPedido
import cl.duoc.costostock.data.PedidoRepository
import cl.duoc.costostock.data.room.ItemCarrito
import cl.duoc.costostock.data.room.OfertaEntity
import cl.duoc.costostock.domain.CalculadoraCO2
import cl.duoc.costostock.domain.Comuna
import cl.duoc.costostock.domain.Configuracion
import cl.duoc.costostock.domain.ConfiguracionApp
import cl.duoc.costostock.domain.FactorCO2
import cl.duoc.costostock.domain.FactoresCO2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del resumen del carrito y del estado del pedido.
 *
 * QUE SE PRUEBA: el dinero y el tiempo. Dos calculos que el usuario ve
 * directamente y donde un error se nota: lo que se cobra, y en que etapa
 * va el pedido.
 *
 * QUE SE MOCKEA: nada con MockK. Las dos funciones son del companion
 * object y reciben todo lo que necesitan por parametro, asi que basta
 * con armarles los datos de entrada.
 *
 * Lo que si se prepara son los dos contenedores que la app carga al
 * arrancar -- Configuracion y FactoresCO2 -- porque sin app nadie los
 * llena. Se les pone una configuracion de prueba con numeros redondos,
 * no la del archivo: si manana el cliente cambia la comision, este test
 * no deberia romperse.
 */
class CarritoYPedidoTest {

    private companion object {
        const val COMISION_DE_PRUEBA = 10
        const val MINUTO = 60_000L
    }

    @Before
    fun prepararConfiguracion() {
        Configuracion.usar(
            ConfiguracionApp(
                comisionPorcentaje = COMISION_DE_PRUEBA,
                radioMinimoKm = 1,
                radioMaximoKm = 30,
                radioPorDefectoKm = 15,
                comunas = listOf(Comuna("Vina del Mar", -33.0245, -71.5518))
            )
        )
        FactoresCO2.usar(
            mapOf(
                "Prueba" to FactorCO2(
                    produccion = 1.0,
                    disposicion = 1.0, // total 2,0: facil de seguir a mano
                    fuente = "tabla de prueba"
                )
            )
        )
    }

    /** Oferta de prueba con numeros redondos. */
    private fun oferta(
        id: Long = 1L,
        precioOferta: Int = 1_000,
        precioOriginal: Int = 2_000,
        pesoKg: Double = 1.0,
        finVigencia: Long = Long.MAX_VALUE,
        comercio: String = "Almacen Uno",
        direccion: String = "Calle Unica 1",
        comuna: String = "Vina del Mar"
    ) = OfertaEntity(
        id = id,
        titulo = "Producto $id",
        descripcion = "",
        categoria = "Prueba",
        precioOriginal = precioOriginal,
        precioOferta = precioOferta,
        stockActual = 10,
        finVigencia = finVigencia,
        comercio = comercio,
        direccion = direccion,
        comuna = comuna,
        latitud = 0.0,
        longitud = 0.0,
        pesoKg = pesoKg,
        imagenUrl = ""
    )

    // ----------------------------------------------------------------
    // El dinero
    // ----------------------------------------------------------------

    @Test
    fun `suma el subtotal de todas las lineas`() {
        val items = listOf(
            ItemCarrito(oferta(id = 1, precioOferta = 1_000), cantidad = 2),
            ItemCarrito(oferta(id = 2, precioOferta = 500), cantidad = 1)
        )

        val resumen = CarritoRepository.resumir(items, ahora = 0L)

        assertEquals(2_500, resumen.subtotal)
    }

    @Test
    fun `aplica la comision sobre el subtotal`() {
        val items = listOf(ItemCarrito(oferta(precioOferta = 1_000), cantidad = 1))

        val resumen = CarritoRepository.resumir(items, ahora = 0L)

        assertEquals(100, resumen.comision)   // 10% de 1.000
        assertEquals(1_100, resumen.total)
    }

    @Test
    fun `la comision sale de la configuracion y no de una constante`() {
        Configuracion.usar(
            Configuracion.vigente().copy(comisionPorcentaje = 50)
        )
        val items = listOf(ItemCarrito(oferta(precioOferta = 1_000), cantidad = 1))

        val resumen = CarritoRepository.resumir(items, ahora = 0L)

        assertEquals(500, resumen.comision)
        assertEquals(1_500, resumen.total)
    }

    @Test
    fun `un carrito vacio no cobra nada`() {
        val resumen = CarritoRepository.resumir(emptyList(), ahora = 0L)

        assertEquals(0, resumen.subtotal)
        assertEquals(0, resumen.comision)
        assertEquals(0, resumen.total)
        assertEquals(0, resumen.unidades)
    }

    @Test
    fun `una oferta vencida vuelve a su precio original`() {
        // Regla del cliente: al expirar el contador el producto sigue en
        // el carrito, pero al precio normal. El total tiene que subir.
        val vencida = oferta(precioOferta = 1_000, precioOriginal = 2_000, finVigencia = 100L)
        val items = listOf(ItemCarrito(vencida, cantidad = 1))

        val vigente = CarritoRepository.resumir(items, ahora = 50L)
        val expirada = CarritoRepository.resumir(items, ahora = 200L)

        assertEquals(1_000, vigente.subtotal)
        assertEquals(2_000, expirada.subtotal)
        assertTrue("El total deberia subir al expirar", expirada.total > vigente.total)
    }

    @Test
    fun `cuenta las unidades, no las lineas`() {
        val items = listOf(
            ItemCarrito(oferta(id = 1), cantidad = 3),
            ItemCarrito(oferta(id = 2), cantidad = 2)
        )

        assertEquals(5, CarritoRepository.resumir(items, ahora = 0L).unidades)
    }

    @Test
    fun `el CO2 del carrito multiplica por la cantidad`() {
        // 1 kg x factor 2,0 x 3 unidades = 6,0
        val items = listOf(ItemCarrito(oferta(pesoKg = 1.0), cantidad = 3))

        val resumen = CarritoRepository.resumir(items, ahora = 0L)

        assertEquals(6.0, resumen.co2Evitado, 0.001)
    }

    // ----------------------------------------------------------------
    // El tiempo: la etapa del pedido se deriva del reloj
    // ----------------------------------------------------------------

    @Test
    fun `recien pagado esta en pago confirmado`() {
        val estado = PedidoRepository.estadoDe(fechaPago = 0L, ahora = 30_000L)

        assertEquals(EstadoPedido.PagoConfirmado, estado)
    }

    @Test
    fun `al minuto pasa a preparando`() {
        val estado = PedidoRepository.estadoDe(fechaPago = 0L, ahora = 2 * MINUTO)

        assertEquals(EstadoPedido.Preparando, estado)
    }

    @Test
    fun `a los tres minutos queda listo para retirar`() {
        val estado = PedidoRepository.estadoDe(fechaPago = 0L, ahora = 5 * MINUTO)

        assertEquals(EstadoPedido.ListoParaRetirar, estado)
    }

    @Test
    fun `el avance nunca retrocede con el tiempo`() {
        val momentos = listOf(0L, MINUTO, 2 * MINUTO, 3 * MINUTO, 10 * MINUTO)

        val avances = momentos.map {
            PedidoRepository.estadoDe(fechaPago = 0L, ahora = it).avance
        }

        assertEquals(avances.sorted(), avances)
    }

    // ----------------------------------------------------------------
    // El pedido congela los datos al pagar
    // ----------------------------------------------------------------

    @Test
    fun `el detalle guarda el precio del momento, no una referencia`() {
        val items = listOf(ItemCarrito(oferta(precioOferta = 1_000), cantidad = 2))

        val detalle = PedidoRepository.detalleDe(items, ahora = 0L)

        assertTrue("Deberia decir la cantidad", detalle.contains("2 x"))
        assertTrue("Deberia traer el precio ya resuelto", detalle.contains("$2.000"))
    }

    @Test
    fun `los puntos de retiro no se repiten`() {
        // Dos productos del mismo local tienen que dar UN punto de
        // retiro, no dos: el comprador va una sola vez a esa direccion.
        val items = listOf(
            ItemCarrito(
                oferta(id = 1, comercio = "Almacen Uno", direccion = "Freire 455"),
                cantidad = 1
            ),
            ItemCarrito(
                oferta(id = 2, comercio = "Almacen Uno", direccion = "Freire 455"),
                cantidad = 1
            ),
            ItemCarrito(
                oferta(id = 3, comercio = "Almacen Dos", direccion = "Limache 20"),
                cantidad = 1
            )
        )

        val puntos = PedidoRepository.puntosRetiroDe(items)

        assertEquals(2, puntos.lines().size)
    }

    @Test
    fun `dos locales distintos en la misma direccion son dos puntos`() {
        // El punto de retiro es comercio MAS direccion, no solo la
        // direccion: en una galeria comercial dos locales comparten calle.
        val items = listOf(
            ItemCarrito(
                oferta(id = 1, comercio = "Almacen Uno", direccion = "Freire 455"),
                cantidad = 1
            ),
            ItemCarrito(
                oferta(id = 2, comercio = "Almacen Dos", direccion = "Freire 455"),
                cantidad = 1
            )
        )

        assertEquals(2, PedidoRepository.puntosRetiroDe(items).lines().size)
    }
}
