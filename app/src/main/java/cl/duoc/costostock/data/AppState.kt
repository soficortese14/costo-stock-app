package cl.duoc.costostock.data

import androidx.compose.runtime.mutableStateListOf
import cl.duoc.costostock.model.Comprador

class AppState {
    val compradores = mutableStateListOf<Comprador>()

    /**
     * Función para registrar a un comprador en el estado de la aplicación.
     * Retorna false si el email ya se encuentra registrado.
     */
    fun registrarComprador(email: String, password: String, numero: String): Boolean {
        if (compradores.any { it.email == email }) return false
        compradores.add(Comprador(email, password, numero))
        return true
    }
}
