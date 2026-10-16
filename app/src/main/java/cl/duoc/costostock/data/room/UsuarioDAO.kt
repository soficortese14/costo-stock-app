package cl.duoc.costostock.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDAO{

    //Registro
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuario(usuario: UsuarioEntity) : Long

    //Login verificar si existe con ese correo y pw
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun verificarLogin(correo: String, contrasena: String) : UsuarioEntity?

    //Si correo ya está registrado
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun verificarCorreo(correo: String) : UsuarioEntity?
}
