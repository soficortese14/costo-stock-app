package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.ResumenCompra
import cl.duoc.costostock.data.room.ItemCarrito
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CO-09 Carrito de compras.
 *
 * El carrito vive en Room, asi que esta pantalla solo observa la tabla:
 * si se agrega un producto desde el detalle, aparece aca sin que nadie
 * recargue nada.
 */
class CarritoViewModel(application: Application) : AndroidViewModel(application) {

    private val carrito = CarritoRepository(application)

    /** Avanza cada segundo: los precios cambian cuando una oferta vence. */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    val items: StateFlow<List<ItemCarrito>> =
        carrito.observarItems().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * El resumen se recalcula con la hora, no solo con el contenido.
     *
     * Si una oferta vence mientras esta en el carrito, su precio vuelve
     * al original y el total sube solo. Es la regla del cliente aplicada
     * tambien aca, no nada mas en el catalogo.
     */
    val resumen: StateFlow<ResumenCompra> =
        combine(items, _ahora) { lista, instante ->
            CarritoRepository.resumir(lista, instante)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CarritoRepository.resumir(emptyList())
        )

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _ahora.value = System.currentTimeMillis()
            }
        }
    }

    fun sumarUno(item: ItemCarrito) {
        viewModelScope.launch {
            val tope = item.oferta.stockActual

            if (item.cantidad >= tope) {
                _mensaje.value = "Solo quedan $tope unidades de este producto"
                return@launch
            }

            carrito.cambiarCantidad(item.oferta.id, item.cantidad + 1, tope)
        }
    }

    fun restarUno(item: ItemCarrito) {
        viewModelScope.launch {
            carrito.cambiarCantidad(
                item.oferta.id,
                item.cantidad - 1,
                item.oferta.stockActual
            )
        }
    }

    fun quitar(item: ItemCarrito) {
        viewModelScope.launch { carrito.quitar(item.oferta.id) }
    }

    fun vaciar() {
        viewModelScope.launch { carrito.vaciar() }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
