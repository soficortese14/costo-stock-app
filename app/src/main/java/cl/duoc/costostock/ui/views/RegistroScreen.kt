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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.RegistroViewModel
import cl.duoc.costostock.utils.ErroresFormulario
import cl.duoc.costostock.utils.ManejadorErrores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: RegistroViewModel
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
    var mostrarTerminosDialog by remember { mutableStateOf(false) }

    val registroExitoso by viewModel.registroExitoso.collectAsState()
    val errorGeneralBD by viewModel.errorGeneral.collectAsState()

    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { if (it != null) fotoPerfil = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Registro", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Costo Stock",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crea tu cuenta de Comprador",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                FotoPerfilComp(fotoPerfil) { launcherCamara.launch(null) }

                Spacer(modifier = Modifier.height(16.dp))

                CampoTxt(
                    nombre, { nombre = it; errores = errores.copy(errorNombre = null) },
                    "Nombre Completo", Icons.Default.Person, errores.errorNombre
                )

                CampoTxt(
                    email, { email = it; errores = errores.copy(errorEmail = null) },
                    "Correo Electrónico", Icons.Default.Email, errores.errorEmail, KeyboardType.Email
                )

                CampoTxt(
                    numero, { numero = it; errores = errores.copy(errorNumero = null) },
                    "Teléfono Contacto", Icons.Default.Phone, errores.errorNumero, KeyboardType.Phone
                )

                CampoTxt(
                    password, { password = it; errores = errores.copy(errorPassword = null) },
                    "Contraseña", Icons.Default.Lock, errores.errorPassword, KeyboardType.Password,
                    true, passwordVisible
                ) { passwordVisible = !passwordVisible }

                CampoTxt(
                    confirmPassword,
                    { confirmPassword = it; errores = errores.copy(errorConfirmPassword = null) },
                    "Confirmar Contraseña",
                    Icons.Default.Lock,
                    errores.errorConfirmPassword,
                    KeyboardType.Password,
                    true,
                    passwordVisible
                ) { passwordVisible = !passwordVisible }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = {
                            aceptoTerminos = it; errores = errores.copy(errorTerminos = null)
                        })
                    Text(
                        text = "Acepto términos y condiciones de privacidad",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable { mostrarTerminosDialog = true },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                errores.errorTerminos?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                errores.errorGeneral?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                // Renderizado de error de Room (Ej: correo ya existe)
                if (errorGeneralBD.isNotEmpty()) {
                    Text(
                        text = errorGeneralBD,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        errores = ManejadorErrores.validarFormulario(
                            nombre, email, numero, password, confirmPassword, aceptoTerminos
                        )
                        if (errores.esValido) {
                            viewModel.registrarUsuario(
                                nombre = nombre.trim(),
                                correo = email.trim(),
                                telefono = numero.trim(),
                                clave = password
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrarse", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate(Rutas.LOGIN){
                        popUpTo(Rutas.REGISTRO) { inclusive = true }
                    } },
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia Sesión",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // El diálogo de éxito se activa cuando Room confirma la inserción
    if (registroExitoso) {
        AlertDialog(
            onDismissRequest = { viewModel.resetearEstado() },
            title = { Text("¡Registro Exitoso!") },
            text = { Text("Bienvenido $nombre. Tu cuenta ha sido registrada en el dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetearEstado()
                    navController.navigate(Rutas.OFERTAS) { popUpTo(Rutas.REGISTRO) { inclusive = true } }
                }) { Text("Aceptar") }
            }
        )
    }

    if (mostrarTerminosDialog) {
        AlertDialog(
            onDismissRequest = { mostrarTerminosDialog = false },
            title = { Text("Términos y Condiciones de Privacidad") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("• Finalidad Declarada: El tratamiento de los datos personales recopilados se limita estrictamente a los fines informados.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("• Derechos del Titular: El usuario titular de los datos cuenta con el derecho de acceder, rectificar y suprimir su información.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("• Seguridad: Las contraseñas no se almacenan en texto plano, son protegidas mediante algoritmos unidireccionales SHA-256.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarTerminosDialog = false }) { Text("Cerrar") }
            }
        )
    }
}

// Subcomponentes intactos
@Composable
private fun FotoPerfilComp(foto: Bitmap?, onTomarFoto: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape).clickable(onClick = onTomarFoto)
    ) {
        if (foto != null) Image(bitmap = foto.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        else Icon(Icons.Default.AccountCircle, null, modifier = Modifier.fillMaxSize(), tint = MaterialTheme.colorScheme.outline)
    }
    OutlinedButton(
        onClick = onTomarFoto,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
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
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (error != null) Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                if (isPassword) {
                    IconButton(onClick = onTogglePassword) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}