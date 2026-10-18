package cl.duoc.costostock.data

import android.content.Context
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Unico punto de acceso a las ofertas.
 *
 * El ViewModel habla con el repositorio y nunca con Room directamente.
 * Asi, si manana las ofertas llegan desde la API en Spring Boot, se
 * cambia solo esta clase y las pantallas siguen igual.
 */
class OfertaRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).ofertaDAO()

    /** Todas las ofertas. El Flow avisa solo cuando la tabla cambia. */
    fun observarOfertas(): Flow<List<OfertaEntity>> = dao.observarTodas()

    fun observarPorCategoria(categoria: String): Flow<List<OfertaEntity>> =
        if (categoria == CATEGORIA_TODAS) dao.observarTodas()
        else dao.observarPorCategoria(categoria)

    suspend fun obtenerPorId(id: Long): OfertaEntity? = dao.obtenerPorId(id)

    /** Comunas con ofertas publicadas, para armar el filtro. */
    fun observarComunas(): Flow<List<String>> = dao.observarComunas()

    /**
     * Carga el catalogo de prueba la primera vez que se abre la app.
     * Si la tabla ya tiene datos no hace nada, para no duplicarlos.
     */
    suspend fun cargarDatosIniciales() {
        if (dao.contar() == 0) {
            dao.insertar(OfertasDePrueba.catalogo())
        }
    }

    /**
     * true si ya existe una oferta vigente con el mismo producto en el
     * mismo comercio.
     */
    suspend fun existeDuplicada(
        titulo: String,
        comercio: String,
        excluirId: Long = SIN_EXCLUIR
    ): Boolean =
        dao.buscarDuplicada(
            titulo.trim(),
            comercio.trim(),
            System.currentTimeMillis(),
            excluirId
        ) != null

    /** Guarda una oferta nueva publicada desde el formulario. */
    suspend fun publicar(oferta: OfertaEntity) {
        dao.insertar(listOf(oferta))
    }

    /** Guarda los cambios de una oferta que ya existe. */
    suspend fun actualizar(oferta: OfertaEntity) {
        dao.actualizar(oferta)
    }

    /** Retira una oferta del catalogo. */
    suspend fun eliminar(id: Long) {
        dao.eliminar(id)
    }

    /** Descuenta stock al comprar. */
    suspend fun comprar(id: Long, cantidad: Int) {
        dao.descontarStock(id, cantidad)
    }

    /** Vuelve a dejar el catalogo como al inicio. Util para demostrar. */
    suspend fun reiniciarCatalogo() {
        dao.borrarTodas()
        dao.insertar(OfertasDePrueba.catalogo())
    }

    companion object {
        /** Ningun id real vale 0, asi que no excluye nada. */
        const val SIN_EXCLUIR = 0L

        const val CATEGORIA_TODAS = "Todas"
        const val COMUNA_TODAS = "Todas"

        val CATEGORIAS = listOf(
            CATEGORIA_TODAS,
            "Abarrotes",
            "Refrigerados",
            "Bebidas",
            "Licores",
            "Hogar"
        )
    }
}
