package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun marcar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE ofertaId = :ofertaId")
    suspend fun desmarcar(ofertaId: Long)

    /** true si esa oferta esta en favoritos. Para pintar el corazon. */
    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE ofertaId = :ofertaId)")
    fun esFavorito(ofertaId: Long): Flow<Boolean>

    /**
     * Solo los ids.
     *
     * La lista de ofertas ya la tiene la pantalla; con los ids alcanza
     * para saber cual corazon va relleno. Traer las ofertas completas
     * seria pedir dos veces lo mismo.
     */
    @Query("SELECT ofertaId FROM favoritos")
    fun observarIds(): Flow<List<Long>>

    /**
     * Las ofertas favoritas, de la mas reciente a la mas antigua.
     *
     * El JOIN con ofertas es lo que garantiza que no aparezca una
     * favorita cuya oferta ya no existe.
     */
    @Query(
        """
        SELECT o.* FROM ofertas o
        INNER JOIN favoritos f ON f.ofertaId = o.id
        ORDER BY f.marcadoEn DESC
        """
    )
    fun observarOfertas(): Flow<List<OfertaEntity>>

    @Query("SELECT COUNT(*) FROM favoritos")
    fun contar(): Flow<Int>
}
