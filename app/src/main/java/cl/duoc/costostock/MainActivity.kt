package cl.duoc.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
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
                AppNavigation(navController)
            }
        }
    }
}
