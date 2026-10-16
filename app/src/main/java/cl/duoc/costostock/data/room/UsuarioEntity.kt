package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val correo : String,
    val contrasena : String
)