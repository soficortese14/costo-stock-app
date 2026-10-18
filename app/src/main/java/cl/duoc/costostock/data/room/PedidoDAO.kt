package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(pedido: PedidoEntity)

    /**
     * Busca por codigo ignorando mayusculas y espacios sobrantes.
     * El comprador lo va a escribir a mano desde una pantalla o un papel.
     */
    @Query("SELECT * FROM pedidos WHERE codigo = :codigo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): PedidoEntity?

    /** Historial, del mas reciente al mas antiguo. */
    @Query("SELECT * FROM pedidos ORDER BY fechaPago DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>
}
