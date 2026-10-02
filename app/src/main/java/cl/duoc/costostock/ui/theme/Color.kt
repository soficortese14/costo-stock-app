package cl.duoc.costostock.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta entregada por el cliente (Costo Stock SpA).
 * Está en el documento de requerimientos, no se modifica sin acuerdo del equipo.
 */

/** Verde Bosque Eco. Top bars, para botones primarios, ítem seleccionado de la bottom bar. */
val VerdeBosque = Color(0xFF2A6B4E)

/** Verde Menta Pastel. Badges de CO2 y contenedores de impacto ambiental. */
val VerdeMenta = Color(0xFFD2E8D3)

/** Verde Salvia. Iconografía secundaria, bordes de tarjeta, chips de filtro activos. */
val VerdeSalvia = Color(0xFF4E8A73)

/** Naranja Coral. RESERVADO: contador regresivo, tag de descuento y boton Comprar. */
val NaranjaCoral = Color(0xFFFF6B4A)

/** Blanco Ecológico Suave. Fondo general de la app, evita el blanco puro. */
val BlancoEco = Color(0xFFF8FBF8)

/** Blanco puro. Cards, modales y bottom sheets. */
val BlancoPuro = Color(0xFFFFFFFF)

/** Gris Oscuro Verdoso. Títulos y precios. Evita el negro puro. */
val GrisOscuro = Color(0xFF191C19)

/** Gris Medio. Descripciones, texto secundario, preguntas y respuestas. */
val GrisMedio = Color(0xFF414942)

/** Rojo Suave. Ofertas expiradas y alertas de stock bajo. */
val RojoError = Color(0xFFBA1A1A)

/*
 * Tonos derivados. No vienen del cliente: son los que Material 3 necesita para
 * que cada color de fondo tenga un texto encima que se lea bien.
 */

/**
 * Coral oscurecido, para cuando el naranja va como TEXTO sobre fondo claro.
 * El NaranjaCoral original sobre blanco queda en 2.8:1 de contraste y el mínimo
 * legible es 4.5:1. Este tono llega a 4.6:1 manteniendo el mismo carácter.
 */
val TerracotaTexto = Color(0xFFD1442A)

/** Verde muy oscuro, para texto sobre VerdeMenta. */
val VerdeProfundo = Color(0xFF0B2016)

/** Verde grisáceo suave, para bordes y divisores. */
val VerdeBorde = Color(0xFFC1C9C1)
