package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.PedidoRepository
import cl.duoc.costostock.data.room.PedidoEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** En que va la consulta. */
sealed class EstadoConsulta {
    object Inicial : EstadoConsulta()
    object Buscando : EstadoConsulta()
    data class Encontrado(val pedido: PedidoEntity) : EstadoConsulta()
    object NoEncontrado : EstadoConsulta()
}

/**
 * CO-12 Consulta de pedido.
 *
 * El comprador escribe el codigo de retiro que recibio al pagar y ve en
 * que etapa va. No hace falta tener sesion iniciada: el codigo es la
 * credencial, igual que en cualquier seguimiento de pedido.
 */
class ConsultaPedidoViewModel(application: Application) : AndroidViewModel(application) {

    private val pedidos = PedidoRepository(application)

    private val _codigo = MutableStateFlow("")
    val codigo: StateFlow<String> = _codigo

    private val _estado = MutableStateFlow<EstadoConsulta>(EstadoConsulta.Inicial)
    val estado: StateFlow<EstadoConsulta> = _estado

    /** Avanza cada segundo: el estado del pedido depende del reloj. */
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

    fun cambiarCodigo(valor: String) {
        // El codigo se guarda en mayusculas para que dé lo mismo como se
        // escriba; la busqueda ademas ignora mayusculas en el SQL.
        _codigo.value = valor.uppercase().take(20)

        if (_estado.value is EstadoConsulta.NoEncontrado) {
            _estado.value = EstadoConsulta.Inicial
        }
    }

    fun buscar() {
        val codigo = _codigo.value.trim()
        if (codigo.isBlank()) return

        viewModelScope.launch {
            _estado.value = EstadoConsulta.Buscando

            // Una espera corta para que se note que fue a buscar
            delay(600)

            val pedido = pedidos.buscar(codigo)

            _estado.value =
                if (pedido != null) EstadoConsulta.Encontrado(pedido)
                else EstadoConsulta.NoEncontrado
        }
    }

    /** Busca de inmediato un codigo que llego por la ruta. */
    fun buscarDirecto(codigo: String) {
        _codigo.value = codigo
        buscar()
    }
}
