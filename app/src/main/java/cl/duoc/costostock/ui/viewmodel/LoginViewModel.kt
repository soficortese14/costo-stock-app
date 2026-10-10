package cl.duoc.costostock.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel(){

    //Tuberias para que el viewmodel solo trabaje los datos y el screen solo escuche (lectura)
    private val _correo = MutableStateFlow("")
    val correo : StateFlow <String> = _correo.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena : StateFlow <String> = _contrasena.asStateFlow()

    private val _errorCorreo = MutableStateFlow("")
    val errorCorreo : StateFlow <String> = _errorCorreo.asStateFlow()

    private val _errorContrasena = MutableStateFlow("")
    val errorContrasena : StateFlow <String> = _errorContrasena.asStateFlow()



    fun actualizarCorreo (nuevoTexto: String){
        _correo.value = nuevoTexto
        _errorCorreo.value = ""
        //Limpiar para quitar la visual en rojo
    }

    fun actualizarContrasena (nuevoTexto: String){
        _contrasena.value = nuevoTexto
        _errorContrasena.value = ""
    }

    fun iniciarSesion (){

        val correoIn = _correo.value
        val contrasenaIn = _contrasena.value
        //maneja la lógica
        var hayErrores = false

        //Validaciones
        if (correoIn.isBlank() || contrasenaIn.isBlank()){
            if (correoIn.isBlank()) _errorCorreo.value = "El correo no puede estar vacío"
            if (contrasenaIn.isBlank()) _errorContrasena.value = "La contraseña no puede estar vacía"
            return //Parar aquí no tiene sentido comprobar formato si están vacíos los campos
        }

        //Regexxxxxx
        val correoRegex = "^[A-Za-z0-9-+._]+@[A-Za-z0-9.-]+$".toRegex()
        if (!correoRegex.matches(correoIn)){
            _errorCorreo.value = "El correo no es válido!"
            hayErrores = true
        }

        val contrasenaRegex = "^(?=.*[A-Z])(?=.*[0-9]).{6,}$".toRegex()
        if(!contrasenaRegex.matches(contrasenaIn)){
            _errorContrasena.value = "La contraseña debe tener al menos 6 caracteres, una mayúscula y un número!"
            hayErrores = true
        }

        if(hayErrores) return


        //éxito para conectar al backend y comprobar credenciales
    }

}