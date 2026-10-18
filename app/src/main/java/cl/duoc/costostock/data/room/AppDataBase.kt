package cl.duoc.costostock.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicacion (SQLite a traves de Room).
 *
 * Cada entidad de la lista es una tabla. Al agregar una nueva hay que
 * subir el numero de version, o Room lanza un error al abrir la base.
 */
@Database(
    entities = [
        ProveedorEntity::class,
        OfertaEntity::class,
        CarritoEntity::class,
        PedidoEntity::class,
        UsuarioEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proveedorDAO(): ProveedorDAO
    abstract fun ofertaDAO(): OfertaDAO
    abstract fun carritoDAO(): CarritoDAO
    abstract fun pedidoDAO(): PedidoDAO

    abstract fun usuarioDAO(): UsuarioDAO

    companion object {
        // Volatile + synchronized: nos aseguramos de crear una ÚNICA instancia
        // de la base de datos en toda la app (patrón Singleton clásico).
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "costostock_db" //nombre del archivo .db en el dispositivo
                )
                    // En desarrollo: si cambia el esquema, se borra y se recrea
                    // en vez de exigir una migracion escrita a mano.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
