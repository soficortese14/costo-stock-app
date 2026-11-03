package cl.duoc.costostock.data

import cl.duoc.costostock.data.room.UsuarioDAO
import cl.duoc.costostock.data.room.UsuarioEntity
import java.security.MessageDigest

class UsuarioRepository(private val usuarioDao: UsuarioDAO) {

    // Ahora recibimos los 4 datos desde el ViewModel
    suspend fun registrarUsuario(nombre: String, correo: String, telefono: String, contrasena: String): Boolean {
        val usuarioExistente = usuarioDao.verificarCorreo(correo)
        if (usuarioExistente != null) {
            return false // El correo existe, rechazamos el registro
        }

        // Armamos la entidad con todos los datos del formulario
        val nuevoUsuario = UsuarioEntity(
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            contrasena = hashearContrasena(contrasena)
        )
        val idGenerado = usuarioDao.insertarUsuario(nuevoUsuario)

        // Mayor que 0 éxito, -1 error
        return idGenerado > 0
    }

    suspend fun validarLogin(correo: String, contrasena: String): Boolean {
        val usuario = usuarioDao.verificarLogin(correo, hashearContrasena(contrasena))
        return usuario != null
    }

    private fun hashearContrasena(contrasena: String): String {
        val pwHasheada = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray())
        return pwHasheada.joinToString("") { "%02x".format(it) }
    }
}