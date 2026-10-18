package cl.duoc.costostock.model

import android.graphics.Bitmap

/**
 * Modelo de datos para representar a un Comprador registrado.
 */
data class Comprador(
    val nombre: String,
    val email: String,
    val numero: String,
    val password: String,
    val fotoPerfil: Bitmap? = null,
    val aceptoTerminos: Boolean = true
)
