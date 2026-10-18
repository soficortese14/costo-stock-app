package cl.duoc.costostock.ui.views

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    // 1. Datos del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }

    // 2. Visibilidad y Foto de perfil
    var passwordVisible by remember { mutableStateOf(false) }
    var fotoPerfil by remember { mutableStateOf<Bitmap?>(null) }

    // 3. Manejo de Errores centralizado en un objeto
    var errores by remember { mutableStateOf(ErroresFormulario()) }
    var mostrarExito by remember { mutableStateOf(false) }

    // Launcher de la Cámara
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) fotoPerfil = bitmap }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("Registro de Comprador") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Cuenta de Comprador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- FOTO DE PERFIL ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcherCamara.launch(null) }
            ) {
                if (fotoPerfil != null) {
                    Image(
                        bitmap = fotoPerfil!!.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sin foto",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            OutlinedButton(
                onClick = { launcherCamara.launch(null) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tomar Foto")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMPOS DE TEXTO ---
            CampoFormulario(
                value = nombre,
                onValueChange = { nombre = it; errores = errores.copy(errorNombre = null) },
                label = "Nombre Completo",
                icon = Icons.Default.Person,
                error = errores.errorNombre
            )

            CampoFormulario(
                value = email,
                onValueChange = { email = it; errores = errores.copy(errorEmail = null) },
                label = "Correo Electrónico (ej: usuario@correo.com)",
                icon = Icons.Default.Email,
                error = errores.errorEmail,
                keyboardType = KeyboardType.Email
            )

            CampoFormulario(
                value = numero,
                onValueChange = { numero = it; errores = errores.copy(errorNumero = null) },
                label = "Teléfono de Contacto",
                icon = Icons.Default.Phone,
                error = errores.errorNumero,
                keyboardType = KeyboardType.Phone
            )

            CampoFormulario(
                value = password,
                onValueChange = { password = it; errores = errores.copy(errorPassword = null) },
                label = "Contraseña (mínimo 4 caracteres)",
                icon = Icons.Default.Lock,
                error = errores.errorPassword,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            CampoFormulario(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errores = errores.copy(errorConfirmPassword = null) },
                label = "Confirmar Contraseña",
                icon = Icons.Default.Lock,
                error = errores.errorConfirmPassword,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            // --- CASILLA DE TÉRMINOS Y CONDICIONES ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = aceptoTerminos,
                    onCheckedChange = {
                        aceptoTerminos = it
                        errores = errores.copy(errorTerminos = null)
                    }
                )
                Text(
                    text = "Acepto los términos, condiciones y políticas de privacidad.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            errores.errorTerminos?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            errores.errorGeneral?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN DE REGISTRO ---
            Button(
                onClick = {
                    // Validar los campos usando el ManejadorErrores independiente
                    errores = ManejadorErrores.validarFormulario(
                        nombre = nombre,
                        email = email,
                        numero = numero,
                        password = password,
                        confirmPassword = confirmPassword,
                        aceptoTerminos = aceptoTerminos
                    )

                    // Si todos los datos son válidos, proceder al registro
                    if (errores.esValido) {
                        val exito = appState.registrarComprador(
                            nombre = nombre.trim(),
                            email = email.trim(),
                            numero = numero.trim(),
                            password = password,
                            fotoPerfil = fotoPerfil,
                            aceptoTerminos = aceptoTerminos
                        )
                        if (exito) {
                            mostrarExito = true
                        } else {
                            errores = errores.copy(errorGeneral = "Este correo electrónico ya está registrado.")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Registrarse", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DIÁLOGO DE ÉXITO
    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { mostrarExito = false },
            title = { Text("¡Registro Exitoso!") },
            text = { Text("Bienvenido $nombre. Tu cuenta de comprador ha sido registrada exitosamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarExito = false
                        navController.navigate(Rutas.OFERTAS) {
                            popUpTo(Rutas.REGISTRO) { inclusive = true }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

/**
 * Componente reutilizable para los campos de texto del formulario.
 */
@Composable
private fun CampoFormulario(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (error != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                if (isPassword) {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar u ocultar"
                        )
                    }
                }
            }
        },
        isError = error != null,
        supportingText = {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        },
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
