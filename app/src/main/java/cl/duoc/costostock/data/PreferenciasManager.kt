package cl.duoc.costostock.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * el dataStore unico para toda la app, q corresponde al patron SINGLETON:
 * garantiza una sola instancia durante toda la ejecucion.
 */
val Context.dataStore by preferencesDataStore(name = "costostock_prefs") // 1 sola instancia datastore para toda la app

/**
 * guarda y lee las preferencias locales del usuario
 * Es la capa Model: no sabe nada de pantallas ni de Compose.
 */
class PreferenciasManager(private val context: Context) {

    // Las llaves con las que se guarda cada dato
    private val ONBOARDING_VISTO = booleanPreferencesKey("onboarding_visto")
    private val EMAIL_SESION = stringPreferencesKey("email_sesion")

    // Lectura: si el usuario ya vio el onboarding
    val onboardingVisto: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_VISTO] ?: false
        }

    // Lectura: el email de la sesion activa, o null si no hay sesion
    val emailSesion: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[EMAIL_SESION]
        }

    // Escritura: marcar que el onboarding ya se mostro
    suspend fun marcarOnboardingVisto() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_VISTO] = true
        }
    }

    // Escritura: guardar la sesion al iniciar sesion
    suspend fun guardarSesion(email: String) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL_SESION] = email
        }
    }

    // Escritura: borrar la sesion al cerrar sesion
    suspend fun cerrarSesion() {
        context.dataStore.edit { prefs ->
            prefs.remove(EMAIL_SESION)
        }
    }
}