package cl.duoc.costostock.data

import android.content.Context
import android.util.Log
import cl.duoc.costostock.data.room.OfertaEntity
import cl.duoc.costostock.domain.CalculadoraCO2
import cl.duoc.costostock.domain.FactorCO2
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException

// --------------------------------------------------------------------
// DTO: el JSON tal como viene, antes de convertirlo
// --------------------------------------------------------------------

/**
 * Una oferta tal como está escrita en catalogo.json.
 *
 * Es una clase aparte de OfertaEntity a propósito. El archivo y la
 * tabla no tienen por qué parecerse: el archivo trae los minutos que
 * dura la oferta, la tabla guarda el instante en que termina; el
 * archivo trae el nombre de la imagen, la tabla guarda la URI completa.
 *
 * Esa separación es la misma que se necesita cuando los datos vengan de
 * una API: lo que llega nunca calza exacto con lo que se guarda, y el
 * traductor vive en un solo lugar en vez de repartido por la app.
 */
private data class OfertaJson(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precioOriginal: Int = 0,
    val precioOferta: Int = 0,
    val stockActual: Int = 0,
    val minutosVigencia: Long = 0,
    val pesoKg: Double = 0.0,
    val comercio: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val imagen: String = ""
)

private data class CatalogoJsonDto(
    val version: Int = 0,
    val ofertas: List<OfertaJson> = emptyList()
)

private data class FactorJson(
    val categoria: String = "",
    val produccion: Double = 0.0,
    val disposicion: Double = 0.0,
    val fuente: String = ""
)

private data class FactoresJsonDto(
    val version: Int = 0,
    val factores: List<FactorJson> = emptyList()
)

// --------------------------------------------------------------------
// El cargador
// --------------------------------------------------------------------

/**
 * Lee el catálogo y la tabla de factores desde archivos de datos.
 *
 * Antes las ocho ofertas estaban escritas como literales de Kotlin
 * dentro de OfertasDePrueba.kt, y los factores de CO₂ eran un mapa
 * privado adentro de un ViewModel. Las dos cosas eran código, no datos:
 * para cambiar un precio había que recompilar la app.
 *
 * Ahora viven en assets/catalogo.json y assets/factores_co2.json, y
 * esta clase los traduce a objetos del dominio. El camino que recorre
 * un dato es el mismo que recorrería viniendo de la API — archivo o
 * red, JSON, DTO, mapper, entidad — así que cuando exista el backend
 * solo cambia de dónde sale el texto: lo demás ya está escrito.
 */
class CatalogoJson(private val context: Context) {

    private val gson = Gson()

    /**
     * Las ofertas del archivo, listas para insertar en Room.
     *
     * Si el archivo falta o está mal escrito devuelve una lista vacía y
     * lo deja en el log, en vez de tumbar la app al arrancar: un
     * catálogo vacío se ve feo, una app que no abre no se ve.
     */
    fun cargarOfertas(ahora: Long = System.currentTimeMillis()): List<OfertaEntity> {
        val dto = leer(ARCHIVO_CATALOGO, CatalogoJsonDto::class.java) ?: return emptyList()
        return dto.ofertas.map { it.aEntidad(ahora) }
    }

    /**
     * La tabla de factores del archivo.
     *
     * Si no se puede leer, devuelve la tabla de referencia que trae
     * CalculadoraCO2 en el código. El cálculo de CO₂ nunca queda sin
     * factores: es preferible usar los de respaldo a mostrar cero.
     */
    fun cargarFactores(): Map<String, FactorCO2> {
        val dto = leer(ARCHIVO_FACTORES, FactoresJsonDto::class.java)
            ?: return CalculadoraCO2.FACTORES_REFERENCIA

        if (dto.factores.isEmpty()) return CalculadoraCO2.FACTORES_REFERENCIA

        return dto.factores.associate { factor ->
            factor.categoria to FactorCO2(
                produccion = factor.produccion,
                disposicion = factor.disposicion,
                fuente = factor.fuente
            )
        }
    }

    /** Una calculadora que usa los factores del archivo. */
    fun calculadora(): CalculadoraCO2 = CalculadoraCO2(cargarFactores())

    /**
     * Abre un archivo de assets y lo convierte al tipo pedido.
     *
     * Se atrapan los dos errores que de verdad pueden pasar y se
     * distinguen, porque no se arreglan igual: IOException es que el
     * archivo no está, JsonSyntaxException es que está pero mal escrito.
     */
    private fun <T> leer(nombre: String, tipo: Class<T>): T? =
        try {
            context.assets.open(nombre).bufferedReader().use { lector ->
                gson.fromJson(lector, tipo)
            }
        } catch (e: IOException) {
            Log.e(TAG, "No se pudo abrir $nombre en assets", e)
            null
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "$nombre tiene un error de formato", e)
            null
        }

    /**
     * Del JSON a la fila de Room.
     *
     * Acá pasan las tres traducciones:
     *
     *  - los minutos de vigencia se convierten en el instante en que
     *    termina, contado desde ahora, para que los contadores siempre
     *    estén corriendo al abrir la app. Un valor negativo deja la
     *    oferta ya expirada, que es lo que se busca para demostrar la
     *    regla del cliente sin esperar.
     *
     *  - el nombre de la imagen se convierte en URI de assets. El campo
     *    sigue siendo una URI: el día que un proveedor suba su foto,
     *    ahí también va a haber una URI y la pantalla no cambia.
     *
     *  - el id queda en 0 para que Room genere uno nuevo.
     */
    private fun OfertaJson.aEntidad(ahora: Long) = OfertaEntity(
        id = 0,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        precioOriginal = precioOriginal,
        precioOferta = precioOferta,
        stockActual = stockActual,
        finVigencia = ahora + minutosVigencia * MINUTO,
        comercio = comercio,
        direccion = direccion,
        comuna = comuna,
        latitud = latitud,
        longitud = longitud,
        pesoKg = pesoKg,
        imagenUrl = "$ASSETS/$imagen"
    )

    private companion object {
        const val TAG = "CatalogoJson"
        const val ARCHIVO_CATALOGO = "catalogo.json"
        const val ARCHIVO_FACTORES = "factores_co2.json"
        const val MINUTO = 60_000L
        const val ASSETS = "file:///android_asset"
    }
}
