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

    /**
     * Se enciende cuando hay que llevar al usuario al carrito.
     *
     * Se avisa con una bandera y no navegando desde el ViewModel porque
     * el ViewModel no conoce el NavController: su trabajo es decir que
     * paso, y la pantalla decide a donde ir. Es el mismo patron de
     * _publicada y _eliminada en PublicarOfertaViewModel.
     */
    private val _irAlCarrito = MutableStateFlow(false)
    val irAlCarrito: StateFlow<Boolean> = _irAlCarrito

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
     * Comprar: deja la oferta en el carrito y manda al checkout.
     *
     * Antes esta funcion descontaba el stock aca mismo y mostraba
     * "Compra realizada", sin pasar por la pasarela. Eran dos caminos de
     * compra distintos y solo uno generaba codigo de retiro: el que
     * apretaba Comprar en el detalle se quedaba sin pedido, sin codigo y
     * sin voucher, aunque la app le dijera que habia comprado. Ademas
     * rompia la regla de que el stock se descuenta recien cuando la
     * pasarela aprueba, porque bastaba tocar el boton para bajarlo.
     *
     * Ahora hay un solo camino de compra: carrito, pasarela, pedido con
     * codigo de retiro y su QR.
     */
    fun comprarAhora(oferta: OfertaEntity) {
        viewModelScope.launch {
            _comprando.value = true

            val agregado = carrito.agregar(
                ofertaId = oferta.id,
                cantidad = 1,
                stockDisponible = oferta.stockActual
            )

            /*
             * Si no se agrego es porque el carrito ya tiene todo el stock
             * disponible. Igual corresponde ir al carrito (el producto ya
             * esta ahi), pero avisando por que no subio la cantidad.
             */
            if (!agregado) {
                _mensaje.value = "Ya tienes todo el stock disponible en el carrito"
            }

            _comprando.value = false
            _irAlCarrito.value = true
        }
    }

    /** La pantalla apaga la bandera despues de navegar. */
    fun carritoAbierto() {
        _irAlCarrito.value = false
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
