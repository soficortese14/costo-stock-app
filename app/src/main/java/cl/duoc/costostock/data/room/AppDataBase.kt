package cl.duoc.costostock.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProveedorEntity::class],
    version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase(){

    abstract fun proveedorDAO(): ProveedorDAO

    companion object{
        // Volatile + synchronized: nos aseguramos de crear una ÚNICA instancia
        // de la base de datos en toda la app (patrón Singleton clásico).
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return  INSTANCE ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "proveedores_db" //nombre del archivo .db en el dispositivo
                ).build().also { INSTANCE = it }
            }
        }
    }
}
