package cl.duoc.costostock.data

import cl.duoc.costostock.data.room.OfertaEntity

/**
 * Catalogo ficticio para poblar la app.
 *
 * Son datos inventados, como exige la pauta: las leyes 21.719 y 19.628
 * obligan a trabajar con sets de prueba y no con datos reales.
 *
 * Las coordenadas corresponden a comunas de la V Region, que es la
 * cobertura que definio el cliente.
 */
object OfertasDePrueba {

    private const val MINUTO = 60_000L
    private const val HORA = 60 * MINUTO
    private const val DIA = 24 * HORA

    /**
     * Las imagenes del catalogo de prueba viven en assets/ y no en una
     * URL remota, para que la app se vea igual sin conexion.
     *
     * El campo sigue siendo una URI: el dia que un proveedor suba su
     * propia foto, ahi tambien va a haber una URI y la pantalla no cambia.
     */
    private const val ASSETS = "file:///android_asset"

    /**
     * Las vigencias se calculan desde el momento en que se cargan,
     * para que los contadores siempre esten corriendo al abrir la app.
     */
    fun catalogo(ahora: Long = System.currentTimeMillis()): List<OfertaEntity> = listOf(
        OfertaEntity(
            titulo = "Pack 6 yogures naturales",
            descripcion = "Yogur natural sin azucar, formato 150 g. " +
                "Vence en 3 dias, perfecto para consumir esta semana.",
            categoria = "Refrigerados",
            precioOriginal = 5990,
            precioOferta = 2390,
            stockActual = 8,
            finVigencia = ahora + 2 * DIA,
            comercio = "Almacen Doña Sofia",
            direccion = "Av. Valparaiso 782",
            comuna = "Vina del Mar",
            latitud = -33.0245,
            longitud = -71.5518,
            co2Evitado = 1.2,
            imagenUrl = "$ASSETS/yogures.jpg"
        ),
        OfertaEntity(
            titulo = "Caja de verduras surtidas 3 kg",
            descripcion = "Mix de temporada: zapallo italiano, zanahoria, " +
                "cebolla y pimenton. Producto de segunda seleccion.",
            categoria = "Abarrotes",
            precioOriginal = 8990,
            precioOferta = 3500,
            stockActual = 3,
            finVigencia = ahora + 3 * DIA,
            comercio = "Verduleria La Verdura",
            direccion = "Calle Limache 1420",
            comuna = "Vina del Mar",
            latitud = -33.0153,
            longitud = -71.5431,
            co2Evitado = 2.8,
            imagenUrl = "$ASSETS/verduras.jpg"
        ),
        OfertaEntity(
            titulo = "Pan de molde integral x2",
            descripcion = "Dos unidades de pan de molde integral 500 g. " +
                "Fecha de vencimiento manana.",
            categoria = "Abarrotes",
            precioOriginal = 4380,
            precioOferta = 1490,
            stockActual = 12,
            // Ya expirada a proposito: deja demostrar la regla del cliente
            // sin esperar. El producto sigue visible y vuelve a su precio normal.
            finVigencia = ahora - 3 * HORA,
            comercio = "Panaderia San Jose",
            direccion = "Freire 455",
            comuna = "Valparaiso",
            latitud = -33.0472,
            longitud = -71.6127,
            co2Evitado = 0.9,
            imagenUrl = "$ASSETS/pan_integral.webp"
        ),
        OfertaEntity(
            titulo = "Quesillo artesanal 400 g",
            descripcion = "Quesillo fresco de produccion local, " +
                "elaborado esta semana.",
            categoria = "Refrigerados",
            precioOriginal = 4500,
            precioOferta = 2250,
            stockActual = 5,
            finVigencia = ahora + 5 * DIA,
            comercio = "Lacteos Quillota",
            direccion = "O'Higgins 230",
            comuna = "Quillota",
            latitud = -32.8805,
            longitud = -71.2489,
            co2Evitado = 1.5,
            imagenUrl = "$ASSETS/quesillo.webp"
        ),
        OfertaEntity(
            titulo = "Vino tinto reserva 750 ml",
            descripcion = "Cabernet sauvignon del valle de Casablanca, " +
                "cosecha 2023. Fin de temporada.",
            categoria = "Licores",
            precioOriginal = 12990,
            precioOferta = 6990,
            stockActual = 20,
            finVigencia = ahora + 7 * DIA,
            comercio = "Distribuidora Casablanca",
            direccion = "Ruta 68 km 61",
            comuna = "Casablanca",
            latitud = -33.3194,
            longitud = -71.4103,
            co2Evitado = 0.6,
            imagenUrl = "$ASSETS/vino_tinto.jpg"
        ),
        OfertaEntity(
            titulo = "Bandeja de frutillas 1 kg",
            descripcion = "Frutillas, ideales para consumo  " +
                "inmediato o mermelada.",
            categoria = "Refrigerados",
            precioOriginal = 6500,
            precioOferta = 2600,
            stockActual = 2,
            // Vigencia corta: el contador se ve correr y puede expirar
            // durante la demostracion.
            finVigencia = ahora + 15 * MINUTO,
            comercio = "Frutas del Valle",
            direccion = "Av. Peru 1150",
            comuna = "Vina del Mar",
            latitud = -33.0199,
            longitud = -71.5490,
            co2Evitado = 1.8,
            imagenUrl = "$ASSETS/frutillas.jpg"
        ),
        OfertaEntity(
            titulo = "Detergente liquido 3 L",
            descripcion = "Envase con la etiqueta danada. " +
                "El producto esta en perfecto estado.",
            categoria = "Hogar",
            precioOriginal = 9990,
            precioOferta = 4990,
            stockActual = 15,
            finVigencia = ahora + 6 * DIA,
            comercio = "Comercial El Sol",
            direccion = "Independencia 2045",
            comuna = "Valparaiso",
            latitud = -33.0458,
            longitud = -71.6197,
            co2Evitado = 0.4,
            imagenUrl = "$ASSETS/detergente.jpg"
        ),
        OfertaEntity(
            titulo = "Pack 12 bebidas lata 350 ml",
            descripcion = "Bebida gaseosa sabor cola. " +
                "Cambio de formato del envase.",
            categoria = "Bebidas",
            precioOriginal = 7990,
            precioOferta = 4200,
            stockActual = 30,
            finVigencia = ahora + 4 * DIA,
            comercio = "Mayorista Limache",
            direccion = "Av. Los Urmeneta 890",
            comuna = "Quilpue",
            latitud = -33.0472,
            longitud = -71.4420,
            co2Evitado = 0.7,
            imagenUrl = "$ASSETS/bebidas.webp"
        )
    )
}
