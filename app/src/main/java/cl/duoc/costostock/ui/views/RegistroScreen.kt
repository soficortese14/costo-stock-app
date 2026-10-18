package cl.duoc.costostock.ui.views

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    appState: AppState = remember { AppState() },
    onRegistroExitoso: () -> Unit = {}
) {
    val context = LocalContext.current

    // Campos del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var aceptoTerminos by remember { mutableStateOf(false) }

    // Foto de perfil
    var fotoPerfilBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Errores individuales por campo
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorNumero by remember { mutableStateOf<String?>(null) }
    var errorPassword by remember { mutableStateOf<String?>(null) }
    var errorConfirmPassword by remember { mutableStateOf<String?>(null) }
    var errorConsentimiento by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    var mostrarDialogoExito by remember { mutableStateOf(false) }

    // Launcher para tomar foto con la cámara
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoPerfilBitmap = bitmap
        }
    }

    // Launcher para elegir foto desde la galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                fotoPerfilBitmap = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Crear Cuenta de Comprador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Foto de Perfil / Cámara ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcherCamara.launch(null) }
            ) {
                if (fotoPerfilBitmap != null) {
                    Image(
                        bitmap = fotoPerfilBitmap!!.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sin foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { launcherCamara.launch(null) },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cámara")
                }

                OutlinedButton(
                    onClick = { launcherGaleria.launch("image/*") },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Campo: Nombre Completo ---
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errorNombre = null
                    errorGeneral = null
                },
                label = { Text("Nombre Completo") },
                placeholder = { Text("Juan Pérez") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Nombre")
                },
                trailingIcon = {
                    if (errorNombre != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en nombre",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = errorNombre != null,
                supportingText = {
                    errorNombre?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Campo: Correo Electrónico ---
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorEmail = null
                    errorGeneral = null
                },
                label = { Text("Correo Electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                },
                trailingIcon = {
                    if (errorEmail != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en correo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = errorEmail != null,
                supportingText = {
                    errorEmail?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Campo: Teléfono (Chile) ---
            OutlinedTextField(
                value = numero,
                onValueChange = {
                    numero = it
                    errorNumero = null
                    errorGeneral = null
                },
                label = { Text("Número de Teléfono (Chile)") },
                placeholder = { Text("Ej: 912345678 o +56912345678") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Teléfono")
                },
                trailingIcon = {
                    if (errorNumero != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en teléfono",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = errorNumero != null,
                supportingText = {
                    errorNumero?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Campo: Contraseña ---
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorPassword = null
                    errorGeneral = null
                },
                label = { Text("Contraseña") },
                placeholder = { Text("Mínimo 6 caracteres") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Contraseña")
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (errorPassword != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error en contraseña",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorPassword != null,
                supportingText = {
                    errorPassword?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Campo: Confirmar Contraseña ---
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorConfirmPassword = null
                    errorGeneral = null
                },
                label = { Text("Confirmar Contraseña") },
                placeholder = { Text("Repita su contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirmar Contraseña")
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (errorConfirmPassword != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error en confirmación",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorConfirmPassword != null,
                supportingText = {
                    errorConfirmPassword?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Casilla de Consentimiento ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        aceptoTerminos = !aceptoTerminos
                        errorConsentimiento = null
                    }
            ) {
                Checkbox(
                    checked = aceptoTerminos,
                    onCheckedChange = {
                        aceptoTerminos = it
                        errorConsentimiento = null
                    }
                )
                Text(
                    text = "Acepto los términos, condiciones y políticas de privacidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            errorConsentimiento?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Error General ---
            errorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // --- Botón de Registro ---
            Button(
                onClick = {
                    val errNom = if (!esNombreValido(nombre)) "El nombre debe tener al menos 2 caracteres" else null
                    val errEmail = if (!esEmailValido(email)) "Ingrese un correo electrónico válido" else null
                    val errNum = if (!esNumeroChilenoValido(numero)) "Ingrese un número chileno válido (ej: 912345678)" else null
                    val errPass = if (!esPasswordValida(password)) "La contraseña debe tener al menos 6 caracteres" else null
                    val errConf = if (password != confirmPassword) "Las contraseñas no coinciden" else null
                    val errCons = if (!aceptoTerminos) "Debe aceptar los términos y condiciones para continuar" else null

                    errorNombre = errNom
                    errorEmail = errEmail
                    errorNumero = errNum
                    errorPassword = errPass
                    errorConfirmPassword = errConf
                    errorConsentimiento = errCons

                    if (errNom == null && errEmail == null && errNum == null && errPass == null && errConf == null && errCons == null) {
                        val registrado = appState.registrarComprador(
                            nombre = nombre.trim(),
                            email = email.trim(),
                            numero = numero.trim(),
                            password = password,
                            fotoPerfil = fotoPerfilBitmap,
                            aceptoTerminos = aceptoTerminos
                        )

                        if (!registrado) {
                            errorGeneral = "Este correo electrónico ya está registrado."
                        } else {
                            errorGeneral = null
                            mostrarDialogoExito = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Registrarse",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de Registro Exitoso
    if (mostrarDialogoExito) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExito = false },
            title = { Text("¡Registro Exitoso!") },
            text = {
                Text("Se ha registrado la cuenta de comprador para $nombre ($email) correctamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoExito = false
                        onRegistroExitoso()
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
