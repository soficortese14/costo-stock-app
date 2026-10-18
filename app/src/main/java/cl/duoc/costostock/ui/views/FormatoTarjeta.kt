package cl.duoc.costostock.ui.views

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Agrupa el numero de tarjeta de a cuatro: 4111 1111 1111 1111.
 *
 * Por que una VisualTransformation y no reformatear el texto en el
 * onValueChange: si se le meten espacios al valor mientras el usuario
 * escribe, el texto crece dos caracteres donde Compose esperaba uno y el
 * cursor queda corrido. Desde ahi, cada digito nuevo entra en medio del
 * numero y se desordena.
 *
 * Con esta clase el estado guarda solo digitos y los espacios existen
 * nada mas al dibujar, asi que el cursor nunca se mueve de donde debe.
 */
class AgrupadorTarjeta : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.take(MAXIMO_DIGITOS)
        val agrupado = digitos.chunked(TAMANO_GRUPO).joinToString(" ")

        val mapeo = object : OffsetMapping {

            /** Del texto real al que se ve: suma un espacio por grupo. */
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                return offset + (offset - 1) / TAMANO_GRUPO
            }

            /** Del texto que se ve al real: descuenta los espacios. */
            override fun transformedToOriginal(offset: Int): Int =
                offset - offset / (TAMANO_GRUPO + 1)
        }

        return TransformedText(AnnotatedString(agrupado), mapeo)
    }

    private companion object {
        const val TAMANO_GRUPO = 4
        const val MAXIMO_DIGITOS = 19
    }
}

/**
 * Muestra el vencimiento como MM/AA guardando solo los cuatro digitos.
 * Mismo motivo que el agrupador: la barra se dibuja, no se guarda.
 */
class FormatoVencimiento : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.take(4)

        val conBarra =
            if (digitos.length <= 2) digitos
            else "${digitos.substring(0, 2)}/${digitos.substring(2)}"

        val mapeo = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else offset - 1
        }

        return TransformedText(AnnotatedString(conBarra), mapeo)
    }
}
