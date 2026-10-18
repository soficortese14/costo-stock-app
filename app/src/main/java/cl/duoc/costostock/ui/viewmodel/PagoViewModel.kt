package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.DatosTarjeta
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.PasarelaPago
import cl.duoc.costostock.data.ResultadoPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * En que punto va la transaccion.
 *
 * Se modela con sealed class, igual que EstadoUbicacion y EstadoDetalle,
 * para que el when de la pantalla sea exhaustivo: si manana se agrega un
 * estado, el compilador obliga a dibujarlo.
 */
sealed class EstadoPago {

    /** Llenando la tarjeta. */
    object Formulario : EstadoPago()

    /** Esperando a la pasarela. No se puede volver atras aca. */
    object Procesando : EstadoPago()

    data class Aprobado(val resultado: ResultadoPago.Aprobado) : EstadoPago()

    data class Rechazado(val motivo: String) : EstadoPago()
}

/** Errores de cada campo. null significa que esta bien. */
data class ErroresTarjeta(
    val numero: String? = null,
    val nombre: String? = null,
    val vencimiento: String? = null,
    val cvv: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(numero, nombre, vencimiento, cvv).any { it != null }
}

/**
 * CO-11 Pago.
 *
 * La pasarela es simulada y esta documentado por que: la llave de comercio
 * no puede viajar dentro del APK. Ver PasarelaPago.
 */
class PagoViewModel(application: Application) : AndroidViewModel(application) {

    private val carrito = CarritoRepository(application)
    private val ofertas = OfertaRepository(application)

    private val _tarjeta = MutableStateFlow(DatosTarjeta())
    val tarjeta: StateFlow<DatosTarjeta> = _tarjeta

    private val _errores = MutableStateFlow(ErroresTarjeta())
    val errores: StateFlow<ErroresTarjeta> = _errores

    private val _estado = MutableStateFlow<EstadoPago>(EstadoPago.Formulario)
    val estado: StateFlow<EstadoPago> = _estado

    private val _monto = MutableStateFlow(0)
    val monto: StateFlow<Int> = _monto

    init {
        // El monto se congela al entrar: el usuario tiene que ver el mismo
        // numero que acepto, aunque una oferta venza mientras paga.
        viewModelScope.launch {
            val items = carrito.observarItems().first()
            _monto.value = CarritoRepository.resumir(items).total
        }
    }

    /*
     * Se guardan solo los digitos. Los espacios y la barra los pone la
     * VisualTransformation al dibujar: si se metieran en el valor, el
     * cursor se correria y el numero se desordenaria al escribir.
     */
    fun cambiarNumero(valor: String) {
        _tarjeta.value = _tarjeta.value.copy(
            numero = PasarelaPago.soloDigitos(valor).take(19)
        )
        _errores.value = _errores.value.copy(numero = null)
    }

    fun cambiarNombre(valor: String) {
        _tarjeta.value = _tarjeta.value.copy(nombre = valor)
        _errores.value = _errores.value.copy(nombre = null)
    }

    fun cambiarVencimiento(valor: String) {
        _tarjeta.value = _tarjeta.value.copy(
            vencimiento = PasarelaPago.soloDigitos(valor).take(4)
        )
        _errores.value = _errores.value.copy(vencimiento = null)
    }

    fun cambiarCvv(valor: String) {
        _tarjeta.value = _tarjeta.value.copy(
            cvv = PasarelaPago.soloDigitos(valor).take(4)
        )
        _errores.value = _errores.value.copy(cvv = null)
    }

    /**
     * Rellena el formulario con una tarjeta de prueba.
     *
     * Existe porque el entorno es simulado y escribir 16 digitos a mano
     * para cada prueba no aporta nada. En una app real este boton no iria.
     */
    fun usarTarjetaDePrueba(aprueba: Boolean) {
        _tarjeta.value =
            if (aprueba) PasarelaPago.TARJETA_APRUEBA
            else PasarelaPago.TARJETA_RECHAZA

        _errores.value = ErroresTarjeta()
    }

    /**
     * Valida y cobra.
     *
     * Las reglas estan en PasarelaPago como funciones puras; aca solo se
     * traducen a mensajes para la pantalla.
     */
    fun pagar() {
        val t = _tarjeta.value

        val errores = ErroresTarjeta(
            numero = if (PasarelaPago.numeroValido(t.numero)) null
            else "Numero de tarjeta invalido",
            nombre = if (PasarelaPago.nombreValido(t.nombre)) null
            else "Escribe el nombre como aparece en la tarjeta",
            vencimiento = if (PasarelaPago.vencimientoValido(t.vencimiento)) null
            else "Fecha invalida o ya vencida",
            cvv = if (PasarelaPago.cvvValido(t.cvv)) null
            else "El codigo son 3 o 4 digitos"
        )

        _errores.value = errores
        if (errores.hayErrores) return

        viewModelScope.launch {
            _estado.value = EstadoPago.Procesando

            when (val resultado = PasarelaPago.procesar(t, _monto.value)) {
                is ResultadoPago.Aprobado -> {
                    confirmarCompra()
                    _estado.value = EstadoPago.Aprobado(resultado)
                }

                is ResultadoPago.Rechazado -> {
                    _estado.value = EstadoPago.Rechazado(resultado.motivo)
                }
            }
        }
    }

    /**
     * Descuenta el stock y vacia el carrito.
     *
     * Solo corre si la pasarela aprobo: si se descontara antes, un pago
     * rechazado dejaria productos reservados que nadie compro.
     */
    private suspend fun confirmarCompra() {
        val items = carrito.observarItems().first()

        items.forEach { item ->
            ofertas.comprar(item.oferta.id, item.cantidad)
        }

        carrito.vaciar()
    }

    /** Vuelve al formulario para reintentar con otra tarjeta. */
    fun reintentar() {
        _estado.value = EstadoPago.Formulario
    }
}
