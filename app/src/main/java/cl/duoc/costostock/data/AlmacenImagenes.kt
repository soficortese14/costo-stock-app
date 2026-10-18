package cl.duoc.costostock.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Guarda las fotos de las ofertas dentro del almacenamiento de la app.
 *
 * Es capa Model: no sabe nada de pantallas.
 *
 * Por que se copia el archivo en vez de guardar el content:// que entrega
 * el selector: ese URI es un permiso prestado y temporal. Al reiniciar la
 * app deja de ser valido y la foto desaparece, lo que ademas romperia la
 * demostracion de persistencia. Copiandola, la foto es nuestra y dura.
 */
object AlmacenImagenes {

    /** Subcarpeta dentro de filesDir. Debe calzar con res/xml/file_paths.xml. */
    private const val CARPETA = "imagenes_ofertas"

    /** Debe calzar con el authorities declarado en el AndroidManifest. */
    private const val AUTORIDAD = "cl.duoc.costostock.fileprovider"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    /**
     * Crea un archivo vacio y devuelve el URI que se le entrega a la camara
     * para que escriba la foto ahi.
     *
     * Se usa FileProvider porque desde Android 7 no se puede pasar un
     * file:// a otra aplicacion: el sistema lanza FileUriExposedException.
     */
    fun prepararFotoDeCamara(context: Context): Pair<Uri, String> {
        val archivo = File(carpeta(context), "oferta_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, AUTORIDAD, archivo)
        return uri to "file://${archivo.absolutePath}"
    }

    /**
     * Copia a nuestra carpeta la imagen que eligio el usuario en la galeria.
     * Devuelve la ruta propia, o null si no se pudo leer.
     */
    suspend fun copiarDesdeGaleria(context: Context, origen: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val destino = File(
                    carpeta(context),
                    "oferta_${System.currentTimeMillis()}.jpg"
                )

                context.contentResolver.openInputStream(origen)?.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                } ?: return@withContext null

                "file://${destino.absolutePath}"
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Borra una foto que quedo huerfana, por ejemplo cuando el usuario
     * saca una y despues elige otra antes de guardar.
     */
    fun borrar(rutaArchivo: String) {
        try {
            if (rutaArchivo.startsWith("file://")) {
                File(rutaArchivo.removePrefix("file://")).delete()
            }
        } catch (e: Exception) {
            // Una foto que no se pudo borrar no justifica caerse
        }
    }
}
