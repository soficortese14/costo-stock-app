package cl.duoc.costostock.data

import kotlinx.coroutines.delay
import java.util.Locale

/** Lo que el usuario escribe en la pasarela. */
data class DatosTarjeta(
    val numero: String = "",
    val nombre: String = "",
    val vencimiento: String = "",
    val cvv: String = ""
)

/** Como termino el intento de pago. */
sealed class ResultadoPago {

    data class Aprobado(
        val codigoOrden: String,
        val ultimosCuatro: String,
        val monto: Int
    ) : ResultadoPago()

    data class Rechazado(val motivo: String) : ResultadoPago()
}

/**
 * Pasarela de pago simulada.
 *
 * Por que es simulada y no una integracion real: el codigo de comercio y
 * la llave de una pasarela viven en el servidor, nunca dentro del APK.
 * Una app Android que las lleve adentro las esta publicando, porque un
 * APK se puede abrir. En produccion la app le pediria el cobro al backend
 * en Spring Boot y ese backend hablaria con la pasarela.
 *
 * Se documenta como integracion simulada, igual que la validacion del SII.
 *
 * Lo que si es real aca: la validacion de la tarjeta. El numero se revisa
 * con el algoritmo de Luhn, que es el mismo que usan las pasarelas de
 * verdad para descartar numeros mal escritos antes de cobrar.
 */
object PasarelaPago {

    /**
     * Tarjetas de prueba del entorno simulado.
     *
     * Las pasarelas reales publican numeros asi para que se pueda probar
     * sin cobrar dinero. Los dos pasan la validacion de Luhn; lo que los
     * diferencia es el ultimo digito, que decide si el emisor aprueba.
     */
    val TARJETA_APRUEBA = DatosTarjeta(
        numero = "4111111111111111",
        nombre = "SOFIA CORTESE",
        vencimiento = "1228",
        cvv = "123"
    )

    val TARJETA_RECHAZA = DatosTarjeta(
        numero = "4000000000000010",
        nombre = "SOFIA CORTESE",
        vencimiento = "1228",
        cvv = "123"
    )

    /** Lo que demora un cobro real, para que la espera se sienta creible. */
    private const val DEMORA_PROCESO_MS = 2_000L

    /**
     * Cobra el monto. Devuelve aprobado o rechazado.
     *
     * El rechazo es deterministico y no al azar: una tarjeta cuyo numero
     * termina en 0 siempre se rechaza. Asi se puede demostrar el camino
     * de error cuando uno quiere, en vez de depender de la suerte.
     */
    suspend fun procesar(tarjeta: DatosTarjeta, monto: Int): ResultadoPago {
        delay(DEMORA_PROCESO_MS)

        val numero = soloDigitos(tarjeta.numero)

        if (numero.endsWith("0")) {
            return ResultadoPago.Rechazado(
                "La tarjeta fue rechazada por el emisor. Intenta con otra."
            )
        }

        return ResultadoPago.Aprobado(
            codigoOrden = generarCodigoOrden(),
            ultimosCuatro = numero.takeLast(4),
            monto = monto
        )
    }

    /** Codigo que ve el comprador y que sirve para el retiro. */
    private fun generarCodigoOrden(): String {
        val letras = ('A'..'Z').toList()
        val sufijo = (1..4).map { letras.random() }.joinToString("")
        val numero = (1000..9999).random()
        return "CS-$numero-$sufijo"
    }

    // ----------------------------------------------------------------
    // Validaciones. Son funciones puras: se pueden probar sin interfaz.
    // ----------------------------------------------------------------

    fun soloDigitos(texto: String): String = texto.filter { it.isDigit() }

    /**
     * Valida el numero con el algoritmo de Luhn.
     *
     * Se recorre de derecha a izquierda duplicando uno de cada dos
     * digitos; si el doble pasa de 9 se le restan 9. Si la suma total es
     * multiplo de 10, el numero esta bien formado. Detecta casi todos los
     * errores de tipeo, que es para lo que existe.
     */
    fun numeroValido(numero: String): Boolean {
        val digitos = soloDigitos(numero)
        if (digitos.length !in 13..19) return false

        var suma = 0
        var duplicar = false

        for (i in digitos.lastIndex downTo 0) {
            var valor = digitos[i] - '0'

            if (duplicar) {
                valor *= 2
                if (valor > 9) valor -= 9
            }

            suma += valor
            duplicar = !duplicar
        }

        return suma % 10 == 0
    }

    /** Espera MM/AA y que la fecha no haya pasado. */
    fun vencimientoValido(vencimiento: String): Boolean {
        val digitos = soloDigitos(vencimiento)
        if (digitos.length != 4) return false

        val mes = digitos.substring(0, 2).toIntOrNull() ?: return false
        val anio = digitos.substring(2, 4).toIntOrNull() ?: return false

        if (mes !in 1..12) return false

        val calendario = java.util.Calendar.getInstance()
        val anioActual = calendario.get(java.util.Calendar.YEAR) % 100
        val mesActual = calendario.get(java.util.Calendar.MONTH) + 1

        return anio > anioActual || (anio == anioActual && mes >= mesActual)
    }

    fun cvvValido(cvv: String): Boolean = soloDigitos(cvv).length in 3..4

    fun nombreValido(nombre: String): Boolean = nombre.trim().length >= 5

    /** Agrupa el numero de a cuatro para que se lea mientras se escribe. */
    fun formatearNumero(numero: String): String =
        soloDigitos(numero)
            .take(19)
            .chunked(4)
            .joinToString(" ")

    /** Deja el vencimiento como MM/AA. */
    fun formatearVencimiento(texto: String): String {
        val digitos = soloDigitos(texto).take(4)
        return if (digitos.length <= 2) digitos
        else "${digitos.substring(0, 2)}/${digitos.substring(2)}"
    }

    /** Pesos chilenos con separador de miles. */
    fun formatearPesos(monto: Int): String =
        "$" + String.format(Locale.US, "%,d", monto).replace(',', '.')
}
