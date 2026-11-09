package cl.duoc.costostock

import cl.duoc.costostock.domain.CalculadoraCO2
import cl.duoc.costostock.domain.FactorCO2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del cálculo de CO₂ evitado.
 *
 * Se pueden correr sin emulador porque CalculadoraCO2 es Kotlin puro:
 * ese fue justamente el motivo de sacarla del ViewModel.
 *
 * DELTA es la tolerancia al comparar Double. Comparar decimales con
 * igualdad exacta falla por el redondeo binario de la IEEE 754, así que
 * JUnit obliga a declarar cuánta diferencia se acepta.
 */
class CalculadoraCO2Test {

    private val calculadora = CalculadoraCO2()

    companion object {
        private const val DELTA = 0.001
    }

    // ----------------------------------------------------------------
    // El cálculo
    // ----------------------------------------------------------------

    @Test
    fun `suma produccion y disposicion, no solo produccion`() {
        // Refrigerados: 3,2 de produccion + 0,6 de disposicion = 3,8
        // 2 kg x 3,8 = 7,6
        assertEquals(7.6, calculadora.calcular("Refrigerados", 2.0), DELTA)
    }

    @Test
    fun `calcula Abarrotes con su factor`() {
        // 1,4 + 0,6 = 2,0 ; 3 kg x 2,0 = 6,0
        assertEquals(6.0, calculadora.calcular("Abarrotes", 3.0), DELTA)
    }

    @Test
    fun `Hogar no suma disposicion porque no genera metano`() {
        // 2,8 + 0,0 = 2,8 ; 1 kg x 2,8 = 2,8
        assertEquals(2.8, calculadora.calcular("Hogar", 1.0), DELTA)
        assertEquals(0.0, calculadora.factorDe("Hogar").disposicion, DELTA)
    }

    @Test
    fun `el resultado se redondea a un decimal`() {
        // Licores: 1,8 + 0,6 = 2,4 ; 1,37 kg x 2,4 = 3,288 -> 3,3
        assertEquals(3.3, calculadora.calcular("Licores", 1.37), DELTA)
    }

    @Test
    fun `redondea hacia arriba y no trunca`() {
        // Bebidas: 2,1 + 0,6 = 2,7 ; 0,99 kg x 2,7 = 2,673
        // Truncando daria 2,6 ; redondeando da 2,7
        assertEquals(2.7, calculadora.calcular("Bebidas", 0.99), DELTA)
    }

    // ----------------------------------------------------------------
    // Entradas que no sirven
    // ----------------------------------------------------------------

    @Test
    fun `peso cero da cero`() {
        assertEquals(0.0, calculadora.calcular("Abarrotes", 0.0), DELTA)
    }

    @Test
    fun `peso negativo da cero y no un impacto negativo`() {
        assertEquals(0.0, calculadora.calcular("Abarrotes", -5.0), DELTA)
    }

    @Test
    fun `peso no numerico da cero`() {
        assertEquals(0.0, calculadora.calcular("Abarrotes", Double.NaN), DELTA)
    }

    // ----------------------------------------------------------------
    // Categorias
    // ----------------------------------------------------------------

    @Test
    fun `una categoria desconocida usa el factor generico`() {
        val desconocida = calculadora.factorDe("Mascotas")
        assertEquals(CalculadoraCO2.FACTOR_GENERICO, desconocida)
    }

    @Test
    fun `el factor generico es conservador frente a los conocidos`() {
        val generico = CalculadoraCO2.FACTOR_GENERICO.total
        val refrigerados = calculadora.factorDe("Refrigerados").total
        assertTrue(
            "El generico deberia quedarse corto, no inflar el impacto",
            generico < refrigerados
        )
    }

    @Test
    fun `todas las categorias de la app tienen factor propio`() {
        val categorias = listOf(
            "Abarrotes", "Refrigerados", "Bebidas", "Licores", "Hogar"
        )
        categorias.forEach { categoria ->
            assertNotEquals(
                "Falta el factor de $categoria",
                CalculadoraCO2.FACTOR_GENERICO,
                calculadora.factorDe(categoria)
            )
        }
    }

    @Test
    fun `cada factor declara de donde sale`() {
        CalculadoraCO2.FACTORES_REFERENCIA.forEach { (categoria, factor) ->
            assertTrue(
                "El factor de $categoria no cita su fuente",
                factor.fuente.isNotBlank()
            )
        }
    }

    // ----------------------------------------------------------------
    // La tabla se puede reemplazar
    // ----------------------------------------------------------------

    @Test
    fun `acepta una tabla de factores distinta a la de referencia`() {
        // Esto es lo que va a pasar cuando los factores vengan de un
        // archivo de datos en vez de estar en el codigo.
        val propia = CalculadoraCO2(
            mapOf(
                "Abarrotes" to FactorCO2(
                    produccion = 10.0,
                    disposicion = 0.0,
                    fuente = "tabla de prueba"
                )
            )
        )
        assertEquals(20.0, propia.calcular("Abarrotes", 2.0), DELTA)
    }

    // ----------------------------------------------------------------
    // La explicacion que ve el usuario
    // ----------------------------------------------------------------

    @Test
    fun `la explicacion muestra la multiplicacion completa`() {
        val texto = calculadora.explicacion("Refrigerados", 2.0)
        assertEquals("2,0 kg × 3,8 kg CO₂e/kg = 7,6 kg CO₂e", texto)
    }

    @Test
    fun `la explicacion usa coma decimal`() {
        val texto = calculadora.explicacion("Abarrotes", 1.5)
        assertTrue("Deberia escribirse a la chilena, con coma", texto.contains(","))
    }
}
