package cl.duoc.costostock.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: UsuarioRepository) : ViewModel() {

    // Solo necesitamos escuchar si la BD tuvo éxito o si tiró un error (ej: correo duplicado)
    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

    private val _errorGeneral = MutableStateFlow("")
    val errorGeneral: StateFlow<String> = _errorGeneral.asStateFlow()

    // Recibe TODOS los datos validados desde la pantalla (ManejadorErrores)
    fun registrarUsuario(nombre: String, correo: String, telefono: String, clave: String) {
        viewModelScope.launch {
            // El repositorio ahora debe guardar los 4 datos
            val fueExitoso = repository.registrarUsuario(nombre, correo, telefono, clave)

            if (fueExitoso) {
                _registroExitoso.value = true
                _errorGeneral.value = ""
            } else {
                _errorGeneral.value = "Este correo ya está registrado en la aplicación"
            }
        }
    }

    fun resetearEstado() {
        _registroExitoso.value = false
        _errorGeneral.value = ""
    }
}