package cl.duoc.costostock.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import cl.duoc.costostock.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    _loginExitoso: () -> Unit
) {

    val correo by viewModel.correo.collectAsState()
    val contrasena by viewModel.contrasena.collectAsState()
    val errorContrasena by viewModel.errorContrasena.collectAsState()
    val errorCorreo by viewModel.errorCorreo.collectAsState()
    val loginExitoso by viewModel.loginExitoso.collectAsState()


    LaunchedEffect(loginExitoso) {
        if (loginExitoso) {
            _loginExitoso()
            viewModel.cerrarSesion()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = correo,
            onValueChange = { textoIn -> viewModel.actualizarCorreo(textoIn) },
            label = { Text("Correo electrónico") },
            isError = errorCorreo.isNotEmpty(),
            trailingIcon = {
                if (errorCorreo.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error en correo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            supportingText = {
                if (errorCorreo.isNotEmpty()) {
                    Text(text = errorCorreo, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { textoIn -> viewModel.actualizarContrasena(textoIn) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorContrasena.isNotEmpty(),
            trailingIcon = {
                if (errorContrasena.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error en contraseña",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            supportingText = {
                if (errorContrasena.isNotEmpty()) {
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