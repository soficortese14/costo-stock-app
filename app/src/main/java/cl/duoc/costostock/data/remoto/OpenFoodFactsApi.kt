package cl.duoc.costostock.data.remoto

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --------------------------------------------------------------------
// DTO: el JSON tal como lo devuelve Open Food Facts
// --------------------------------------------------------------------

/**
 * Un producto, tal como viene de la API.
 *
 * Todos los campos son nullable y con valor por defecto a proposito:
 * Open Food Facts es una base colaborativa y los datos son irregulares.
 * Hay productos sin peso, sin foto o sin nota ambiental, porque nadie
 * los completo todavia. Si estos campos no fueran nullable, Gson los
 * dejaria en null de todos modos y la app reventaria al leerlos.
 *
 * @SerializedName mapea el nombre del JSON al nombre Kotlin: la API usa
 * snake_case y el proyecto camelCase.
 */
data class ProductoJson(
    @SerializedName("code") val codigo: String? = null,
    @SerializedName("product_name") val nombre: String? = null,
    @SerializedName("brands") val marcas: String? = null,

    /** Texto libre: "1 L", "400 g e". Para mostrar, no para calcular. */
    @SerializedName("quantity") val cantidadTexto: String? = null,

    /**
     * Peso en GRAMOS, ya normalizado por la API.
     *
     * Este es el campo que importa: es el que alimenta el calculo de
     * CO2. Viene como numero, a diferencia de quantity que es texto.
     */
    @SerializedName("product_quantity") val pesoGramos: Double? = null,

    @SerializedName("image_url") val imagenUrl: String? = null,

    /** Nota ambiental de la A a la E, minuscula. */
    @SerializedName("ecoscore_grade") val notaAmbiental: String? = null,

    @SerializedName("categories") val categorias: String? = null
)

/**
 * La respuesta de una consulta por codigo de barras.
 *
 * status vale 1 si el producto existe y 0 si no. La API responde
 * HTTP 200 en los dos casos, asi que "no encontrado" NO es un error de
 * red: hay que mirar este campo.
 */
data class RespuestaProducto(
    @SerializedName("status") val estado: Int = 0,
    @SerializedName("status_verbose") val detalle: String? = null,
    @SerializedName("product") val producto: ProductoJson? = null
)

/** La respuesta de una busqueda. */
data class RespuestaBusqueda(
    @SerializedName("count") val total: Int = 0,
    @SerializedName("products") val productos: List<ProductoJson> = emptyList()
)

// --------------------------------------------------------------------
// El contrato de la API
// --------------------------------------------------------------------

/**
 * Open Food Facts: base de datos colaborativa de productos
 * alimenticios, con ~8.000 productos chilenos.
 *
 * Es la API externa del proyecto. Se eligio esta y no otra porque
 * entrega el PESO del producto, que es justo lo que necesita
 * CalculadoraCO2: hasta ahora el proveedor escribia ese peso a mano al
 * publicar, y de un numero inventado salia el CO2 evitado.
 *
 * No necesita clave ni cuenta: es un proyecto de datos abiertos. Eso
 * tambien evita tener que esconder un secreto dentro del APK, que se
 * puede abrir.
 *
 * Retrofit convierte esta interfaz en una implementacion real: cada
 * anotacion describe una URL, y la funcion suspend hace que la llamada
 * corra fuera del hilo principal sin escribir nada mas.
 */
interface OpenFoodFactsApi {

    /**
     * Busca un producto por su codigo de barras.
     *
     * fields limita lo que devuelve la API: sin esto llegan cientos de
     * campos -- ingredientes, aditivos, aportes nutricionales -- y la
     * respuesta pesa cientos de kilobytes por producto.
     */
    @GET("api/v2/product/{codigo}.json")
    suspend fun buscarPorCodigo(
        @Path("codigo") codigo: String,
        @Query("fields") campos: String = CAMPOS
    ): RespuestaProducto

    /** Busca productos por texto, para cuando no se tiene el codigo. */
    @GET("api/v2/search")
    suspend fun buscarPorNombre(
        @Query("categories_tags_es") categoria: String,
        @Query("fields") campos: String = CAMPOS,
        @Query("page_size") cuantos: Int = 10
    ): RespuestaBusqueda

    companion object {
        /**
         * Subdominio chileno: prioriza los productos del pais.
         *
         * Es coherente con el caso, que define cobertura en la V Region.
         */
        const val URL_BASE = "https://cl.openfoodfacts.org/"

        /** Solo los campos que la app usa. */
        const val CAMPOS =
            "code,product_name,brands,quantity,product_quantity," +
                "image_url,ecoscore_grade,categories"
    }
}
