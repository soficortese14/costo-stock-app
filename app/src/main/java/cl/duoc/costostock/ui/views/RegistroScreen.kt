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
import cl.duoc.costostock.utils.esEmailValido
import cl.duoc.costostock.utils.esNombreValido
import cl.duoc.costostock.utils.esNumeroChilenoValido
import cl.duoc.costostock.utils.esPasswordValida

/**
 * Pantalla de Registro de Comprador.
 *
 * Muestra el formulario con foto de perfil, datos personales,
 * validaciones e íconos de error visuales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    appState: AppState = remember { AppState() }
) {
    // 1. Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }

    // 2. Estados de visibilidad y fotos
    var passwordVisible by remember { mutableStateOf(false) }
    var fotoPerfil by remember { mutableStateOf<Bitmap?>(null) }

    // 3. Estados de errores
    var errNombre by remember { mutableStateOf<String?>(null) }
    var errEmail by remember { mutableStateOf<String?>(null) }
    var errNumero by remember { mutableStateOf<String?>(null) }
    var errPassword by remember { mutableStateOf<String?>(null) }
    var errConfirm by remember { mutableStateOf<String?>(null) }
    var errTerminos by remember { mutableStateOf<String?>(null) }
    var errGeneral by remember { mutableStateOf<String?>(null) }
    var mostrarExito by remember { mutableStateOf(false) }

    // Launchers para la Cámara
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

            // --- CAMPOS DEL FORMULARIO ---
            CampoFormulario(
                value = nombre,
                onValueChange = { nombre = it; errNombre = null },
                label = "Nombre Completo",
                icon = Icons.Default.Person,
                error = errNombre
            )

            CampoFormulario(
                value = email,
                onValueChange = { email = it; errEmail = null },
                label = "Correo Electrónico",
                icon = Icons.Default.Email,
                error = errEmail,
                keyboardType = KeyboardType.Email
            )

            CampoFormulario(
                value = numero,
                onValueChange = { numero = it; errNumero = null },
                label = "Teléfono Chileno (ej: 912345678)",
                icon = Icons.Default.Phone,
                error = errNumero,
                keyboardType = KeyboardType.Phone
            )

            CampoFormulario(
                value = password,
                onValueChange = { password = it; errPassword = null },
                label = "Contraseña",
                icon = Icons.Default.Lock,
                error = errPassword,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            CampoFormulario(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errConfirm = null },
                label = "Confirmar Contraseña",
                icon = Icons.Default.Lock,
                error = errConfirm,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            // --- CASILLA DE CONSENTIMIENTO ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = aceptoTerminos,
                    onCheckedChange = { aceptoTerminos = it; errTerminos = null }
                )
                Text(
                    text = "Acepto los términos y condiciones de privacidad.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            errTerminos?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            errGeneral?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN REGISTRARSE ---
            Button(
                onClick = {
                    // Validaciones rápidas
                    errNombre = if (!esNombreValido(nombre)) "Nombre inválido (min. 2 letras)" else null
                    errEmail = if (!esEmailValido(email)) "Correo electrónico inválido" else null
                    errNumero = if (!esNumeroChilenoValido(numero)) "Teléfono chileno inválido (ej: 912345678)" else null
                    errPassword = if (!esPasswordValida(password)) "La contraseña debe tener mínimo 6 caracteres" else null
                    errConfirm = if (password != confirmPassword) "Las contraseñas no coinciden" else null
                    errTerminos = if (!aceptoTerminos) "Debe aceptar los términos y condiciones" else null

                    // Si no hay ningún error, registrar
                    if (errNombre == null && errEmail == null && errNumero == null &&
                        errPassword == null && errConfirm == null && errTerminos == null
                    ) {
                        val exito = appState.registrarComprador(
                            nombre = nombre.trim(),
                            email = email.trim(),
                            numero = numero.trim(),
                            password = password,
                            fotoPerfil = fotoPerfil,
                            aceptoTerminos = aceptoTerminos
                        )
                        if (exito) {
                            errGeneral = null
                            mostrarExito = true
                        } else {
                            errGeneral = "Este correo electrónico ya está registrado."
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
            text = { Text("Bienvenido $nombre. Tu cuenta de comprador ha sido creada.") },
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
 * Reduce el código repetido y facilita la lectura para un estudiante.
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
                // Ícono visual de error cuando falle la validación
                if (error != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                // Botón de ver/ocultar contraseña
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
