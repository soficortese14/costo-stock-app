package cl.duoc.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.costostock.data.AppState
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
                val navController = rememberNavController()
                val data_base = AppDatabase.getInstance(applicationContext)
                val appState = AppState(data_base)

                appState.cargarDatos()
                setContent {
                    MyApp(appState)
                }
            }
        }
    }

    @Composable
    fun MyApp(appState: AppState){
        val navController = rememberNavController()
        MaterialTheme() {
            AppNavigation(navController,appState)
        }
    }
}
