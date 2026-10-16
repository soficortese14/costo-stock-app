package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Operaciones sobre la tabla de ofertas.
 *
 * Las consultas devuelven Flow: Room avisa sola cuando la tabla cambia
 * y la pantalla se redibuja sin que nadie tenga que recargarla a mano.
 */
@Dao
interface OfertaDAO {

    /** Todas las ofertas, las que vencen antes primero. */
    @Query("SELECT * FROM ofertas ORDER BY finVigencia ASC")
    fun observarTodas(): Flow<List<OfertaEntity>>

    /** Filtra por categoria. 'Todas' se maneja en el repositorio. */
    @Query("SELECT * FROM ofertas WHERE categoria = :categoria ORDER BY finVigencia ASC")
    fun observarPorCategoria(categoria: String): Flow<List<OfertaEntity>>

    @Query("SELECT * FROM ofertas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): OfertaEntity?

    /**
     * Busca una oferta vigente con el mismo producto en el mismo comercio.
     * Sirve para no publicar dos veces lo mismo por error.
     *
     * COLLATE NOCASE ignora mayusculas: "Pan integral" y "pan integral"
     * se consideran el mismo producto.
     */
    @Query(
        """
        SELECT * FROM ofertas
        WHERE titulo = :titulo COLLATE NOCASE
          AND comercio = :comercio COLLATE NOCASE
          AND finVigencia > :ahora
        LIMIT 1
        """
    )
    suspend fun buscarDuplicada(
        titulo: String,
        comercio: String,
        ahora: Long
    ): OfertaEntity?

    /** Sirve para saber si hay que cargar los datos de prueba. */
    @Query("SELECT COUNT(*) FROM ofertas")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(ofertas: List<OfertaEntity>)

    @Update
    suspend fun actualizar(oferta: OfertaEntity)

    /** Descuenta stock al comprar. */
    @Query("UPDATE ofertas SET stockActual = stockActual - :cantidad WHERE id = :id")
    suspend fun descontarStock(id: Long, cantidad: Int)

    @Query("DELETE FROM ofertas")
    suspend fun borrarTodas()
}
