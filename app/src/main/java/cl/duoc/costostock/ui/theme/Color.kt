package cl.duoc.costostock.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta de Costo Stock.
 *
 * Tomada de la identidad visual del sitio real (costostock.cl): naranja
 * terracota sobre fondos crema, con negro para las acciones secundarias.
 *
 * El verde se conserva unicamente para el impacto ambiental. Asi el badge
 * de CO2 resalta sobre el resto de la interfaz, que es el diferenciador
 * del negocio segun el caso.
 */

/** Naranja Terracota. Color de marca: top bars, botones primarios, chips. */
val NaranjaMarca = Color(0xFFD96D37)

/** Durazno Suave. Contenedores destacados y fondos de tarjetas informativas. */
val DuraznoSuave = Color(0xFFFBE4D2)

/** Negro Carbon. Botones secundarios, igual que en la web. */
val NegroCarbon = Color(0xFF1C1C1C)

/** Crema Calido. Fondo general de la app, evita el blanco puro. */
val CremaFondo = Color(0xFFFDF5EF)

/** Blanco puro. Cards, modales y bottom sheets. */
val BlancoPuro = Color(0xFFFFFFFF)

/** Cafe Oscuro. Titulos y precios. Evita el negro puro sobre crema. */
val CafeTexto = Color(0xFF2B1A12)

/** Gris Calido. Descripciones, texto secundario, metadatos. */
val GrisCalido = Color(0xFF6B5A51)

/** Rojo Suave. Contador por vencer, ofertas expiradas y stock bajo. */
val RojoError = Color(0xFFBA1A1A)

/*
 * Verde ecologico. Reservado para el impacto ambiental: badges de CO2
 * evitado y cualquier indicador de economia circular.
 */

/** Verde Bosque Eco. Iconografia y textos de impacto ambiental. */
val VerdeEco = Color(0xFF2A6B4E)

/** Verde Menta Pastel. Fondo de los badges de CO2. */
val VerdeMenta = Color(0xFFD2E8D3)

/** Verde muy oscuro, para texto sobre VerdeMenta. */
val VerdeProfundo = Color(0xFF0B2016)

/*
 * Tonos derivados. No vienen de la marca: son los que Material 3 necesita
 * para que cada fondo tenga encima un texto que se lea bien.
 */

/** Naranja oscurecido, para cuando el naranja va como TEXTO sobre crema. */
val TerracotaTexto = Color(0xFFA84A1C)

/** Cafe profundo, para texto sobre DuraznoSuave. */
val CafeProfundo = Color(0xFF4A2212)

/** Beige grisaceo, para bordes y divisores. */
val BordeSuave = Color(0xFFE3D5CA)
