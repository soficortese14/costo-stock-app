package cl.duoc.costostock.data.repository

import cl.duoc.costostock.model.Proveedor

class UsuarioRepository(private val api: ApiService = RetrofitClient.api) {

    suspend fun obtenerUsuarios(): Result<List<Proveedor>> =
        try {
            Result.success(api.getProveedor())
        } catch (e: Exception) {
            Result.failure(e)
        }
}