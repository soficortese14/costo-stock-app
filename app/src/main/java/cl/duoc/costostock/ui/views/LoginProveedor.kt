package cl.duoc.costostock.ui.ui.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.duoc.costostock.data.AppStateProveedor
import cl.duoc.costostock.utilsproveedor.validarRUT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginProveedor(
    navController: NavController,
    appStateProveedor: AppStateProveedor
){
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current //Con esto usaré el Toast



    Scaffold(
        topBar = { TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = { Text("Login") }) }
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp)) //Para dibujar espacio entre controles
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),//oculta la contraseña
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                //verifico si hay algún error
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { //verificar reglas de negocio implícitas
                        if (rut.isBlank() || password.isBlank()) {
                            error = "Debe Ingresar Usuario y Contraseña."
                        } else if (!validarRUT(rut)) {
                            error = "El formato de RUT NO es válido."
                        } else if (password.length < 3) {
                            error = "La contraseña debe tener al menos 3 caracteres."
                        } else if (appStateProveedor.login(rut, password)) {
                            error = ""
                            Toast.makeText(context,"Registro Exitoso", Toast.LENGTH_SHORT).show()
                            // TO DO: La navegación debe colocarlo en la página, imagino algo como:
                            // navController.navigate("menu") {
                            //popUpTo("login") { inclusive = true }
                        } else {
                            error = "Usuario o contraseña incorrectos."

                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Sesión")
                }
                TextButton(onClick = {/*navegar hacia pantalla de registro*/
                    navController.navigate("registro_proveedor")
                }) {
                    Text("¿No ha registrado su empresa? ¡Haga click aquí para ser un proveedor de Costo Stock!")
            }
        }
    }
}
