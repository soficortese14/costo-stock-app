package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Operaciones sobre el carrito. */
@Dao
interface CarritoDAO {

    /**
     * El carrito con los datos de cada producto.
     *
     * El INNER JOIN tiene un efecto util: si el proveedor elimina una
     * oferta, la linea desaparece sola del carrito en vez de quedar
     * apuntando a un producto que ya no existe.
     */
    @Query(
        """
        SELECT o.*, c.cantidad AS cantidad
        FROM carrito c
        INNER JOIN ofertas o ON o.id = c.ofertaId
        ORDER BY o.titulo COLLATE NOCASE
        """
    )
    fun observarItems(): Flow<List<ItemCarrito>>

    /**
     * Unidades totales, para el globo del icono del carrito.
     *
     * Cruza con ofertas igual que observarItems: asi el numero del globo
     * nunca puede diferir de lo que el usuario ve al abrir el carrito.
     */
    @Query(
        """
        SELECT COALESCE(SUM(c.cantidad), 0)
        FROM carrito c
        INNER JOIN ofertas o ON o.id = c.ofertaId
        """
    )
    fun observarUnidades(): Flow<Int>

    /** Cuanto hay ya de este producto. null si no esta en el carrito. */
    @Query("SELECT cantidad FROM carrito WHERE ofertaId = :ofertaId")
    suspend fun cantidadDe(ofertaId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(linea: CarritoEntity)

    @Query("DELETE FROM carrito WHERE ofertaId = :ofertaId")
    suspend fun quitar(ofertaId: Long)

    @Query("DELETE FROM carrito")
    suspend fun vaciar()
}
