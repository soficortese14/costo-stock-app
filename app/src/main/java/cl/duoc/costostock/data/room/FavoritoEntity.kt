package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una oferta marcada como favorita.
 *
 * Es tabla propia y no una columna `esFavorito` dentro de OfertaEntity,
 * por dos razones:
 *
 *  - Marcar un favorito no es un dato del producto, es del usuario. Si
 *    fuera columna de la oferta, al reemplazar el catalogo -el boton
 *    Restaurar catalogo borra e inserta de nuevo- se perderian todos los
 *    favoritos junto con el.
 *
 *  - El dia que exista el backend, los favoritos viajan por usuario y la
 *    oferta no cambia. Con una columna habria que tocar la tabla de
 *    ofertas para algo que no le pertenece.
 *
 * La clave primaria es el id de la oferta: una oferta no puede estar dos
 * veces en favoritos, igual que en el carrito.
 *
 * Se guarda tambien el instante en que se marco, para poder listarlos
 * del mas reciente al mas antiguo sin inventar un orden.
 */
@Entity(
    tableName = "favoritos",
    /*
     * CASCADE, igual que el carrito: si la oferta desaparece del
     * catalogo, marcarla como favorita deja de tener sentido. Que lo
     * garantice la base y no cada consulta.
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
data class FavoritoEntity(
    @PrimaryKey val ofertaId: Long,
    val marcadoEn: Long
)
