package cl.duoc.costostock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color de Costo Stock.
 *
 * Cada color de la paleta del cliente se asigna al rol de Material 3 que le
 * corresponde. Los roles que empiezan con "on" son el color del texto o del
 * ícono que va ENCIMA de ese fondo.
 *
 * El naranja coral está reservado para urgencia: contador regresivo, tag de
 * descuento y botón Comprar. Usarlo en otro lugar le quita su función de señal.
 */
private val EsquemaCostoStock = lightColorScheme(
    // Verde principal: top bars, botones primarios, navegación
    primary = VerdeBosque,
    onPrimary = BlancoPuro,
    primaryContainer = VerdeMenta,
    onPrimaryContainer = VerdeProfundo,

    // Verde salvia: iconografía secundaria, chips, bordes
    secondary = VerdeSalvia,
    onSecondary = BlancoPuro,
    secondaryContainer = VerdeMenta,
    onSecondaryContainer = VerdeProfundo,

    // Coral: solo urgencia. Texto oscuro encima, porque el blanco no se lee.
    tertiary = NaranjaCoral,
    onTertiary = GrisOscuro,
    tertiaryContainer = NaranjaCoral,
    onTertiaryContainer = GrisOscuro,

    // Fondo general de las pantallas
    background = BlancoEco,
    onBackground = GrisOscuro,

    // Superficies: cards, modales, bottom sheets
    surface = BlancoPuro,
    onSurface = GrisOscuro,
    surfaceVariant = VerdeMenta,
    onSurfaceVariant = GrisMedio,

    outline = VerdeBorde,
    outlineVariant = VerdeBorde,

    // Expirado y stock bajo
    error = RojoError,
    onError = BlancoPuro,
)

/**
 * Tema de la aplicación. Envuelve toda la UI para que cualquier Composable
 * pueda leer los colores con MaterialTheme.colorScheme.
 *
 * No usa color dinámico a propósito. En Android 12 en adelante, el color
 * dinámico toma los colores del fondo de pantalla del usuario y reemplaza los
 * de la app: la paleta del cliente no se aplicaría nunca.
 *
 * Tampoco define esquema oscuro: el cliente entregó solo la paleta clara. Si
 * más adelante se necesita, se agrega un darkColorScheme acá y se vuelve a
 * recibir el parámetro darkTheme.
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
