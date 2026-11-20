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
     * Catalogo filtrado por favoritos.
     *
     * No es una pantalla aparte: es el mismo catalogo con el filtro ya
     * puesto. Duplicar la pantalla solo para mostrar una lista mas corta
     * obligaria a mantener dos veces las tarjetas, el buscador y el
     * orden.
     *
     * El argumento es opcional para que Rutas.OFERTAS siga sirviendo tal
     * cual desde el resto de la app.
     */
    const val ARG_SOLO_FAVORITOS = "favoritos"
    const val OFERTAS_CON_FILTRO = "ofertas?$ARG_SOLO_FAVORITOS={$ARG_SOLO_FAVORITOS}"

    /** ofertasFavoritas() devuelve la ruta con el filtro activo. */
    fun ofertasFavoritas(): String = "ofertas?favoritos=true"

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

    /*
     * Carrito y pago (CO-09 y CO-11).
     *
     * El pago es pantalla aparte y no un dialogo dentro del carrito: es
     * un paso con sus propios estados, y asi el boton atras se comporta
     * como el usuario espera.
     */
    const val CARRITO = "carrito"
    const val PAGO = "pago"

    /*
     * Consulta de pedido (CO-12).
     *
     * El codigo es opcional: si se llega desde el pago viene puesto y la
     * busqueda arranca sola; si se entra desde el perfil, se escribe.
     */
    const val ARG_CODIGO = "codigo"
    const val CONSULTA_PEDIDO = "consulta_pedido?$ARG_CODIGO={$ARG_CODIGO}"

    /*
     * Vouchers (CO-13).
     *
     * El historial de compras con el codigo de retiro y su QR. No lleva
     * argumento: la pantalla lee todos los pedidos de Room.
     */
    const val VOUCHERS = "vouchers"

    /** consultaPedido("CS-1-A") devuelve la ruta con el codigo puesto. */
    fun consultaPedido(codigo: String? = null): String =
        if (codigo.isNullOrBlank()) "consulta_pedido"
        else "consulta_pedido?codigo=$codigo"

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