package cl.duoc.costostock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import cl.duoc.costostock.data.AppState
import cl.duoc.costostock.ui.theme.CostoStockTheme
import cl.duoc.costostock.views.RegistroScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = remember { AppState() }
            CostoStockTheme {
                RegistroScreen(
                    appState = appState,
                    onRegistroExitoso = {
                        Toast.makeText(
                            this,
                            "Comprador registrado con éxito",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }
}
