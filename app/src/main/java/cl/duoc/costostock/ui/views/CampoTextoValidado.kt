package cl.duoc.costostock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Campo de texto con validacion visual.
 *
 * Componente compartido por todos los formularios de la app: login,
 * registro de comprador, postulacion de proveedor y publicar oferta.
 * Asi los errores se ven igual en todas las pantallas.
 *
 * Cuando hay error muestra tres senales a la vez, no solo una:
 *   - el borde y la etiqueta en rojo (lo hace OutlinedTextField solo)
 *   - un icono de alerta al final del campo
 *   - el mensaje debajo, explicando que corregir
 *
 * El mensaje aparece con animacion para que el formulario no salte
 * de golpe cuando un error se muestra o se va.
 *
 * @param error mensaje a mostrar, o null si el campo esta correcto
 */
@Composable
fun CampoTextoValidado(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    iconoInicial: ImageVector? = null,
    placeholder: String? = null,
    soporte: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    lineaUnica: Boolean = true,
    maxLineas: Int = 1,
    transformacionVisual: VisualTransformation = VisualTransformation.None
) {
    val hayError = error != null

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            label = { Text(etiqueta) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = hayError,
            singleLine = lineaUnica,
            maxLines = maxLineas,
            visualTransformation = transformacionVisual,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            leadingIcon = iconoInicial?.let { icono ->
                {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = if (hayError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
            },
            // El icono de alerta solo aparece cuando hay error
            trailingIcon = if (hayError) {
                {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = hayError,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Texto de ayuda, solo mientras no haya error
        if (!hayError && soporte != null) {
            Text(
                text = soporte,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
