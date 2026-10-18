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
import cl.duoc.costostock.data.AppState
import cl.duoc.costostock.utils.validarRUT
import cl.duoc.costostock.utils.validarTelefono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroProveedor(
    navController: NavController,
    appState: AppState){
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var giro by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val telefonoValidado = remember(telefono) { validarTelefono(telefono) }
    val telefonoValido = telefonoValidado != null
    val context = LocalContext.current //Con esto usaré el Toast



    Scaffold(
        topBar = { TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = { Text("Registro de Usuarios") }) }
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
                onValueChange = {
                    rut = it},
                label = {Text("Rut")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacios entre un control y otro
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                label = {Text("Contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacios entre un control y otro
            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = {confirmarPassword = it},
                label = {Text("Confirmar Contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = razonSocial,
                onValueChange = {razonSocial = it},
                label = {Text("Nombre de Empresa")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = giro,
                onValueChange = {giro = it},
                label = {Text("Giro de Empresa")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = comuna,
                onValueChange = {comuna = it},
                label = {Text("Comuna de Empresa")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = telefono,
                onValueChange = {limpiando ->
                    telefono = limpiando.filter { it in '0'..'9' }.take(9)
                },
                label = {Text("Teléfono de Empresa")},
                prefix = {Text("+56 ")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            //verifico si hay error
            if (error.isNotEmpty()){
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            //dibujo un botón
            Button(
                onClick = { //verifico reglas de negocio
                    when {
                        rut.isBlank() || password.isBlank() || confirmarPassword.isBlank() || razonSocial.isBlank()
                                || giro.isBlank() || comuna.isBlank() || telefono.isBlank() ->
                            error = "Todos los campos son obligatorios"
                        !validarRUT(rut) ->
                            error = "El formato de RUT NO es válido"
                        password.length < 3 ->
                            error = "La contraseña debe tener al menos 3 caracteres"
                        !telefonoValido ->
                            error = "El teléfono debe tener 9 dígitos y partir con 9"
                        password != confirmarPassword ->
                            error = "Las contraseñas no coinciden"
                        !appState.registrarProveedor(rut.trim(), password, razonSocial,giro,comuna,telefono) ->
                            error = "El usuario ya existe"
                        //Si pasa sin errores, entonces va a llegar al else.
                        else -> {
                            error = ""
                            Toast.makeText(context,"Registro Exitoso", Toast.LENGTH_SHORT).show()
                            // Con esto lo devolvemos al login, como partimos de ahí y después recién a registro es la ruta natural.
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
            TextButton(onClick = {/*navegar hacia pantalla de Inicio de Sesión en caso que haya apretado por accidente*/
                navController.navigate("postulacion_proveedor")
            }) {
                Text("¿Ya está asociado con nosotros? ¡Haga click aquí para iniciar Sesión!")
            }
        }
    }
}