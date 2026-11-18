package cl.duoc.costostock

import cl.duoc.costostock.domain.CalculadoraCO2
import cl.duoc.costostock.domain.FactorCO2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del calculo de CO2 evitado.
 *
 * Corren sin emulador porque CalculadoraCO2 es Kotlin puro: ese fue el
 * motivo de sacarla del ViewModel.
 *
 * Las pruebas arman su PROPIA tabla de factores y no usan la de la app.
 * Es a proposito: un test que dependiera de assets/factores_co2.json se
 * rompería cada vez que el cliente corrige un factor, aunque la formula
 * siga perfecta. Lo que se prueba aca es la cuenta, no los datos.
 *
 * DELTA es la tolerancia al comparar Double. Comparar decimales con
 * igualdad exacta falla por el redondeo binario de la IEEE 754.
 */
class CalculadoraCO2Test {

    companion object {
        private const val DELTA = 0.001

        /** Tabla de prueba, con numeros redondos elegidos para la cuenta. */
        private val TABLA = mapOf(
            "Alimento" to FactorCO2(
                produccion = 3.2,
                disposicion = 0.6,   // total 3,8
                fuente = "tabla de prueba"
            ),
            "NoAlimento" to FactorCO2(
                produccion = 2.8,
                disposicion = 0.0,   // total 2,8
                fuente = "tabla de prueba"
            )
        )
    }

    private val calculadora = CalculadoraCO2(TABLA)

    // ----------------------------------------------------------------
    // El calculo
    // ----------------------------------------------------------------

    @Test
    fun `suma produccion y disposicion, no solo produccion`() {
        // 3,2 + 0,6 = 3,8 ; 2 kg x 3,8 = 7,6
        assertEquals(7.6, calculadora.calcular("Alimento", 2.0), DELTA)
    }

    @Test
    fun `lo que no es alimento no suma disposicion`() {
        // Un detergente no genera metano al descomponerse
        assertEquals(2.8, calculadora.calcular("NoAlimento", 1.0), DELTA)
        assertEquals(0.0, calculadora.factorDe("NoAlimento").disposicion, DELTA)
    }

    @Test
    fun `el resultado se redondea a un decimal`() {
        // 1,37 kg x 3,8 = 5,206 -> 5,2
        assertEquals(5.2, calculadora.calcular("Alimento", 1.37), DELTA)
    }

    @Test
    fun `redondea hacia arriba y no trunca`() {
        // 0,99 kg x 2,8 = 2,772 ; truncando daria 2,7 y redondeando 2,8
        assertEquals(2.8, calculadora.calcular("NoAlimento", 0.99), DELTA)
    }

    // ----------------------------------------------------------------
    // Entradas que no sirven
    // ----------------------------------------------------------------

    @Test
    fun `peso cero da cero`() {
        assertEquals(0.0, calculadora.calcular("Alimento", 0.0), DELTA)
    }

    @Test
    fun `peso negativo da cero y no un impacto negativo`() {
        assertEquals(0.0, calculadora.calcular("Alimento", -5.0), DELTA)
    }

    @Test
    fun `peso no numerico da cero`() {
        assertEquals(0.0, calculadora.calcular("Alimento", Double.NaN), DELTA)
    }

    // ----------------------------------------------------------------
    // Categorias desconocidas
    // ----------------------------------------------------------------

    @Test
    fun `una categoria que no esta en la tabla usa el generico`() {
        assertEquals(
            CalculadoraCO2.FACTOR_GENERICO,
            calculadora.factorDe("Mascotas")
        )
    }

    @Test
    fun `el generico es conservador frente a un factor real`() {
        assertTrue(
            "El generico deberia quedarse corto, no inflar el impacto",
            CalculadoraCO2.FACTOR_GENERICO.total < calculadora.factorDe("Alimento").total
        )
    }

    @Test
    fun `una tabla vacia no rompe el calculo`() {
        // Pasa si el archivo de factores falta o esta mal escrito:
        // la app tiene que seguir mostrando un numero razonable.
        val sinTabla = CalculadoraCO2(emptyMap())
        val esperado = CalculadoraCO2.FACTOR_GENERICO.total * 2
        assertEquals(esperado, sinTabla.calcular("Alimento", 2.0), DELTA)
    }

    // ----------------------------------------------------------------
    // La tabla se puede reemplazar
    // ----------------------------------------------------------------

    @Test
    fun `acepta cualquier tabla de factores`() {
        // Esto es lo que ocurre en la app: los factores vienen del
        // archivo de datos, no del codigo.
        val propia = CalculadoraCO2(
            mapOf(
                "Alimento" to FactorCO2(
                    produccion = 10.0,
                    disposicion = 0.0,
                    fuente = "otra tabla"
                )
            )
        )
        assertEquals(20.0, propia.calcular("Alimento", 2.0), DELTA)
    }

    @Test
    fun `cada factor declara de donde sale`() {
        TABLA.forEach { (categoria, factor) ->
            assertTrue(
                "El factor de $categoria no cita su fuente",
                factor.fuente.isNotBlank()
            )
        }
    }

    // ----------------------------------------------------------------
    // La explicacion que ve el usuario
    // ----------------------------------------------------------------

    @Test
    fun `la explicacion muestra la multiplicacion completa`() {
        val texto = calculadora.explicacion("Alimento", 2.0)
        assertEquals("2,0 kg × 3,8 kg CO₂e/kg = 7,6 kg CO₂e", texto)
    }

    @Test
    fun `la explicacion usa coma decimal`() {
        val texto = calculadora.explicacion("Alimento", 1.5)
        assertTrue("Deberia escribirse a la chilena, con coma", texto.contains(","))
    }
}
