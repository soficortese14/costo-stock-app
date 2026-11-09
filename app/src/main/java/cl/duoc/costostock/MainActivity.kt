package cl.duoc.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.costostock.data.AppStateProveedor
import cl.duoc.costostock.data.CatalogoJson
import cl.duoc.costostock.domain.FactoresCO2
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.navigation.AppNavigation
import cl.duoc.costostock.ui.theme.CostoStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Controlador que recuerda en que pantalla estamos
        /*val navController = rememberNavController()

        * Debido al AppState lo tuve que dejar comentado, lo hace MyApp Directamente
         */
        val data_base = AppDatabase.getInstance(applicationContext)
        val appStateProveedor = AppStateProveedor(data_base)

        appStateProveedor.cargarDatos()

        /*
         * Los factores de CO2 se cargan una sola vez, al arrancar.
         *
         * Desde aca en adelante cualquier CalculadoraCO2 creada sin
         * argumentos usa los del archivo assets/factores_co2.json. Si
         * el archivo falta o esta mal escrito, FactoresCO2 se queda con
         * la tabla de referencia del codigo y la app sigue calculando.
         */
        FactoresCO2.usar(CatalogoJson(applicationContext).cargarFactores())

        setContent {
            CostoStockTheme {
                MyApp(appStateProveedor)

            }
        }
    }

    @Composable
    fun MyApp(appStateProveedor: AppStateProveedor){
        val navController = rememberNavController()
        MaterialTheme() {
            AppNavigation(navController,appStateProveedor)
        }
    }
}
