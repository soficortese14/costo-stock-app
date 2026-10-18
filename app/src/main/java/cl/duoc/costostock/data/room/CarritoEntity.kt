package cl.duoc.costostock.data.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una linea del carrito.
 *
 * La clave primaria es el id de la oferta y no un id propio: asi el mismo
 * producto no puede aparecer dos veces en el carrito. Agregarlo de nuevo
 * suma a la cantidad que ya habia, que es lo que espera el usuario.
 *
 * Solo se guarda el id y la cantidad. El precio NO se copia aca a
 * proposito: una oferta puede vencer mientras esta en el carrito, y
 * entonces su precio vuelve al original. Guardar una copia dejaria al
 * comprador pagando un precio que ya no corre.
 */
@Entity(
    tableName = "carrito",
    /*
     * La linea depende de la oferta: si la oferta se borra, la linea no
     * tiene sentido y se va con ella.
     *
     * Sin esto quedaban filas huerfanas apuntando a ofertas que ya no
     * existian. El carrito no las mostraba, porque su consulta cruza con
     * la tabla de ofertas, pero el globo del icono si las contaba. Es la
     * base de datos la que debe garantizar esto, no cada consulta.
     */
    foreignKeys = [
        ForeignKey(
            entity = OfertaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ofertaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ofertaId")]
)
data class CarritoEntity(
    @PrimaryKey val ofertaId: Long,
    val cantidad: Int
)

/**
 * Una linea del carrito junto con la oferta completa.
 *
 * Room arma esto con el JOIN del DAO: @Embedded mete todas las columnas
 * de ofertas dentro del campo oferta.
 */
data class ItemCarrito(
    @Embedded val oferta: OfertaEntity,
    val cantidad: Int
) {
    /** Lo que cuesta esta linea, al precio que corre ahora. */
    fun subtotal(ahora: Long = System.currentTimeMillis()): Int =
        oferta.precioVigente(ahora) * cantidad

    /** CO2 evitado por esta linea. */
    fun co2(): Double = oferta.co2Evitado * cantidad
}
