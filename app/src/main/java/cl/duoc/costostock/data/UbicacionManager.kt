package cl.duoc.costostock.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/** Lo que devuelve una consulta de ubicacion. */
data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val comuna: String?
)

/**
 * Obtiene la ubicacion del dispositivo.
 *
 * Es capa Model: no sabe nada de pantallas. Devuelve null cuando no hay
 * permiso o cuando el dispositivo no logra entregar una posicion.
 */
class UbicacionManager(private val context: Context) {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    /** true si el usuario concedio al menos el permiso aproximado. */
    fun hayPermiso(): Boolean {
        val fina = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val aproximada = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fina || aproximada
    }

    /**
     * Pide una posicion al dispositivo y la traduce a comuna.
     * Devuelve null si no hay permiso o si no se pudo obtener.
     */
    suspend fun obtenerUbicacion(): Ubicacion? {
        if (!hayPermiso()) return null

        val posicion = posicionActual() ?: return null

        return Ubicacion(
            latitud = posicion.first,
            longitud = posicion.second,
            comuna = comunaDesdeCoordenadas(posicion.first, posicion.second)
        )
    }

    /**
     * Envuelve la llamada de Google Play Services, que trabaja con callbacks,
     * dentro de una funcion suspend para poder usarla con corrutinas.
     */
    private suspend fun posicionActual(): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuacion ->
            try {
                val cancelacion = CancellationTokenSource()

                cliente.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancelacion.token
                ).addOnSuccessListener { ubicacion ->
                    continuacion.resume(
                        ubicacion?.let { it.latitude to it.longitude }
                    )
                }.addOnFailureListener {
                    continuacion.resume(null)
                }

                // Si la pantalla se cierra antes de la respuesta, se cancela
                continuacion.invokeOnCancellation { cancelacion.cancel() }
            } catch (e: SecurityException) {
                // El permiso se revoco entre el chequeo y la llamada
                continuacion.resume(null)
            }
        }

    /**
     * Traduce coordenadas a un nombre de comuna con Geocoder.
     * Devuelve null si el servicio no responde: la app funciona igual
     * mostrando solo las coordenadas.
     */
    private suspend fun comunaDesdeCoordenadas(
        latitud: Double,
        longitud: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null

            val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))

            @Suppress("DEPRECATION")
            val direcciones = geocoder.getFromLocation(latitud, longitud, 1)

            direcciones?.firstOrNull()?.let { direccion ->
                direccion.locality ?: direccion.subAdminArea ?: direccion.adminArea
            }
        } catch (e: Exception) {
            null
        }
    }
}
