package cl.duoc.costostock.data

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Convierte el código de retiro en un QR.
 *
 * Lo que va dentro del QR es el código y nada más. La razón: el que
 * escanea es el comercio al momento de entregar, no el comprador. Si
 * llevara una URL o un enlace a la app, serviría para abrir la pantalla
 * del comprador, que es justo al revés de lo que se necesita en el
 * mostrador.
 *
 * El QR es negro sobre blanco a propósito, aunque el resto de la app
 * siga la paleta del tema: un lector necesita contraste máximo y el
 * estándar asume esos dos colores. Es el único lugar de la app donde
 * no se usa MaterialTheme, y es una decisión, no un olvido.
 *
 * Devuelve null si el código viene vacío o si ZXing no logra
 * codificarlo, para que la pantalla muestre el código en texto y no
 * se caiga.
 */
object GeneradorQR {

    /** Lado del mapa de bits. 512 alcanza para que no se pixele al ampliarlo. */
    private const val LADO_PX = 512

    /**
     * Zona de silencio, en módulos. El estándar pide 4; acá van 2
     * porque la tarjeta blanca que lo rodea en pantalla ya aporta el
     * resto del margen que el lector necesita.
     */
    private const val MARGEN = 2

    fun generar(contenido: String, ladoPx: Int = LADO_PX): ImageBitmap? {
        if (contenido.isBlank()) return null

        val pistas = mapOf(
            // Nivel M: recupera hasta un 15% del QR tapado o rayado.
            // Es el equilibrio habitual entre tamaño y tolerancia.
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to MARGEN,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        return try {
            val matriz = QRCodeWriter().encode(
                contenido,
                BarcodeFormat.QR_CODE,
                ladoPx,
                ladoPx,
                pistas
            )

            val ancho = matriz.width
            val alto = matriz.height

            /*
             * Se arma un IntArray completo y se vuelca de una sola vez con
             * setPixels. Pintar píxel por píxel con setPixel sobre el Bitmap
             * son ~262.000 llamadas y se nota al abrir la pantalla.
             */
            val pixeles = IntArray(ancho * alto)
            for (y in 0 until alto) {
                val inicioFila = y * ancho
                for (x in 0 until ancho) {
                    pixeles[inicioFila + x] =
                        if (matriz.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
                .apply { setPixels(pixeles, 0, ancho, 0, 0, ancho, alto) }
                .asImageBitmap()

        } catch (e: WriterException) {
            // ZXing lanza esto si el contenido no cabe en el formato pedido
            null
        }
    }
}
