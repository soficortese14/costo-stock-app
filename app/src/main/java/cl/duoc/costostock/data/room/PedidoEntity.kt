package cl.duoc.costostock.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un pedido pagado.
 *
 * La clave primaria es el codigo de retiro: es lo que el comprador tiene
 * en la mano y con lo que va a consultar.
 *
 * Por que los productos se guardan como texto y no como referencia a la
 * tabla de ofertas: un pedido es una foto del momento de la compra. Si
 * apuntara a las ofertas vivas, que el proveedor edite el precio o borre
 * el producto cambiaria un pedido ya pagado, y el historial dejaria de
 * ser fiel. Es el caso contrario al del carrito, donde si queremos que la
 * linea siga a la oferta.
 */
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val codigo: String,

    /** Momento del pago. De aca sale el estado del pedido. */
    val fechaPago: Long,

    val total: Int,
    val unidades: Int,
    val co2Evitado: Double,

    /** Para que el comprador reconozca con que tarjeta pago. */
    val ultimosCuatro: String,

    /** Los productos, ya resueltos a texto. Ej: "2 x Pan integral". */
    val detalle: String,

    /** Donde hay que ir a retirar. */
    val puntosRetiro: String
)
