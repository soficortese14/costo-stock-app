package cl.duoc.costostock.data.remoto

import cl.duoc.costostock.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye el cliente de red.
 *
 * Es un object y no una clase porque crear un Retrofit es caro: arma su
 * propio pool de conexiones y de hilos. Una sola instancia para toda la
 * app los reutiliza, en vez de abrir conexiones nuevas en cada pantalla.
 *
 * by lazy: no se construye al arrancar la app, sino la primera vez que
 * alguien pide la API. Si el usuario nunca publica una oferta, nunca se
 * crea.
 */
object ClienteRetrofit {

    /**
     * Topes de tiempo.
     *
     * Sin ellos, una red lenta deja la pantalla esperando sin final. Son
     * mas cortos que los 10 s que trae OkHttp por defecto porque esto
     * ocurre mientras alguien llena un formulario: si en 8 segundos no
     * llego, conviene dejarlo escribir a mano.
     */
    private const val TOPE_CONEXION_S = 8L
    private const val TOPE_LECTURA_S = 8L

    private val registro: HttpLoggingInterceptor
        get() = HttpLoggingInterceptor().apply {
            /*
             * En debug imprime cuerpo completo, para poder ver en
             * Logcat que devolvio la API. En release no imprime nada:
             * los logs del telefono los puede leer otra app.
             */
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TOPE_CONEXION_S, TimeUnit.SECONDS)
            .readTimeout(TOPE_LECTURA_S, TimeUnit.SECONDS)
            .addInterceptor(registro)
            .addInterceptor { cadena ->
                /*
                 * Open Food Facts pide identificar la aplicacion en el
                 * User-Agent. No es obligatorio -- responde igual sin
                 * el -- pero es su norma de uso y permite que el
                 * proyecto sepa quien consulta sus datos.
                 */
                val conIdentidad = cadena.request().newBuilder()
                    .header("User-Agent", IDENTIFICACION)
                    .build()
                cadena.proceed(conIdentidad)
            }
            .build()
    }

    val openFoodFacts: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(OpenFoodFactsApi.URL_BASE)
            .client(http)
            /*
             * Gson en modo tolerante: la API devuelve campos que a
             * veces son numero y a veces texto, segun quien cargo el
             * producto. Con el Gson estricto, un solo producto mal
             * cargado tumbaria la respuesta completa.
             */
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    private const val IDENTIFICACION =
        "CostoStock-DuocUC/1.0 (proyecto academico DSY1105)"
}
