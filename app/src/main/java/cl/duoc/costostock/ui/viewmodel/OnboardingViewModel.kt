package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.PreferenciasManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Guarda que el onboarding ya se mostro, para que el Splash
 * no vuelva a mandar al usuario aqui la proxima vez que abra la app.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = PreferenciasManager(application)

    // false mientras no se termine; true cuando ya se guardo
    private val _terminado = MutableStateFlow(false)
    val terminado: StateFlow<Boolean> = _terminado

    // Guarda si el usuario concedio el permiso de ubicacion,
    // para mostrarselo en la ultima lamina
    private val _permisoConcedido = MutableStateFlow(false)
    val permisoConcedido: StateFlow<Boolean> = _permisoConcedido

    /** La pantalla avisa que respondio el dialogo del sistema. */
    fun registrarRespuestaPermiso(concedido: Boolean) {
        _permisoConcedido.value = concedido
    }

    /** Se llama al tocar "Comenzar" en la ultima lamina. */
    fun terminarOnboarding() {
        viewModelScope.launch {
            preferencias.marcarOnboardingVisto()
            _terminado.value = true
        }
    }
}
