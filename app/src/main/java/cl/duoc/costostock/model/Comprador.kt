package cl.duoc.costostock.model

/**
 * Modelo de datos para representar a un Comprador registrado en la aplicación.
 *
 * @property email Correo electrónico del comprador.
 * @property password Contraseña de la cuenta.
 * @property numero Número de teléfono de contacto.
 */
data class Comprador(
    val email: String,
    val password: String,
    val numero: String
)
