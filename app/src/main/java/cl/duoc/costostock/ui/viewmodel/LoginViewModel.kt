package cl.duoc.costostock.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel(){

    private val _correo = MutableStateFlow("")
    val correo : StateFlow <String> = _correo.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena : StateFlow <String> = _contrasena.asStateFlow()


    fun actualizarCorreo (nuevoTexto: String){
        _correo.value = nuevoTexto
    }

    fun actualizarContrasena (nuevoTexto: String){
        _contrasena.value = nuevoTexto
    }

}