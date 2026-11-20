package cl.duoc.costostock.domain

import java.util.Locale
import kotlin.math.round

/**
 * Cuanto CO₂ se evita al rescatar un kilo de una categoría.
 *
 * Son dos componentes y no uno, porque rescatar un producto evita dos
 * cosas distintas:
 * @param produccion kg CO₂e por kg de producto, de la fase productiva
 * @param disposicion kg CO₂e por kg evitados al no ir al relleno
 * @param fuente de dónde sale el número, para poder defenderlo
 */
data class FactorCO2(
    val produccion: Double,
    val disposicion: Double,
    val fuente: String
) {
    /** Lo que se evita en total por cada kilo rescatado. */
    val total: Double
        get() = produccion + disposicion
}

/**
 * Calcula el CO₂ evitado de una oferta.
 * La tabla de factores se recibe por constructor en vez de estar fija
 * adentro. Así el día que los factores vengan de un archivo de datos o
 * de la API, se le pasan y la clase no cambia. Por defecto usa la tabla
 * de referencia de más abajo.
 */
class CalculadoraCO2(
    private val factores: Map<String, FactorCO2> = FactoresCO2.vigentes()
) {

    /** El factor de una categoría, o el genérico si no está en la tabla. */
    fun factorDe(categoria: String): FactorCO2 =
        factores[categoria] ?: FACTOR_GENERICO

    /**
     * Kilos de CO₂ evitados al rescatar [pesoKg] de [categoria].
     */

    //calculo de co2
    fun calcular(categoria: String, pesoKg: Double): Double {
        if (pesoKg <= 0 || pesoKg.isNaN()) return 0.0
        return redondear(pesoKg * factorDe(categoria).total)
    }

    fun explicacion(categoria: String, pesoKg: Double): String {
        val factor = factorDe(categoria)
        val resultado = calcular(categoria, pesoKg)
        return "${decimal(pesoKg)} kg × ${decimal(factor.total)} kg CO₂e/kg " +
            "= ${decimal(resultado)} kg CO₂e"
    }
    // El unico numero de negocio que queda en Kotlin es el generico de
    // emergencia. Los factores reales viven en assets/factores_co2.json.
    companion object {
        const val DISPOSICION_ALIMENTO = 0.6
        /**
         * El unico factor escrito en Kotlin, y es deliberado.
         *
         * Los factores reales de cada categoria viven en
         * assets/factores_co2.json. Antes estaban ademas copiados aca,
         * o sea los mismos cinco numeros en dos lugares: corregir uno y
         * olvidar el otro los dejaba contradiciendose sin que nada
         * avisara.
         *
         * Ahora el respaldo es este valor conservador y nada mas. Si la
         * app llega a usarlo es porque el archivo falta o esta mal
         * escrito, y ahi conviene quedarse corto antes que inflar el
         * impacto ambiental que la app declara.
         */
        val FACTOR_GENERICO = FactorCO2(
            produccion = 1.0,
            disposicion = DISPOSICION_ALIMENTO,
            fuente = "Valor conservador para categorías sin factor propio"
        )

        /** Un decimal: más precisión que esa sería fingir exactitud. */
        private fun redondear(valor: Double): Double = round(valor * 10) / 10.0

        /** Coma decimal, como se escribe en Chile. */
        private fun decimal(valor: Double): String =
            String.format(Locale.forLanguageTag("es-CL"), "%.1f", valor)
    }
}

/**
 * esto es la tabla de factores que está usando la app.
 * Existe para que el calculo use los factores del archivo de datos sin
 * que cada pantalla tenga que ir a buscarlos. La app los carga una vez
 * al arrancar con [usar] y desde ahí cualquier CalculadoraCO2 creada sin
 * argumentos los toma.
 *
 * Arranca vacia: los numeros no estan en el codigo, estan en el
 * archivo. Mientras nadie la cargue, factorDe() devuelve el generico.
 * Los tests no dependen de esto porque arman su propia tabla, que es
 * como debe ser: una prueba no deberia depender de los datos de
 * produccion.
 */
object FactoresCO2 {

    @Volatile
    private var tabla: Map<String, FactorCO2> = emptyMap()

    fun vigentes(): Map<String, FactorCO2> = tabla

    /**
     * Las categorias que existen en la app.
     *
     * Salen de las llaves de la tabla de factores, no de una lista
     * aparte. Antes habia dos listas escritas a mano, en
     * OfertaRepository y en PublicarOfertaViewModel, con contenidos
     * distintos: se podian desincronizar sin que nada avisara.
     *
     * Derivarlas de los factores hace imposible agregar una categoria
     * sin darle su factor de CO2, que es lo que antes pasaba en
     * silencio aplicando el generico.
     */
    fun categorias(): List<String> = tabla.keys.toList()

    /**
     * Reemplaza la tabla por la que viene del archivo de datos.
     *
     * Una tabla vacia se ignora: si el archivo falta o esta mal escrito
     * conviene dejar la que ya habia, en vez de borrar la buena y
     * quedarse sin ninguna.
     */
    fun usar(nuevos: Map<String, FactorCO2>) {
        if (nuevos.isNotEmpty()) tabla = nuevos
    }
}
