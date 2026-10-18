package cl.duoc.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.costostock.data.AppStateProveedor
import cl.duoc.costostock.data.room.AppDatabase
import cl.duoc.costostock.navigation.AppNavigation
import cl.duoc.costostock.ui.theme.CostoStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CostoStockTheme {
                // Controlador que recuerda en que pantalla estamos
                /*val navController = rememberNavController()

                * Debido al AppState lo tuve que dejar comentado, lo hace MyApp Directamente
                 */
                val data_base = AppDatabase.getInstance(applicationContext)
                val appStateProveedor = AppStateProveedor(data_base)

                appStateProveedor.cargarDatos()
                setContent {
                    MyApp(appStateProveedor)
                }
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
