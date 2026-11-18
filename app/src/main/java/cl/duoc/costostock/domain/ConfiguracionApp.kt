package cl.duoc.costostock.domain

/** Una comuna de cobertura, con el punto que la representa en el mapa. */
data class Comuna(
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)

/**
 * Los parámetros que definió el cliente.
 *
 * Son datos de negocio, no decisiones técnicas: la comisión cambia por
 * volumen de ventas, las comunas dependen de dónde opere Costo Stock y
 * el radio lo fijó el cliente en la reunión. Por eso viven en
 * assets/config.json y no escritos en Kotlin.
 *
 * El criterio para decidir qué va acá y qué se queda en el código: si
 * lo puede cambiar el cliente, es dato. Si lo decide quien programa, es
 * constante. El radio de la Tierra no es negociable con Costo Stock;
 * el radio de búsqueda sí.
 */
data class ConfiguracionApp(
    val comisionPorcentaje: Int,
    val radioMinimoKm: Int,
    val radioMaximoKm: Int,
    val radioPorDefectoKm: Int,
    val comunas: List<Comuna>
) {
    /** Solo los nombres, que es lo que piden los selectores de la interfaz. */
    val nombresDeComunas: List<String>
        get() = comunas.map { it.nombre }

    /**
     * Las coordenadas de una comuna, o las de Viña del Mar si no está.
     *
     * Viña es el respaldo porque es donde opera el cliente y donde se
     * centra el mapa: una oferta mal ubicada ahí se nota, mientras que
     * en el (0,0) del Atlántico pasaría desapercibida.
     */
    fun coordenadasDe(nombre: String): Pair<Double, Double> {
        val comuna = comunas.find { it.nombre == nombre } ?: comunas.firstOrNull()
        return (comuna?.latitud ?: -33.0245) to (comuna?.longitud ?: -71.5518)
    }

    companion object {
        /**
         * Lo mínimo para que la app funcione si no se puede leer el
         * archivo.
         *
         * No es una copia de config.json: no repite las comunas ni los
         * valores del cliente, justamente para que no haya dos fuentes
         * que se puedan contradecir. Es un mínimo viable, y si la app
         * llega a usarlo es porque algo se rompió.
         */
        val MINIMA = ConfiguracionApp(
            comisionPorcentaje = 0,
            radioMinimoKm = 1,
            radioMaximoKm = 30,
            radioPorDefectoKm = 15,
            comunas = emptyList()
        )
    }
}

/**
 * La configuración que está usando la app.
 *
 * Mismo patrón que FactoresCO2: se carga una vez al arrancar y desde
 * ahí la lee quien la necesite, sin que cada pantalla tenga que ir al
 * archivo.
 */
object Configuracion {

    @Volatile
    private var actual: ConfiguracionApp = ConfiguracionApp.MINIMA

    fun vigente(): ConfiguracionApp = actual

    fun usar(nueva: ConfiguracionApp) {
        actual = nueva
    }
}
