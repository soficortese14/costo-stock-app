package cl.duoc.costostock.data

/**
 * Los tres perfiles de usuario que define el caso.
 *
 * Se usa un enum y no una sealed class porque es un conjunto cerrado
 * sin datos propios: cada perfil es solo un nombre y una descripcion.
 * Ademas se guarda en DataStore como texto, y enum trae valueOf() listo.
 */
enum class Perfil(
    val etiqueta: String,
    val descripcion: String
) {
    COMPRADOR(
        etiqueta = "Comprador",
        descripcion = "Busca y compra excedentes cercanos"
    ),
    PROVEEDOR(
        etiqueta = "Proveedor",
        descripcion = "Publica sus excedentes y gestiona su stock"
    ),
    ADMIN(
        etiqueta = "Administrador",
        descripcion = "Valida proveedores y aprueba publicaciones"
    );

    companion object {
        /**
         * Convierte el texto guardado en DataStore a un Perfil.
         * Si no hay nada guardado, o el valor no existe, devuelve
         * COMPRADOR: el cliente pidio que el catalogo se navegue sin
         * sesion, y ese es el perfil por defecto.
         */
        fun desdeNombre(nombre: String?): Perfil =
            entries.find { it.name == nombre } ?: COMPRADOR
    }
}
