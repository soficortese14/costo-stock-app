package cl.duoc.costostock

import cl.duoc.costostock.data.ProductoExternoRepository
import cl.duoc.costostock.data.ResultadoBusqueda
import cl.duoc.costostock.data.remoto.OpenFoodFactsApi
import cl.duoc.costostock.data.remoto.ProductoJson
import cl.duoc.costostock.data.remoto.RespuestaProducto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Pruebas del consumo de la API externa (Open Food Facts).
 *
 * QUE SE PRUEBA: la traduccion de lo que devuelve la API a lo que
 * entiende la app, y el manejo de todo lo que puede salir mal. Open
 * Food Facts es una base colaborativa: hay productos sin peso, sin
 * foto, con peso cero, y codigos que no existen.
 *
 * QUE SE MOCKEA: la interfaz OpenFoodFactsApi, que es la que Retrofit
 * implementa. Se reemplaza por un doble de MockK para no depender de
 * internet: una prueba que saliera a la red fallaria sin conexion, y
 * dependeria de que un producto siga existiendo en una base que
 * editan voluntarios.
 *
 * COMO: el repositorio recibe la API por constructor, igual que
 * FavoritoRepository recibe su DAO. coEvery programa que devuelve el
 * doble en cada caso, incluida la excepcion de red.
 */
class ProductoExternoRepositoryTest {

    private lateinit var api: OpenFoodFactsApi
    private lateinit var repositorio: ProductoExternoRepository

    private companion object {
        const val CODIGO_LECHE = "7802920777542"

        /** Respuesta tal como la devuelve la API para la leche Colun. */
        val LECHE = ProductoJson(
            codigo = CODIGO_LECHE,
            nombre = "Leche Entera Natural Caja",
            marcas = "Colun",
            cantidadTexto = "1 L",
            pesoGramos = 1000.0,
            imagenUrl = "https://images.openfoodfacts.org/leche.jpg",
            notaAmbiental = "d",
            categorias = "Whole milks"
        )
    }

    @Before
    fun prepararDoble() {
        api = mockk()
        repositorio = ProductoExternoRepository(api)
    }

    // ----------------------------------------------------------------
    // El camino feliz
    // ----------------------------------------------------------------

    @Test
    fun `convierte el producto de la API al modelo de la app`() = runTest {
        coEvery { api.buscarPorCodigo(CODIGO_LECHE, any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE)

        val resultado = repositorio.buscarPorCodigo(CODIGO_LECHE)

        assertTrue(resultado is ResultadoBusqueda.Encontrado)
        val producto = (resultado as ResultadoBusqueda.Encontrado).producto
        assertEquals("Leche Entera Natural Caja", producto.nombre)
        assertEquals("Colun", producto.marca)
    }

    @Test
    fun `convierte los gramos de la API a kilos`() = runTest {
        // Es la traduccion que importa: la API entrega gramos y
        // CalculadoraCO2 trabaja en kilos
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE)

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertEquals(1.0, producto.pesoKg!!, 0.001)
    }

    @Test
    fun `redondea el peso a dos decimales`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE.copy(pesoGramos = 333.0))

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertEquals(0.33, producto.pesoKg!!, 0.001)
    }

    @Test
    fun `arma el titulo juntando marca y producto`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE)

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertEquals("Colun Leche Entera Natural Caja", producto.tituloSugerido)
    }

    @Test
    fun `pasa la nota ambiental a mayuscula`() = runTest {
        // La API la entrega en minuscula y se muestra como letra grande
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE)

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertEquals("D", producto.notaAmbiental)
    }

    @Test
    fun `se queda con la primera marca cuando vienen varias`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE.copy(marcas = "Colun, Soprole"))

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertEquals("Colun", producto.marca)
    }

    // ----------------------------------------------------------------
    // Datos incompletos: la base es colaborativa
    // ----------------------------------------------------------------

    @Test
    fun `un producto sin peso no rompe la conversion`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE.copy(pesoGramos = null))

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertNull("Sin peso debe quedar null, no cero", producto.pesoKg)
    }

    @Test
    fun `un peso cero se descarta`() = runTest {
        // Hay productos cargados con 0 g. Un peso cero daria 0 kg de
        // CO2 evitado, que es peor que no mostrar nada.
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE.copy(pesoGramos = 0.0))

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertNull(producto.pesoKg)
    }

    @Test
    fun `una imagen vacia queda en null`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE.copy(imagenUrl = ""))

        val producto = (repositorio.buscarPorCodigo(CODIGO_LECHE)
            as ResultadoBusqueda.Encontrado).producto

        assertNull(producto.imagenUrl)
    }

    @Test
    fun `un producto sin nombre no rompe nada`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 1, producto = ProductoJson(codigo = CODIGO_LECHE))

        val resultado = repositorio.buscarPorCodigo(CODIGO_LECHE)

        assertTrue(resultado is ResultadoBusqueda.Encontrado)
        assertEquals("", (resultado as ResultadoBusqueda.Encontrado).producto.nombre)
    }

    // ----------------------------------------------------------------
    // Lo que puede salir mal
    // ----------------------------------------------------------------

    @Test
    fun `status cero significa no encontrado`() = runTest {
        // La API responde HTTP 200 aunque el producto no exista: el
        // aviso viene en el campo status, no en el codigo HTTP
        coEvery { api.buscarPorCodigo(any(), any()) } returns
            RespuestaProducto(estado = 0, producto = null)

        assertEquals(
            ResultadoBusqueda.NoEncontrado,
            repositorio.buscarPorCodigo(CODIGO_LECHE)
        )
    }

    @Test
    fun `sin internet devuelve SinConexion y no revienta`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } throws IOException("sin red")

        assertEquals(
            ResultadoBusqueda.SinConexion,
            repositorio.buscarPorCodigo(CODIGO_LECHE)
        )
    }

    @Test
    fun `un fallo inesperado se traduce a Error`() = runTest {
        coEvery { api.buscarPorCodigo(any(), any()) } throws
            RuntimeException("JSON invalido")

        val resultado = repositorio.buscarPorCodigo(CODIGO_LECHE)

        assertTrue(resultado is ResultadoBusqueda.Error)
        assertEquals("JSON invalido", (resultado as ResultadoBusqueda.Error).motivo)
    }

    // ----------------------------------------------------------------
    // Validacion antes de salir a la red
    // ----------------------------------------------------------------

    @Test
    fun `un codigo muy corto no llega a consultar la API`() = runTest {
        val resultado = repositorio.buscarPorCodigo("123")

        assertEquals(ResultadoBusqueda.NoEncontrado, resultado)
        // Lo importante: no se gasto una llamada de red que ya sabiamos
        // que iba a fallar
        coVerify(exactly = 0) { api.buscarPorCodigo(any(), any()) }
    }

    @Test
    fun `un codigo con letras se limpia antes de consultar`() = runTest {
        coEvery { api.buscarPorCodigo(CODIGO_LECHE, any()) } returns
            RespuestaProducto(estado = 1, producto = LECHE)

        repositorio.buscarPorCodigo("780-292 077 7542 ")

        // Se consulto con los digitos solos, sin guiones ni espacios
        coVerify(exactly = 1) { api.buscarPorCodigo(CODIGO_LECHE, any()) }
    }

    @Test
    fun `un codigo vacio no consulta la API`() = runTest {
        repositorio.buscarPorCodigo("")

        coVerify(exactly = 0) { api.buscarPorCodigo(any(), any()) }
    }
}
