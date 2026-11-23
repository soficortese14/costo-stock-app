package cl.duoc.costostock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioAPI {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body request: UsuarioRequest): Response<UsuarioResponse>

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioResponse>

    @GET("api/usuarios/{correo}")
    suspend fun buscarPorCorreo(@Path("correo") correo: String): Response<UsuarioResponse>
}