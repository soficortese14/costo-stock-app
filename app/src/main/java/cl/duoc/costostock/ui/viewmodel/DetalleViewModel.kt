package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.data.PreferenciasManager
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estados de la pantalla de detalle.
 *
 * Igual que EstadoUbicacion, se modela con sealed class para que el when
 * de la pantalla sea exhaustivo y el compilador avise si falta un caso.
 */
sealed class EstadoDetalle {
    object Cargando : EstadoDetalle()
    data class Listo(val oferta: OfertaEntity) : EstadoDetalle()
    object NoEncontrada : EstadoDetalle()
}

/**
 * CO-05 Detalle de producto.
 *
 * Recibe el id de la oferta que llego por la ruta y la busca en Room.
 */
class DetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = OfertaRepository(application)
    private val preferencias = PreferenciasManager(application)
    private val carrito = CarritoRepository(application)

    /**
     * Perfil con el que se navega. Decide si se ofrece editar la oferta:
     * un comprador no puede modificar lo que publico otro.
     */
    val perfilActivo: StateFlow<Perfil> =
        preferencias.perfilActivo.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Perfil.COMPRADOR
        )

    private val _estado = MutableStateFlow<EstadoDetalle>(EstadoDetalle.Cargando)
    val estado: StateFlow<EstadoDetalle> = _estado

    /** Marca de tiempo que avanza cada segundo, para el contador. */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora

    /** true mientras se procesa una compra, para bloquear el boton. */
    private val _comprando = MutableStateFlow(false)
    val comprando: StateFlow<Boolean> = _comprando

    /** Mensaje que la pantalla muestra y luego limpia. */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    init {
        iniciarReloj()
    }

    /**
     * Carga la oferta. La pantalla la llama una sola vez, con el id
     * que extrajo de la ruta.
     */
    fun cargar(ofertaId: Long) {
        viewModelScope.launch {
            _estado.value = EstadoDetalle.Cargando

            val oferta = repositorio.obtenerPorId(ofertaId)

            _estado.value =
                if (oferta != null) EstadoDetalle.Listo(oferta)
                else EstadoDetalle.NoEncontrada
        }
    }

    private fun iniciarReloj() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _ahora.value = System.currentTimeMillis()
            }
        }
    }

    /**
     * Descuenta una unidad del stock y recarga la oferta.
     * Demuestra escritura en Room desde la interfaz.
     */
    fun comprar(oferta: OfertaEntity) {
        viewModelScope.launch {
            _comprando.value = true

            repositorio.comprar(oferta.id, 1)
            cargar(oferta.id)

            _comprando.value = false
            _mensaje.value = "Compra realizada. Evitaste " +
                "${oferta.co2Evitado} kg de CO2"
        }
    }

    /** La pantalla llama a esto despues de mostrar el mensaje. */
    /**
     * Agrega una unidad al carrito, sin pasarse del stock.
     *
     * Agregar no descuenta stock: eso recien ocurre cuando la pasarela
     * aprueba el pago. Hasta entonces el producto sigue disponible para
     * los demas, que es como funciona cualquier tienda.
     */
    fun agregarAlCarrito(oferta: OfertaEntity) {
        viewModelScope.launch {
            val agregado = carrito.agregar(
                ofertaId = oferta.id,
                cantidad = 1,
                stockDisponible = oferta.stockActual
            )

            _mensaje.value =
                if (agregado) "Agregado al carrito"
                else "Ya tienes todo el stock disponible en el carrito"
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
