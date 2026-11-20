package cl.duoc.costostock

import cl.duoc.costostock.data.PasarelaPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la pasarela de pago.
 *
 * QUE SE PRUEBA: las validaciones de la tarjeta. Son funciones puras
 * -- entra un texto, sale un booleano -- y son lo unico real de la
 * pasarela: el cobro es simulado, pero el algoritmo de Luhn no.
 *
 * QUE SE MOCKEA: nada, y es a proposito. No hay dependencias que aislar:
 * estas funciones no tocan Room, ni red, ni Android. Mockear algo aca
 * seria agregar ruido sin ganar nada.
 *
 * Es la contraparte de FavoritoRepositoryTest, donde si hace falta un
 * doble porque hay una base de datos de por medio.
 */
class PasarelaPagoTest {

    // ----------------------------------------------------------------
    // Luhn: el algoritmo que usan las tarjetas reales
    // ----------------------------------------------------------------

    @Test
    fun `acepta numeros de tarjeta validos`() {
        // Numeros de prueba publicos de las marcas, todos cumplen Luhn
        assertTrue(PasarelaPago.numeroValido("4111111111111111")) // Visa
        assertTrue(PasarelaPago.numeroValido("5500000000000004")) // Mastercard
        assertTrue(PasarelaPago.numeroValido("4000000000000010"))
    }

    @Test
    fun `rechaza un numero inventado`() {
        // Mismo largo y formato, pero no cumple Luhn
        assertFalse(PasarelaPago.numeroValido("4111111111111112"))
        assertFalse(PasarelaPago.numeroValido("1234567812345678"))
    }

    @Test
    fun `un digito cambiado invalida el numero`() {
        // Es para lo que sirve Luhn: cazar errores de tipeo
        assertTrue(PasarelaPago.numeroValido("4111111111111111"))
        assertFalse(PasarelaPago.numeroValido("4111111111111121"))
    }

    @Test
    fun `rechaza numeros demasiado cortos o largos`() {
        assertFalse(PasarelaPago.numeroValido("411111111111"))        // 12
        assertFalse(PasarelaPago.numeroValido("41111111111111111111")) // 20
    }

    @Test
    fun `ignora los espacios al validar`() {
        // La pantalla dibuja el numero en grupos de cuatro; el valor
        // guardado son solo digitos, pero validar no debe depender de eso
        assertTrue(PasarelaPago.numeroValido("4111 1111 1111 1111"))
    }

    @Test
    fun `un texto vacio no es un numero valido`() {
        assertFalse(PasarelaPago.numeroValido(""))
    }

    // ----------------------------------------------------------------
    // CVV y nombre
    // ----------------------------------------------------------------

    @Test
    fun `el cvv acepta 3 o 4 digitos`() {
        assertTrue(PasarelaPago.cvvValido("123"))
        assertTrue(PasarelaPago.cvvValido("1234"))
    }

    @Test
    fun `el cvv rechaza otros largos`() {
        assertFalse(PasarelaPago.cvvValido("12"))
        assertFalse(PasarelaPago.cvvValido("12345"))
        assertFalse(PasarelaPago.cvvValido(""))
    }

    @Test
    fun `el nombre pide al menos cinco caracteres`() {
        assertTrue(PasarelaPago.nombreValido("Sofia Cortese"))
        assertFalse(PasarelaPago.nombreValido("Ana"))
    }

    @Test
    fun `el nombre no acepta solo espacios`() {
        assertFalse(PasarelaPago.nombreValido("       "))
    }

    // ----------------------------------------------------------------
    // Vencimiento
    // ----------------------------------------------------------------

    @Test
    fun `rechaza un mes que no existe`() {
        assertFalse(PasarelaPago.vencimientoValido("1330"))
        assertFalse(PasarelaPago.vencimientoValido("0030"))
    }

    @Test
    fun `rechaza una tarjeta ya vencida`() {
        assertFalse(PasarelaPago.vencimientoValido("0120"))
    }

    @Test
    fun `rechaza un vencimiento incompleto`() {
        assertFalse(PasarelaPago.vencimientoValido("12"))
        assertFalse(PasarelaPago.vencimientoValido(""))
    }

    // ----------------------------------------------------------------
    // Utilidades de formato
    // ----------------------------------------------------------------

    @Test
    fun `soloDigitos descarta todo lo que no sea numero`() {
        assertEquals("4111111111111111", PasarelaPago.soloDigitos("4111-1111 1111/1111"))
        assertEquals("", PasarelaPago.soloDigitos("abc"))
    }

    @Test
    fun `formatearPesos usa punto de miles`() {
        assertEquals("$4.730", PasarelaPago.formatearPesos(4730))
        assertEquals("$990", PasarelaPago.formatearPesos(990))
    }

    // ----------------------------------------------------------------
    // Las tarjetas de prueba
    // ----------------------------------------------------------------

    @Test
    fun `las tarjetas de prueba pasan sus propias validaciones`() {
        // Si una de estas dejara de validar, los botones de la pantalla
        // de pago quedarian inservibles para la demostracion
        listOf(PasarelaPago.TARJETA_APRUEBA, PasarelaPago.TARJETA_RECHAZA)
            .forEach { tarjeta ->
                assertTrue(
                    "El numero ${tarjeta.numero} deberia cumplir Luhn",
                    PasarelaPago.numeroValido(tarjeta.numero)
                )
                assertTrue(PasarelaPago.cvvValido(tarjeta.cvv))
                assertTrue(PasarelaPago.nombreValido(tarjeta.nombre))
                assertTrue(PasarelaPago.vencimientoValido(tarjeta.vencimiento))
            }
    }
}
