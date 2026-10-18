package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class ProveedorEntity(
    @PrimaryKey val rut: String,
    val password: String,
    val razonSocial: String,
    val giro: String,
    val comuna: String,
    val telefono: String
)