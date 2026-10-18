package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.Ubicacion
import cl.duoc.costostock.data.UbicacionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Los estados posibles de la ubicacion del usuario.
 *
 * Se modela con sealed class para que el when de la pantalla sea exhaustivo:
 * si manana se agrega un estado nuevo, el compilador obliga a dibujarlo.
 */
sealed class EstadoUbicacion {

    /** Se esta consultando al dispositivo. */
    object Cargando : EstadoUbicacion()

    /** Se obtuvo la posicion. */
    data class Disponible(val ubicacion: Ubicacion) : EstadoUbicacion()

    /** El usuario no concedio el permiso de ubicacion. */
    object SinPermiso : EstadoUbicacion()

    /** Hay permiso, pero el dispositivo no entrego una posicion. */
    object NoDisponible : EstadoUbicacion()
}

/**
 * Pantalla de ofertas. Por ahora solo resuelve la ubicacion del usuario;
 * el listado de ofertas se agrega despues sobre esta misma base.
 */
class OfertasViewModel(application: Application) : AndroidViewModel(application) {

    private val ubicacionManager = UbicacionManager(application)

    private val _estado = MutableStateFlow<EstadoUbicacion>(EstadoUbicacion.Cargando)
    val estado: StateFlow<EstadoUbicacion> = _estado

    // Radio de busqueda en kilometros. El cliente pidio 15 por defecto,
    // ajustable hasta 30.
    private val _radioKm = MutableStateFlow(15)
    val radioKm: StateFlow<Int> = _radioKm

    init {
        cargarUbicacion()
    }

    /** Consulta la ubicacion. La pantalla tambien la llama al reintentar. */
    fun cargarUbicacion() {
        viewModelScope.launch {
            _estado.value = EstadoUbicacion.Cargando

            if (!ubicacionManager.hayPermiso()) {
                _estado.value = EstadoUbicacion.SinPermiso
                return@launch
            }

            val ubicacion = ubicacionManager.obtenerUbicacion()

            _estado.value =
                if (ubicacion != null) EstadoUbicacion.Disponible(ubicacion)
                else EstadoUbicacion.NoDisponible
        }
    }

    /** Cambia el radio de busqueda, entre 1 y 30 km. */
    fun cambiarRadio(km: Int) {
        _radioKm.value = km.coerceIn(1, 30)
    }
}
