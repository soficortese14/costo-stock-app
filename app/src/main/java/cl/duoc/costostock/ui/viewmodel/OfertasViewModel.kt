package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.data.PreferenciasManager
import cl.duoc.costostock.data.Ubicacion
import cl.duoc.costostock.data.UbicacionManager
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
 * Pantalla de ofertas: resuelve la ubicacion del usuario y entrega
 * el catalogo filtrado por categoria.
 */
class OfertasViewModel(application: Application) : AndroidViewModel(application) {

    private val ubicacionManager = UbicacionManager(application)
    private val repositorio = OfertaRepository(application)
    private val preferencias = PreferenciasManager(application)

    /**
     * Perfil con el que se esta navegando. Decide si se muestra el boton
     * de publicar: solo el proveedor puede cargar ofertas.
     */
    val perfilActivo: StateFlow<Perfil> =
        preferencias.perfilActivo.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Perfil.COMPRADOR
        )

    private val _estado = MutableStateFlow<EstadoUbicacion>(EstadoUbicacion.Cargando)
    val estado: StateFlow<EstadoUbicacion> = _estado

    // Radio de busqueda en kilometros. El cliente pidio 15 por defecto,
    // ajustable hasta 30.
    private val _radioKm = MutableStateFlow(15)
    val radioKm: StateFlow<Int> = _radioKm

    private val _categoria = MutableStateFlow(OfertaRepository.CATEGORIA_TODAS)
    val categoria: StateFlow<String> = _categoria

    /**
     * Marca de tiempo que avanza cada segundo.
     *
     * Los contadores regresivos la observan: al cambiar, Compose redibuja
     * solo los textos del tiempo restante y no toda la lista.
     */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora

    /**
     * El catalogo. Se arma combinando la categoria elegida con lo que
     * emite Room: al cambiar el filtro o la tabla, la lista se recalcula sola.
     */
    val ofertas: StateFlow<List<OfertaEntity>> =
        _categoria
            .combine(repositorio.observarOfertas()) { categoriaActual, todas ->
                if (categoriaActual == OfertaRepository.CATEGORIA_TODAS) todas
                else todas.filter { it.categoria == categoriaActual }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        cargarCatalogo()
        cargarUbicacion()
        iniciarReloj()
    }

    /** Puebla la base la primera vez que se abre la app. */
    private fun cargarCatalogo() {
        viewModelScope.launch {
            repositorio.cargarDatosIniciales()
        }
    }

    /** Hace avanzar los contadores regresivos. */
    private fun iniciarReloj() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _ahora.value = System.currentTimeMillis()
            }
        }
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

    fun cambiarCategoria(nueva: String) {
        _categoria.value = nueva
    }

    /** Cambia el radio de busqueda, entre 1 y 30 km. */
    fun cambiarRadio(km: Int) {
        _radioKm.value = km.coerceIn(1, 30)
    }

    /** Descuenta una unidad del stock. Demuestra escritura en Room. */
    fun comprar(oferta: OfertaEntity) {
        viewModelScope.launch {
            repositorio.comprar(oferta.id, 1)
        }
    }

    /** Vuelve a dejar el catalogo como al inicio. */
    fun reiniciarCatalogo() {
        viewModelScope.launch {
            repositorio.reiniciarCatalogo()
        }
    }
}
