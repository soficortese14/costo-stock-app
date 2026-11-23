package cl.duoc.costostock.network

data class UsuarioRequest (
    val correo : String,
    val nombre : String,
    val telefono: String,
    val contrasena: String
)

data class LoginRequest (
    val correo : String,
    val contrasena: String
)

data class UsuarioResponse(
    val correo : String,
    val nombre : String,
    val telefono : String
)
