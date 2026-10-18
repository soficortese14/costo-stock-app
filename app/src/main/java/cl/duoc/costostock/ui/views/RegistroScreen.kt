package cl.duoc.costostock.ui.views

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.duoc.costostock.data.AppState
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.utils.ErroresFormulario
import cl.duoc.costostock.utils.ManejadorErrores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    appState: AppState = remember { AppState() }
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var fotoPerfil by remember { mutableStateOf<Bitmap?>(null) }

    var errores by remember { mutableStateOf(ErroresFormulario()) }
    var mostrarExito by remember { mutableStateOf(false) }

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { if (it != null) fotoPerfil = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Comprador") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // Foto de Perfil
            FotoPerfilComp(fotoPerfil) { launcherCamara.launch(null) }

            // Campos del Formulario
            CampoTxt(nombre, { nombre = it; errores = errores.copy(errorNombre = null) }, "Nombre Completo", Icons.Default.Person, errores.errorNombre)
            CampoTxt(email, { email = it; errores = errores.copy(errorEmail = null) }, "Correo Electrónico", Icons.Default.Email, errores.errorEmail, KeyboardType.Email)
            CampoTxt(numero, { numero = it; errores = errores.copy(errorNumero = null) }, "Teléfono Contacto", Icons.Default.Phone, errores.errorNumero, KeyboardType.Phone)
            CampoTxt(password, { password = it; errores = errores.copy(errorPassword = null) }, "Contraseña", Icons.Default.Lock, errores.errorPassword, KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
            CampoTxt(confirmPassword, { confirmPassword = it; errores = errores.copy(errorConfirmPassword = null) }, "Confirmar Contraseña", Icons.Default.Lock, errores.errorConfirmPassword, KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }

            // Términos y Condiciones
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it; errores = errores.copy(errorTerminos = null) })
                Text("Acepto términos y condiciones de privacidad", style = MaterialTheme.typography.bodySmall)
            }
            errores.errorTerminos?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            errores.errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }

            Spacer(Modifier.height(12.dp))

            // Botón Registrarse
            Button(
                onClick = {
                    errores = ManejadorErrores.validarFormulario(nombre, email, numero, password, confirmPassword, aceptoTerminos)
                    if (errores.esValido) {
                        if (appState.registrarComprador(nombre.trim(), email.trim(), numero.trim(), password, fotoPerfil, aceptoTerminos)) {
                            mostrarExito = true
                        } else {
                            errores = errores.copy(errorGeneral = "Este correo electrónico ya está registrado.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrarse")
            }
        }
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { mostrarExito = false },
            title = { Text("¡Registro Exitoso!") },
            text = { Text("Bienvenido $nombre. Tu cuenta ha sido registrada.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarExito = false
                    navController.navigate(Rutas.OFERTAS) { popUpTo(Rutas.REGISTRO) { inclusive = true } }
                }) { Text("Aceptar") }
            }
        )
    }
}

@Composable
private fun FotoPerfilComp(foto: Bitmap?, onTomarFoto: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape).clickable(onClick = onTomarFoto)
    ) {
        if (foto != null) Image(bitmap = foto.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        else Icon(Icons.Default.AccountCircle, null, modifier = Modifier.fillMaxSize(), tint = MaterialTheme.colorScheme.outline)
    }
    OutlinedButton(onClick = onTomarFoto, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Tomar Foto")
    }
}

@Composable
private fun CampoTxt(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (error != null) Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                if (isPassword) {
                    IconButton(onClick = onTogglePassword) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                }
            }
        },
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}
