package cl.duoc.costostock.model

data class Proveedor(
    val email: String,
    val rut: String,
    val password: String,
    val razonSocial: String,
    val giro: String,
    val comuna: String,
    val telefono: String
)