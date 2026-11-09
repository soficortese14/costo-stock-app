package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.costostock.domain.CalculadoraCO2

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

    /**
     * Peso del producto en kilos.
     *
     * Se guarda el peso y no el CO2 ya calculado, que era lo que habia
     * antes. El peso es un dato fisico del producto -- sale del propio
     * titulo: "6 yogures de 150 g" son 0,9 kg -- mientras que el CO2 es
     * el resultado de aplicarle un factor que puede cambiar.
     *
     * Guardar el resultado traia dos problemas. Al editar una oferta
     * habia que despejar el peso desde el CO2 para volver a llenar el
     * formulario, y el redondeo hacia que el numero se corriera en cada
     * edicion. Y si se corregia un factor, las ofertas ya publicadas
     * seguian mostrando el calculo viejo para siempre.
     */
    val pesoKg: Double,

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

    /**
     * Kilos de CO2 que se evitan al rescatar este producto.
     *
     * Se calcula al momento de mostrarlo y no se guarda: es informacion
     * derivada del peso y la categoria, igual que precioVigente se
     * deriva de la hora. Si manana se corrige un factor, todas las
     * ofertas pasan a mostrar el numero corregido sin tocar la base.
     *
     * La calculadora se puede pasar por parametro para inyectar una con
     * otra tabla de factores -- la que venga del archivo de datos, por
     * ejemplo -- sin cambiar a quien llama.
     */
    fun co2Evitado(calculadora: CalculadoraCO2 = CalculadoraCO2()): Double =
        calculadora.calcular(categoria, pesoKg)
}
