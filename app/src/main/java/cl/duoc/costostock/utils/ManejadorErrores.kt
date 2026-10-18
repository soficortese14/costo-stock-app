package cl.duoc.costostock.utils

data class ErroresFormulario(
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorNumero: String? = null,
    val errorPassword: String? = null,
    val errorConfirmPassword: String? = null,
    val errorTerminos: String? = null,
    val errorGeneral: String? = null
) {
    /**
     * Retorna true si no existe ningún error en el formulario.
     */
    val esValido: Boolean
        get() = errorNombre == null &&
                errorEmail == null &&
                errorNumero == null &&
                errorPassword == null &&
                errorConfirmPassword == null &&
                errorTerminos == null &&
                errorGeneral == null
}
object ManejadorErrores {

    fun validarFormulario(
        nombre: String,
        email: String,
        numero: String,
        password: String,
        confirmPassword: String,
        aceptoTerminos: Boolean
    ): ErroresFormulario {

        val errNombre = when {
            nombre.isBlank() -> "El nombre es un campo obligatorio."
            nombre.trim().length < 4 -> "El nombre debe tener al menos 4 caracteres."
            else -> null
        }

        val errEmail = when {
            email.isBlank() -> "El correo electrónico es un campo obligatorio."
            !email.contains("@") || !email.contains(".com") -> "El correo debe incluir '@' y '.com' (ej: usuario@correo.com)."
            else -> null
        }

        val errNumero = when {
            numero.isBlank() -> "El número telefónico es un campo obligatorio."
            numero.replace(" ", "").length < 4 -> "El número debe tener al menos 4 caracteres."
            else -> null
        }

        val errPassword = when {
            password.isBlank() -> "La contraseña es un campo obligatorio."
            password.length < 4 -> "La contraseña debe tener al menos 4 caracteres."
            else -> null
        }

        val errConfirm = when {
            confirmPassword.isBlank() -> "Debe confirmar su contraseña."
            confirmPassword != password -> "Las contraseñas no coinciden."
            else -> null
        }

        val errTerminos = if (!aceptoTerminos) {
            "Debe aceptar la casilla de términos y condiciones para registrarse."
        } else {
            null
        }

        return ErroresFormulario(
            errorNombre = errNombre,
            errorEmail = errEmail,
            errorNumero = errNumero,
            errorPassword = errPassword,
            errorConfirmPassword = errConfirm,
            errorTerminos = errTerminos
        )
    }
}
