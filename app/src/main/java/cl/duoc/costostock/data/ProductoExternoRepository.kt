package cl.duoc.costostock.data

import cl.duoc.costostock.data.remoto.ClienteRetrofit
import cl.duoc.costostock.data.remoto.OpenFoodFactsApi
import cl.duoc.costostock.data.remoto.ProductoJson
import java.io.IOException

/**
 * Un producto encontrado en la API externa, ya traducido al lenguaje
 * de la app.
 *
 * No se usa ProductoJson directamente fuera de la capa de red a
 * proposito: ese objeto tiene la forma que decidio Open Food Facts, con
 * todo nullable y los pesos en gramos. Si llegara hasta las pantallas,
 * cambiar de API obligaria a tocar la interfaz. Aca se traduce una
 * sola vez.
 */
data class ProductoExterno(
    val codigo: String,
    val nombre: String,
    val marca: String,
    /** Ya convertido a kilos, que es la unidad que usa la app. */
    val pesoKg: Double?,
    val imagenUrl: String?,
    /** Nota ambiental en mayuscula: A, B, C, D o E. */
    val notaAmbiental: String?
) {
    /** Titulo sugerido para el formulario: marca y producto juntos. */
    val tituloSugerido: String
        get() = listOf(marca, nombre)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
}

/**
 * En que termino la consulta a la API.
 *
 * sealed class para que el when de la pantalla sea exhaustivo, igual
 * que EstadoUbicacion, EstadoPago y EstadoVouchers.
 *
 * Los tres casos de fallo estan separados porque el usuario tiene que
 * hacer cosas distintas en cada uno: si no hay internet espera, si el
 * codigo no existe lo escribe de nuevo o carga a mano, y si la API
 * fallo no puede hacer nada mas que reintentar.
 */
sealed class ResultadoBusqueda {
    data class Encontrado(val producto: ProductoExterno) : ResultadoBusqueda()
    object NoEncontrado : ResultadoBusqueda()
    object SinConexion : ResultadoBusqueda()
    data class Error(val motivo: String) : ResultadoBusqueda()
}

/**
 * Consulta productos en la API externa.
 *
 * Es la capa que separa la red del resto de la app: las pantallas no
 * saben que existe Retrofit ni Open Food Facts, solo piden un producto
 * por codigo y reciben un ResultadoBusqueda.
 *
 * La API se recibe por constructor para poder pasarle un doble en las
 * pruebas, igual que FavoritoRepository con su DAO.
 */
class ProductoExternoRepository(
    private val api: OpenFoodFactsApi = ClienteRetrofit.openFoodFacts
) {

    /**
     * Busca un producto por su codigo de barras.
     *
     * Nunca lanza excepciones: cualquier fallo se traduce a un caso de
     * ResultadoBusqueda. Una excepcion escapando de aca tumbaria la
     * pantalla de publicar justo cuando el proveedor esta escribiendo.
     */
    suspend fun buscarPorCodigo(codigo: String): ResultadoBusqueda {
        val limpio = codigo.filter { it.isDigit() }

        /*
         * Un codigo de barras tiene entre 8 y 14 digitos: EAN-8, UPC-A
         * de 12, EAN-13 y GTIN-14. Se descarta antes de salir a la red
         * para no gastar una llamada que ya sabemos que va a fallar.
         */
        if (limpio.length !in 8..14) return ResultadoBusqueda.NoEncontrado

        return try {
            val respuesta = api.buscarPorCodigo(limpio)

            /*
             * La API responde HTTP 200 aunque el producto no exista: el
             * aviso viene en el campo status, no en el codigo HTTP. Por
             * eso no alcanza con que la llamada no lance excepcion.
             */
            val producto = respuesta.producto
            if (respuesta.estado != ESTADO_ENCONTRADO || producto == null) {
                ResultadoBusqueda.NoEncontrado
            } else {
                ResultadoBusqueda.Encontrado(producto.aDominio(limpio))
            }

        } catch (e: IOException) {
            // Sin internet, DNS caido o se acabo el tope de tiempo
            ResultadoBusqueda.SinConexion
        } catch (e: Exception) {
            // La API respondio algo que no se pudo interpretar
            ResultadoBusqueda.Error(e.message ?: "No se pudo consultar el producto")
        }
    }

    /**
     * Del JSON de la API al objeto de la app.
     *
     * Pasan tres traducciones:
     *
     *  - gramos a kilos, que es la unidad del formulario y de
     *    CalculadoraCO2
     *  - los nulos se vuelven cadenas vacias, para que la pantalla no
     *    tenga que preguntar por null en cada campo
     *  - la nota ambiental se pasa a mayuscula: la API la entrega en
     *    minuscula y se muestra como letra grande
     *
     * Un peso en cero o negativo se descarta: hay productos cargados
     * con 0 g, y un peso cero daria 0 kg de CO2 evitado, que es peor
     * que no mostrar nada.
     */
    private fun ProductoJson.aDominio(codigoConsultado: String) = ProductoExterno(
        codigo = codigo ?: codigoConsultado,
        nombre = nombre?.trim().orEmpty(),
        marca = marcas?.split(",")?.firstOrNull()?.trim().orEmpty(),
        pesoKg = pesoGramos
            ?.takeIf { it > 0 }
            ?.let { gramos -> (gramos / 1000.0 * 100).toInt() / 100.0 },
        imagenUrl = imagenUrl?.takeIf { it.isNotBlank() },
        notaAmbiental = notaAmbiental
            ?.takeIf { it.isNotBlank() && it.length == 1 }
            ?.uppercase()
    )

    private companion object {
        /** La API marca con 1 que el producto existe, y con 0 que no. */
        const val ESTADO_ENCONTRADO = 1
    }
}
