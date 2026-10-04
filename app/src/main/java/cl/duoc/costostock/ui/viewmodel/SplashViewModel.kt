package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.PreferenciasManager
import cl.duoc.costostock.navigation.Rutas
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * es el q decide a que pantalla entra la app al abrirse
 *
 * no navega: solo expone el destino. Quien navega es SplashScreen
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = PreferenciasManager(application)

    // Mutable y privado: solo el ViewModel puede cambiarlo
    private val _destino = MutableStateFlow<String?>(null)

    // Publico y de solo lectura: la pantalla observa, no modifica
    val destino: StateFlow<String?> = _destino

    // Se ejecuta apenas se crea el ViewModel
    init {
        decidirDestino()
    }

    private fun decidirDestino() {
        viewModelScope.launch {
            // Tiempo minimo visible del logo
            delay(1500)

            val yaVioOnboarding = preferencias.onboardingVisto.first()

            _destino.value =
                if (yaVioOnboarding) Rutas.OFERTAS else Rutas.ONBOARDING
        }
    }
}