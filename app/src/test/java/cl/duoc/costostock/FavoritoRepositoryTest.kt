package cl.duoc.costostock

import cl.duoc.costostock.data.FavoritoRepository
import cl.duoc.costostock.data.room.FavoritoDAO
import cl.duoc.costostock.data.room.FavoritoEntity
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de FavoritoRepository.
 *
 * QUE SE PRUEBA: la logica de alternar un favorito. Es la unica decision
 * real de la clase -- si marcar o desmarcar segun el estado actual, y que
 * se guarde el instante en que se marco.
 *
 * QUE SE MOCKEA: el FavoritoDAO, que es la puerta a Room. Se reemplaza
 * por un doble de MockK porque Room necesita un dispositivo Android y
 * acá se quiere probar la decision, no la base de datos. Probar que Room
 * guarda filas seria probar Room, no este codigo.
 *
 * COMO: el repositorio recibe el DAO por constructor, asi que el test le
 * entrega el doble en vez de la base real. Despues se verifica con
 * coVerify que se llamo al metodo correcto, y con un slot se captura la
 * fila que se intento guardar para revisarla.
 *
 * runTest viene de kotlinx-coroutines-test: ejecuta las funciones suspend
 * de forma inmediata y determinista, sin esperas reales.
 */
class FavoritoRepositoryTest {

    private lateinit var dao: FavoritoDAO
    private lateinit var repositorio: FavoritoRepository

    private companion object {
        const val ID_OFERTA = 7L
    }

    @Before
    fun prepararDoble() {
        // relaxed = true: los metodos que no interesan devuelven un valor
        // por defecto en vez de reventar. Asi el test declara solo lo que
        // de verdad esta probando.
        dao = mockk(relaxed = true)
        repositorio = FavoritoRepository(dao)
    }

    // ----------------------------------------------------------------
    // Alternar: la decision que toma la clase
    // ----------------------------------------------------------------

    @Test
    fun `si no esta en favoritos, lo marca`() = runTest {
        val quedoMarcado = repositorio.alternar(ID_OFERTA, estaEnFavoritos = false)

        coVerify(exactly = 1) { dao.marcar(any()) }
        coVerify(exactly = 0) { dao.desmarcar(any()) }
        assertTrue("Deberia quedar marcado", quedoMarcado)
    }

    @Test
    fun `si ya esta en favoritos, lo quita`() = runTest {
        val quedoMarcado = repositorio.alternar(ID_OFERTA, estaEnFavoritos = true)

        coVerify(exactly = 1) { dao.desmarcar(ID_OFERTA) }
        coVerify(exactly = 0) { dao.marcar(any()) }
        assertFalse("Deberia quedar sin marcar", quedoMarcado)
    }

    @Test
    fun `marca la oferta pedida y no otra`() = runTest {
        // slot captura el objeto que se le paso al doble, para revisarlo
        val capturado = slot<FavoritoEntity>()
        coEvery { dao.marcar(capture(capturado)) } returns Unit

        repositorio.alternar(ID_OFERTA, estaEnFavoritos = false)

        assertEquals(ID_OFERTA, capturado.captured.ofertaId)
    }

    @Test
    fun `guarda el instante en que se marco`() = runTest {
        val antes = System.currentTimeMillis()
        val capturado = slot<FavoritoEntity>()
        coEvery { dao.marcar(capture(capturado)) } returns Unit

        repositorio.alternar(ID_OFERTA, estaEnFavoritos = false)

        val despues = System.currentTimeMillis()
        val marcadoEn = capturado.captured.marcadoEn

        // El instante tiene que caer entre el antes y el despues de la
        // llamada. Es lo que permite ordenar los favoritos por fecha.
        assertTrue(
            "marcadoEn=$marcadoEn deberia estar entre $antes y $despues",
            marcadoEn in antes..despues
        )
    }

    @Test
    fun `alternar dos veces deja la oferta como estaba`() = runTest {
        val primera = repositorio.alternar(ID_OFERTA, estaEnFavoritos = false)
        val segunda = repositorio.alternar(ID_OFERTA, estaEnFavoritos = primera)

        assertTrue(primera)
        assertFalse(segunda)
        coVerify(exactly = 1) { dao.marcar(any()) }
        coVerify(exactly = 1) { dao.desmarcar(ID_OFERTA) }
    }

    // ----------------------------------------------------------------
    // Las consultas: el repositorio delega, no reimplementa
    // ----------------------------------------------------------------

    @Test
    fun `esFavorito entrega lo que responde la base`() = runTest {
        every { dao.esFavorito(ID_OFERTA) } returns flowOf(true)

        val resultado = mutableListOf<Boolean>()
        repositorio.esFavorito(ID_OFERTA).collect { resultado.add(it) }

        assertEquals(listOf(true), resultado)
    }

    @Test
    fun `observarIds entrega los ids guardados`() = runTest {
        every { dao.observarIds() } returns flowOf(listOf(1L, 7L, 9L))

        val resultado = mutableListOf<List<Long>>()
        repositorio.observarIds().collect { resultado.add(it) }

        assertEquals(listOf(listOf(1L, 7L, 9L)), resultado)
    }

    @Test
    fun `sin favoritos devuelve una lista vacia y no null`() = runTest {
        every { dao.observarIds() } returns flowOf(emptyList())

        val resultado = mutableListOf<List<Long>>()
        repositorio.observarIds().collect { resultado.add(it) }

        assertTrue(resultado.first().isEmpty())
    }
}
