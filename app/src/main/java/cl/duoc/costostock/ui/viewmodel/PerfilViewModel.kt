package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.data.PreferenciasManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CO-13 Perfil.
 *
 * Expone el perfil activo y el correo de la sesion, y permite cambiar
 * de perfil o cerrar sesion.
 */
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = PreferenciasManager(application)

    /**
     * El perfil guardado en DataStore, convertido a StateFlow para que
     * la pantalla lo observe. Al cambiarlo, todas las pantallas que lo
     * esten mirando se redibujan solas.
     */
    val perfilActivo: StateFlow<Perfil> =
        preferencias.perfilActivo.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Perfil.COMPRADOR
        )

    /** Correo de la sesion, o null si se navega como invitado. */
    val emailSesion: StateFlow<String?> =
        preferencias.emailSesion.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun cambiarPerfil(perfil: Perfil) {
        viewModelScope.launch {
            preferencias.cambiarPerfil(perfil)
        }
    }

    /**
     * Cierra la sesion y vuelve al perfil de comprador.
     *
     * No expulsa al usuario de la app: el cliente pidio que el catalogo
     * se pueda navegar sin cuenta, asi que queda como invitado.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            preferencias.cerrarSesion()
        }
    }
}
