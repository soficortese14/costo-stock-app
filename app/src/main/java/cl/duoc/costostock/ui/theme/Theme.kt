package cl.duoc.costostock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Esquema de color de Costo Stock.
 *
 * Cada color de la marca se asigna al rol de Material 3 que le corresponde.
 * Los roles que empiezan con "on" son el color del texto o del icono que va
 * ENCIMA de ese fondo, y por eso van siempre en pareja.
 *
 * Como las pantallas piden roles y no colores fijos, cambiar la identidad
 * visual de toda la app se hace solo en este archivo.
 *
 * El verde esta reservado para el impacto ambiental (tertiary) y el rojo
 * para la urgencia (error): contadores por vencer y stock bajo. Usarlos en
 * otro lugar les quita su funcion de senal.
 */
private val EsquemaCostoStock = lightColorScheme(
    // Naranja de marca: top bars, botones primarios, chips seleccionados
    primary = NaranjaMarca,
    onPrimary = BlancoPuro,
    primaryContainer = DuraznoSuave,
    onPrimaryContainer = CafeProfundo,

    // Negro: acciones secundarias, como en la web
    secondary = NegroCarbon,
    onSecondary = BlancoPuro,
    secondaryContainer = DuraznoSuave,
    onSecondaryContainer = CafeProfundo,

    // Verde: SOLO impacto ambiental, badges de CO2 evitado
    tertiary = VerdeEco,
    onTertiary = BlancoPuro,
    tertiaryContainer = VerdeMenta,
    onTertiaryContainer = VerdeProfundo,

    // Fondo general de las pantallas
    background = CremaFondo,
    onBackground = CafeTexto,

    // Superficies: cards, modales, bottom sheets
    surface = BlancoPuro,
    onSurface = CafeTexto,
    surfaceVariant = DuraznoSuave,
    onSurfaceVariant = GrisCalido,

    outline = BordeSuave,
    outlineVariant = BordeSuave,

    // Urgencia: contador por vencer, oferta expirada, stock bajo
    error = RojoError,
    onError = BlancoPuro,
)

/**
 * Tema de la aplicacion. Envuelve toda la UI para que cualquier Composable
 * pueda leer los colores con MaterialTheme.colorScheme.
 *
 * No usa color dinamico a proposito. En Android 12 en adelante, el color
 * dinamico toma los colores del fondo de pantalla del usuario y reemplaza
 * los de la app: la identidad de la marca no se aplicaria nunca.
 *
 * Tampoco define esquema oscuro: la marca solo tiene version clara. Si mas
 * adelante se necesita, se agrega un darkColorScheme aqui y se vuelve a
 * recibir el parametro darkTheme.
 */
@Composable
fun CostoStockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaCostoStock,
        typography = Typography,
        content = content
    )
}
