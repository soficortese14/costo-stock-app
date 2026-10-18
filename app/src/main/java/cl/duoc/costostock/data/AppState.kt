package cl.duoc.costostock.data

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import cl.duoc.costostock.model.Comprador

class AppState {
    val compradores = mutableStateListOf<Comprador>()

    /**
     * Registra un nuevo comprador en el estado en memoria de la aplicación.
     * Retorna false si el email ya se encuentra registrado.
     */
    fun registrarComprador(
        nombre: String,
        email: String,
        numero: String,
        password: String,
        fotoPerfil: Bitmap? = null,
        aceptoTerminos: Boolean = true
    ): Boolean {
        if (compradores.any { it.email == email }) return false
        compradores.add(
            Comprador(
                nombre = nombre,
                email = email,
                numero = numero,
                password = password,
                fotoPerfil = fotoPerfil,
                aceptoTerminos = aceptoTerminos
            )
        )
        return true
    }
}
