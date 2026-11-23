package cl.duoc.costostock.data

import cl.duoc.costostock.network.LoginRequest
import cl.duoc.costostock.network.RetrofitClient
import cl.duoc.costostock.network.UsuarioAPI
import cl.duoc.costostock.network.UsuarioRequest

class UsuarioRepository(
    private val api: UsuarioAPI = RetrofitClient.usuarioApi
) {

    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        telefono: String,
        contrasena: String
    ): String? {
        return try {
            val request = UsuarioRequest(
                correo = correo,
                nombre = nombre,
                telefono = telefono,
                contrasena = contrasena
            )
            val response = api.registrar(request)

            if (response.isSuccessful && response.body() != null) {
                null
            } else {
                val detalle = response.errorBody()?.string() ?: "Sin detalle"
                "Error ${response.code()}: $detalle"
            }
        } catch (e: Exception) {
            "Fallo de red (${e.javaClass.simpleName}): ${e.message}"
        }
    }

    suspend fun validarLogin(correo: String, contrasena: String): String? {
        return try {
            val request = LoginRequest(
                correo = correo,
                contrasena = contrasena
            )
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                null
            } else {
                val detalle = response.errorBody()?.string() ?: "Sin detalle"
                "Error ${response.code()}: $detalle"
            }
        } catch (e: Exception) {
            "Fallo de red (${e.javaClass.simpleName}): ${e.message}"
        }
    }
}