package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProveedorDAO {
    @Insert
    suspend fun insertar(proveedor: ProveedorEntity)

    // Se usa una sola vez, al iniciar la app, para cargar todos los
    // usuarios a memoria (mismo patrón que el "cargarDatos()" original).
    @Query("SELECT * FROM proveedores")
    suspend fun obtenerTodos(): List<ProveedorEntity>
}

