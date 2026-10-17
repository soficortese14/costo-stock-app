package cl.duoc.costostock.data

import cl.duoc.costostock.data.room.UsuarioDAO
import cl.duoc.costostock.data.room.UsuarioEntity
import java.security.MessageDigest

class UsuarioRepository(private val usuarioDao: UsuarioDAO){

    suspend fun registrarUsuario(correo: String, contrasena: String): Boolean {
        val usuarioExistente = usuarioDao.verificarCorreo(correo)
        if (usuarioExistente != null) {
            return false // El correo existe, rechazamos el registro
        }

        val nuevoUsuario = UsuarioEntity(correo = correo, contrasena = contrasena)
        val idGenerado = usuarioDao.insertarUsuario(nuevoUsuario)

        //Mayor que 0 éxito, -1 error
        return idGenerado > 0
    }

    suspend fun validarLogin(correo: String, contrasena: String): Boolean {
        val usuario = usuarioDao.verificarLogin(correo, contrasena)
        return usuario != null
    }

    fun hashearContrasena(contrasena: String): String {
        val pwHasheada = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray())
        return pwHasheada.joinToString("") { "%02x".format(it) }
    }
}