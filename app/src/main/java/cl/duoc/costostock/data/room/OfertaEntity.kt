package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una oferta de excedente publicada por un proveedor.
 *
 * Los precios se guardan en pesos chilenos como enteros: no hay decimales
 * y evita los errores de redondeo que traen los Double con dinero.
 *
 * finVigencia es la hora en que termina la oferta, en milisegundos desde
 * 1970. Guardarlo como Long permite compararlo y ordenarlo en SQL; la
 * pantalla lo convierte a un contador regresivo.
 */
@Entity(tableName = "ofertas")
data class OfertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val titulo: String,
    val descripcion: String,
    val categoria: String,

    val precioOriginal: Int,
    val precioOferta: Int,

    val stockActual: Int,
    val finVigencia: Long,

    // Comercio y punto de retiro
    val comercio: String,
    val direccion: String,
    val comuna: String,
    val latitud: Double,
    val longitud: Double,

    // Kilos de CO2 que se evitan al rescatar este producto
    val co2Evitado: Double,

    val imagenUrl: String
) {
    /** Porcentaje de descuento, para el tag rojo de la tarjeta. */
    val descuento: Int
        get() = if (precioOriginal > 0)
            ((precioOriginal - precioOferta) * 100) / precioOriginal
        else 0

    /**
     * Regla de negocio del cliente: cuando el contador llega a cero la
     * oferta no desaparece, vuelve a su precio original.
     */
    fun precioVigente(ahora: Long = System.currentTimeMillis()): Int =
        if (ahora < finVigencia && stockActual > 0) precioOferta else precioOriginal

    /** true mientras quede tiempo y stock. */
    fun estaVigente(ahora: Long = System.currentTimeMillis()): Boolean =
        ahora < finVigencia && stockActual > 0
}
