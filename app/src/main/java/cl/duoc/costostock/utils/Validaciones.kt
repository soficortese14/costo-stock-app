package cl.duoc.costostock.utils

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val NUMERO_CHILENO_REGEX = Regex("^(?:\\+?56)?\\s?9\\d{8}$")

/**
 * Valida si el formato del correo electrónico es correcto.
 */
fun esEmailValido(email: String): Boolean {
    return EMAIL_REGEX.matches(email.trim())
}

/**
 * Valida si el número telefónico corresponde a un número chileno válido.
 * Ejemplos válidos: 912345678, +56912345678, 56912345678
 */
fun esNumeroChilenoValido(numero: String): Boolean {
    val numeroLimpio = numero.replace(" ", "").replace("-", "")
    return NUMERO_CHILENO_REGEX.matches(numeroLimpio)
}
