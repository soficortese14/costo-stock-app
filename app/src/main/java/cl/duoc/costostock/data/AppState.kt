package cl.duoc.costostock.data

import androidx.compose.runtime.mutableStateListOf
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.data.room.ProveedorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.map

data class Proveedor(
    val rut: String,
    val password: String,
    val razonSocial: String,
    val giro: Int,
    val comuna: String,
    val telefono: Int)

class AppState( private val db: AppDatabase) {
    val proveedor = mutableStateListOf<Proveedor>()
    var proveedorActual: Proveedor? = null
        private set
    private val scope = CoroutineScope(Dispatchers.IO)

    // Realizo una carga inicial que trae todos los usuarios a memoria.
    // Las notas se cargan recién al hacer login (más eficiente que traer las de todos).
    fun cargarDatos(){
        scope.launch {
            val users = db.proveedorDAO().obtenerTodos()
            proveedor.clear()
            proveedor.addAll(users.map { Proveedor(
                it.rut,
                it.password,
                it.razonSocial,
                it.giro,
                it.comuna,
                it.telefono) })
        }
    }
/* razonSocial: String,
    val giro: Int,
    val comuna: String,
    val telefono: Int */

    //función para registrar proveedor
    fun registrarProveedor(rut: String, password: String, razonSocial: String,
                           giro: Int, comuna: String, telefono: Int): Boolean{
        //comprobar email repetido
        if (proveedor.any { it.rut == rut}) return false
        proveedor.add(Proveedor(rut,password,razonSocial,giro,comuna,telefono))//refleja el cambio en la UI al instante
        scope.launch {
            db.proveedorDAO().insertar(ProveedorEntity(
                rut,
                password,
                razonSocial,
                giro,
                comuna,
                telefono)) //Aprovechamos para que persista en SQLite
        }
        return true
    }

    //La función para iniciar sesión
    fun login(rut: String, password: String) : Boolean{
        val prove =
            proveedor.find { it.rut == rut &&
                    it.password ==password } ?: return false
        proveedorActual = prove

        return true
    }

    //función para cerrar sesión
    fun logout(){
        proveedorActual = null
    }

}