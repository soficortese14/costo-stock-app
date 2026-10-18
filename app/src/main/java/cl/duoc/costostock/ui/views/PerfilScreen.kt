package cl.duoc.costostock.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.navigation.Rutas
import cl.duoc.costostock.ui.viewmodel.PerfilViewModel

/**
 * CO-13 Perfil.
 *
 * Muestra quien esta usando la app, permite cambiar entre los tres
 * perfiles del caso y cerrar sesion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController,
    viewModel: PerfilViewModel = viewModel()
) {
    val perfilActivo by viewModel.perfilActivo.collectAsState()
    val email by viewModel.emailSesion.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            CabeceraUsuario(email = email, perfil = perfilActivo)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Perfil activo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Cambia el perfil para ver la app como cada tipo de usuario.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            OpcionPerfil(
                perfil = Perfil.COMPRADOR,
                icono = Icons.Filled.ShoppingBag,
                activo = perfilActivo == Perfil.COMPRADOR,
                onClick = {
                    viewModel.cambiarPerfil(Perfil.COMPRADOR)
                    navController.popBackStack()
                }
            )
            Spacer(Modifier.height(8.dp))
            OpcionPerfil(
                perfil = Perfil.PROVEEDOR,
                icono = Icons.Filled.Storefront,
                activo = perfilActivo == Perfil.PROVEEDOR,
                onClick = {
                    viewModel.cambiarPerfil(Perfil.PROVEEDOR)
                    navController.popBackStack()
                }
            )
            Spacer(Modifier.height(8.dp))
            OpcionPerfil(
                perfil = Perfil.ADMIN,
                icono = Icons.Filled.AdminPanelSettings,
                activo = perfilActivo == Perfil.ADMIN,
                onClick = {
                    viewModel.cambiarPerfil(Perfil.ADMIN)
                    navController.popBackStack()
                }
            )

            Spacer(Modifier.height(28.dp))

            /*
             * Consultar un pedido no necesita sesion: el codigo de retiro
             * es la credencial. Va antes del login a proposito, porque es
             * lo que mas busca alguien que acaba de comprar.
             */
            OutlinedButton(
                onClick = { navController.navigate(Rutas.consultaPedido()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    Icons.Filled.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Consultar un pedido", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            if (email.isNullOrBlank()) {
                Button(
                    onClick = { navController.navigate(Rutas.LOGIN) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Iniciar sesion", fontWeight = FontWeight.SemiBold)
                }
            } else {
            OutlinedButton(
                onClick = { mostrarDialogo = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Cerrar sesion",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
            }

            Spacer(Modifier.height(24.dp))

            AvisoPrivacidad()

            Spacer(Modifier.height(24.dp))
        }
    }

    // Confirmacion antes de cerrar sesion: es una accion que el usuario
    // puede tocar sin querer
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Cerrar sesion") },
            text = {
                Text(
                    "Vas a salir de tu cuenta. Puedes seguir viendo el " +
                        "catalogo como invitado."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cerrarSesion()
                    mostrarDialogo = false
                }) {
                    Text(
                        text = "Cerrar sesion",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Avatar con la inicial, correo y perfil actual. */
@Composable
private fun CabeceraUsuario(email: String?, perfil: Perfil) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (email.isNullOrBlank()) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = email.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(Modifier.size(16.dp))

        Column {
            Text(
                text = email ?: "Invitado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = perfil.etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (email.isNullOrBlank()) {
                Text(
                    text = "Navegando sin cuenta",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Una opcion del selector. La activa se marca con borde y check. */
@Composable
private fun OpcionPerfil(
    perfil: Perfil,
    icono: ImageVector,
    activo: Boolean,
    onClick: () -> Unit
) {
    val colorBorde by animateColorAsState(
        targetValue =
            if (activo) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "bordePerfil"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor =
                if (activo) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (activo) 2.dp else 1.dp, colorBorde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = perfil.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = perfil.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activo) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Perfil activo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Aviso de tratamiento de datos.
 *
 * Las leyes 19.628 y 21.719 exigen informar al usuario para que se usan
 * sus datos. Este bloque cumple esa obligacion en la version academica.
 */
@Composable
private fun AvisoPrivacidad() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tus datos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Solo pedimos nombre, correo y telefono para coordinar " +
                    "el retiro de tus compras. Tu ubicacion se usa unicamente " +
                    "para mostrarte ofertas cercanas y no se almacena. Puedes " +
                    "eliminar tu cuenta cuando quieras.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ley 19.628 y Ley 21.719 · Datos de prueba",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
