package cl.duoc.costostock.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.costostock.ui.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(viewModel : LoginViewModel = viewModel()) {

    val correo by viewModel.correo.collectAsState()
    val contrasena by viewModel.contrasena.collectAsState()
    val errorContrasena by viewModel.errorContrasena.collectAsState()
    val errorCorreo by viewModel.errorCorreo.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = correo,
            onValueChange = { textoIn -> viewModel.actualizarCorreo(textoIn)
            },
            label = { Text("Correo electrónico") },
            isError = errorCorreo.isNotEmpty(),
            supportingText = {
                if(errorCorreo.isNotEmpty()){
                    Text(text = errorCorreo, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { textoIn -> viewModel.actualizarContrasena(textoIn)},
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorContrasena.isNotEmpty(),
            supportingText = {
                if(errorContrasena.isNotEmpty()){
                    Text(text = errorContrasena, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Button(
            onClick = { viewModel.iniciarSesion() }
        ) {
            Text("Iniciar sesión")
        }
    }
}

