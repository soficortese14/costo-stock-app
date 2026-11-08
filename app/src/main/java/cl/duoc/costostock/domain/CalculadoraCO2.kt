package cl.duoc.costostock.domain

import java.util.Locale
import kotlin.math.round

/**
 * Cuánto CO₂ se evita al rescatar un kilo de una categoría.
 *
 * Son dos componentes y no uno, porque rescatar un producto evita dos
 * cosas distintas:
 *
 *  - **producción**: las emisiones que ya se gastaron en producir ese
 *    alimento y que se perderían si termina en la basura. Es el número
 *    grande y el que varía por categoría.
 *
 *  - **disposición**: el metano que ese alimento habría generado al
 *    descomponerse en un relleno sanitario. No depende de qué producto
 *    sea, sino de que sea orgánico.
 *
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
 *
 * Es Kotlin puro, sin nada de Android: por eso se puede probar con
 * JUnit normal y no necesita emulador. Antes esta fórmula vivía dentro
 * de PublicarOfertaViewModel, donde no se podía probar ni reutilizar, y
 * los factores eran un mapa privado sin ninguna fuente citada.
 *
 * La tabla de factores se recibe por constructor en vez de estar fija
 * adentro. Así el día que los factores vengan de un archivo de datos o
 * de la API, se le pasan y la clase no cambia. Por defecto usa la tabla
 * de referencia de más abajo.
 */
class CalculadoraCO2(
    private val factores: Map<String, FactorCO2> = FACTORES_REFERENCIA
) {

    /** El factor de una categoría, o el genérico si no está en la tabla. */
    fun factorDe(categoria: String): FactorCO2 =
        factores[categoria] ?: FACTOR_GENERICO

    /**
     * Kilos de CO₂ evitados al rescatar [pesoKg] de [categoria].
     *
     * Un peso que no tiene sentido devuelve 0 y no un número negativo:
     * la pantalla muestra este valor como un logro ambiental y un
     * "-2,4 kg evitados" no significa nada.
     */
    fun calcular(categoria: String, pesoKg: Double): Double {
        if (pesoKg <= 0 || pesoKg.isNaN()) return 0.0
        return redondear(pesoKg * factorDe(categoria).total)
    }

    /**
     * La misma cuenta, escrita para que se vea en pantalla.
     *
     * Ejemplo: "2,0 kg × 3,8 kg CO₂e/kg = 7,6 kg CO₂e"
     *
     * Existe porque el número solo no se explica: mostrando la
     * multiplicación queda claro que sale de un cálculo y no de un
     * valor escrito a mano.
     */
    fun explicacion(categoria: String, pesoKg: Double): String {
        val factor = factorDe(categoria)
        val resultado = calcular(categoria, pesoKg)
        return "${decimal(pesoKg)} kg × ${decimal(factor.total)} kg CO₂e/kg " +
            "= ${decimal(resultado)} kg CO₂e"
    }

    companion object {

        /**
         * Metano evitado al no mandar un kilo de alimento al relleno
         * sanitario.
         *
         * 0,6 kg CO₂e/kg, de la metodología de ReFED y Quantis (2020),
         * coherente con el modelo WARM de la EPA de Estados Unidos.
         *
         * Solo aplica a alimentos: un detergente no genera metano al
         * descomponerse, por eso la categoría Hogar lleva 0.
         */
        const val DISPOSICION_ALIMENTO = 0.6

        /**
         * Factores de referencia por categoría.
         *
         * Los de alimentos salen de Poore & Nemecek (2018), el
         * metaanálisis publicado en Science sobre ~38.700 granjas en
         * 119 países, que es la fuente estándar de huella de carbono de
         * alimentos. Cada categoría de la app agrupa varios productos,
         * así que se toma el representativo que más pesa en ella.
         */
        val FACTORES_REFERENCIA: Map<String, FactorCO2> = mapOf(
            "Abarrotes" to FactorCO2(
                produccion = 1.4,
                disposicion = DISPOSICION_ALIMENTO,
                fuente = "Poore & Nemecek 2018: trigo y centeno 1,57 · " +
                    "otras verduras 0,53 · papas 0,46"
            ),
            "Refrigerados" to FactorCO2(
                produccion = 3.2,
                disposicion = DISPOSICION_ALIMENTO,
                fuente = "Poore & Nemecek 2018: leche 3,15 · huevos 4,67"
            ),
            "Bebidas" to FactorCO2(
                produccion = 2.1,
                disposicion = DISPOSICION_ALIMENTO,
                fuente = "LCA de bebidas envasadas, ~2,1 kg CO₂e por litro " +
                    "incluyendo el envase"
            ),
            "Licores" to FactorCO2(
                produccion = 1.8,
                disposicion = DISPOSICION_ALIMENTO,
                fuente = "Poore & Nemecek 2018: vino 1,79"
            ),
            "Hogar" to FactorCO2(
                produccion = 2.8,
                // Un detergente no se descompone generando metano:
                // rescatarlo evita producir uno nuevo, nada más.
                disposicion = 0.0,
                fuente = "LCA de surfactantes LAS, ~2,8 kg CO₂e/kg. " +
                    "Es el factor menos firme de la tabla: no es alimento " +
                    "y no está cubierto por Poore & Nemecek"
            )
        )

        /**
         * Para una categoría que no esté en la tabla.
         *
         * Se usa el valor más bajo de los alimentos y no un promedio: si
         * no sabemos qué es, conviene quedarse corto antes que inflar el
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
