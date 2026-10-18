package cl.duoc.costostock.navigation

/**
 * rutas de navegacion de la app
 *
 * se declaran como constantes para que el compilador avise si alguien
 * escribe mal el nombre de una pantalla, en vez de que la app falle
 * recien cuando el usuario toca el boton.
 */

object Rutas {

    // Modulo de acceso (AC-01 a AC-05) - constante fija
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val POSTULACION_PROVEEDOR = "postulacion_proveedor"


    // Módulo comprador (CO-01)
    const val OFERTAS = "ofertas"

    /*
     * Detalle de producto (CO-05).
     *
     * Esta ruta lleva un argumento: el id de la oferta que hay que mostrar.
     * En el NavHost se declara con el hueco {ofertaId}, y al navegar se
     * reemplaza por el id real usando la funcion detalle() de mas abajo.
     */
    const val ARG_OFERTA_ID = "ofertaId"
    const val DETALLE = "detalle/{$ARG_OFERTA_ID}"

    /** Arma la ruta concreta: detalle(3) devuelve "detalle/3". */
    fun detalle(ofertaId: Long): String = "detalle/$ofertaId"

    const val PERFIL = "perfil"

    // Modulo proveedor (PR-03)
    const val PUBLICAR_OFERTA = "publicar_oferta"

    /*
     * Editar oferta (PR-04).
     *
     * Reusa la pantalla de publicar: lo unico que cambia es que llega un
     * id. Son rutas distintas para que el boton atras funcione como se
     * espera y para que la pantalla sepa en que modo esta.
     */
    const val EDITAR_OFERTA = "editar_oferta/{$ARG_OFERTA_ID}"

    /** Arma la ruta concreta: editarOferta(3) devuelve "editar_oferta/3". */
    fun editarOferta(ofertaId: Long): String = "editar_oferta/$ofertaId"
}