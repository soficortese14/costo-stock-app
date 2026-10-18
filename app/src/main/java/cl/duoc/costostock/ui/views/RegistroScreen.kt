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
import cl.duoc.costostock.ui.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegistroExitoso: () -> Unit
) {

    val correo by viewModel.correo.collectAsState()
    val contrasena by viewModel.contrasena.collectAsState()
    val confirmarContrasena by viewModel.confirmarContrasena.collectAsState()

    val errorCorreo by viewModel.errorCorreo.collectAsState()
    val errorContrasena by viewModel.errorContrasena.collectAsState()
    val errorConfirmar by viewModel.errorConfirmar.collectAsState()

    val registroExitoso by viewModel.registroExitoso.collectAsState()

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            onRegistroExitoso()
            viewModel.resetearSesion()
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
                    Icon(Icons.Filled.Error, "Error en correo", tint = MaterialTheme.colorScheme.error)
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
                    Icon(Icons.Filled.Error, "Error en contraseña", tint = MaterialTheme.colorScheme.error)
                }
            },
            supportingText = {
                if (errorContrasena.isNotEmpty()) {
                    Text(text = errorContrasena, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { textoIn -> viewModel.actualizarConfirmarContrasena(textoIn) },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorConfirmar.isNotEmpty(),
            trailingIcon = {
                if (errorConfirmar.isNotEmpty()) {
                    Icon(Icons.Filled.Error, "Error al confirmar contraseña", tint = MaterialTheme.colorScheme.error)
                }
            },
            supportingText = {
                if (errorConfirmar.isNotEmpty()) {
                    Text(text = errorConfirmar, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Button(
            onClick = { viewModel.registrarse() }
        ) {
            Text("Crear cuenta")
        }
    }
}
