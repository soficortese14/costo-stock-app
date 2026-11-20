package cl.duoc.costostock.data

import android.content.Context
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.room.FavoritoDAO
import cl.duoc.costostock.data.room.FavoritoEntity
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Las ofertas que el comprador guardo para despues.
 *
 * Responde a una necesidad del caso que hasta ahora no estaba cubierta:
 * una oferta puede interesar sin que se quiera comprar en el momento.
 * El carrito no sirve para eso, porque el carrito es intencion de
 * compra y se vacia al pagar.
 */
class FavoritoRepository(private val dao: FavoritoDAO) {

    /**
     * Constructor que usa la app.
     *
     * El principal recibe el DAO ya construido; este lo saca de la base
     * a partir del Context. Son dos puertas a la misma clase y existe
     * por una razon concreta: con el DAO por parametro se le puede
     * pasar un doble de prueba y probar esta clase con JUnit, sin Room
     * y sin emulador.
     *
     * Antes el repositorio construia su propio DAO adentro, y eso lo
     * hacia imposible de probar: cualquier test habria necesitado una
     * base de datos real y un dispositivo Android.
     */
    constructor(context: Context) : this(
        AppDatabase.getInstance(context).favoritoDAO()
    )

    fun observarOfertas(): Flow<List<OfertaEntity>> = dao.observarOfertas()

    fun observarIds(): Flow<List<Long>> = dao.observarIds()

    fun esFavorito(ofertaId: Long): Flow<Boolean> = dao.esFavorito(ofertaId)

    fun contar(): Flow<Int> = dao.contar()

    /**
     * Marca o desmarca, segun como este.
     *
     * Se resuelve en una sola funcion y no en dos porque la interfaz
     * tiene un solo boton: el corazon. Que la pantalla tenga que
     * averiguar primero el estado para decidir a cual de dos funciones
     * llamar seria trasladarle una decision que es del repositorio.
     *
     * Devuelve el estado en que quedo, para que la pantalla pueda
     * avisar con un mensaje sin volver a consultar.
     */
    suspend fun alternar(ofertaId: Long, estaEnFavoritos: Boolean): Boolean {
        if (estaEnFavoritos) {
            dao.desmarcar(ofertaId)
        } else {
            dao.marcar(
                FavoritoEntity(
                    ofertaId = ofertaId,
                    marcadoEn = System.currentTimeMillis()
                )
            )
        }
        return !estaEnFavoritos
    }
}
