package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.PedidoRepository
import cl.duoc.costostock.data.room.PedidoEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Qué mostrar en la pantalla de vouchers.
 *
 * Se modela con sealed class para que el when de la pantalla sea
 * exhaustivo, igual que EstadoUbicacion, EstadoPago y EstadoConsulta.
 *
 * Vacío es un estado propio y no una lista de cero elementos: la
 * pantalla tiene que decir algo distinto cuando nunca se ha comprado
 * nada, y con una lista vacía ese caso se confundiría con el de
 * "todavía estoy cargando".
 */
sealed class EstadoVouchers {
    object Cargando : EstadoVouchers()
    data class ConVouchers(val pedidos: List<PedidoEntity>) : EstadoVouchers()
    object Vacio : EstadoVouchers()
}

/**
 * CO-13 Vouchers.
 *
 * El historial de compras con el código de retiro de cada una. Responde
 * al problema de que hasta ahora el código solo se veía una vez, al
 * terminar de pagar: quedaba guardado en Room pero no había pantalla
 * que lo leyera y había que escribirlo de memoria en la consulta.
 */
class VouchersViewModel(application: Application) : AndroidViewModel(application) {

    private val pedidos = PedidoRepository(application)

    /**
     * La lista viene de Room como Flow, así que un pedido nuevo aparece
     * sin refrescar nada.
     *
     * stateIn convierte ese Flow frío en un StateFlow caliente que
     * sobrevive a los cambios de configuración: girar el teléfono no
     * vuelve a consultar la base. WhileSubscribed(5s) corta la
     * suscripción al salir de la pantalla, pero la mantiene unos
     * segundos para que ir y volver no pague la consulta de nuevo.
     */
    val estado: StateFlow<EstadoVouchers> =
        pedidos.observarTodos()
            .map { lista ->
                if (lista.isEmpty()) EstadoVouchers.Vacio
                else EstadoVouchers.ConVouchers(lista)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EstadoVouchers.Cargando
            )

    /**
     * Avanza cada segundo porque la etapa del pedido se deriva del reloj,
     * no se guarda en la tabla. Es el mismo mecanismo que usa
     * ConsultaPedidoViewModel.
     */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _ahora.value = System.currentTimeMillis()
            }
        }
    }
}
