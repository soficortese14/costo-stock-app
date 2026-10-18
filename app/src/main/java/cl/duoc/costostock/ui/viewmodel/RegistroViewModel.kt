package cl.duoc.costostock.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena: StateFlow<String> = _contrasena.asStateFlow()

    // tubería para confirmar la contraseña
    private val _confirmarContrasena = MutableStateFlow("")
    val confirmarContrasena: StateFlow<String> = _confirmarContrasena.asStateFlow()

    private val _errorCorreo = MutableStateFlow("")
    val errorCorreo: StateFlow<String> = _errorCorreo.asStateFlow()

    private val _errorContrasena = MutableStateFlow("")
    val errorContrasena: StateFlow<String> = _errorContrasena.asStateFlow()

    private val _errorConfirmar = MutableStateFlow("")
    val errorConfirmar: StateFlow<String> = _errorConfirmar.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

    fun actualizarCorreo(texto: String) {
        _correo.value = texto
        _errorCorreo.value = ""
    }

    fun actualizarContrasena(texto: String) {
        _contrasena.value = texto
        _errorContrasena.value = ""
        _errorConfirmar.value = ""
    }

    fun actualizarConfirmarContrasena(texto: String) {
        _confirmarContrasena.value = texto
        _errorConfirmar.value = ""
    }

    fun registrarse() {
        val correoIn = _correo.value.trim()
        val contrasenaIn = _contrasena.value.trim()
        val confirmarIn = _confirmarContrasena.value.trim()

        var hayErrores = false

        // Validaciones
        if (correoIn.isBlank()) {
            _errorCorreo.value = "El correo no puede estar vacío"
            hayErrores = true
        }
        if (contrasenaIn.isBlank()) {
            _errorContrasena.value = "La contraseña no puede estar vacía"
            hayErrores = true
        }
        if (confirmarIn.isBlank()) {
            _errorConfirmar.value = "Debes confirmar tu contraseña"
            hayErrores = true
        }

        //Validaciones (Regex)
        val correoRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!correoRegex.matches(correoIn)) {
            _errorCorreo.value = "El correo no es válido"
            hayErrores = true
        }

        val contrasenaRegex = "^(?=.*[A-Z])(?=.*[0-9]).{6,}$".toRegex()
        if (!contrasenaRegex.matches(contrasenaIn)) {
            _errorContrasena.value = "Mínimo 6 caracteres, una mayúscula y un número"
            hayErrores = true
        }

        if (contrasenaIn != confirmarIn) {
            _errorConfirmar.value = "Las contraseñas no coinciden"
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            val fueExitoso = repository.registrarUsuario(correoIn, contrasenaIn)
            if (fueExitoso) {
                _registroExitoso.value = true
            } else {
                _errorCorreo.value = "Este correo ya está registrado en la aplicación"
            }
        }
    }

    fun resetearSesion() {
        _registroExitoso.value = false
    }
}