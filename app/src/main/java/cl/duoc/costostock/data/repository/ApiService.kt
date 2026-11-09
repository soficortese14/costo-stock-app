package cl.duoc.costostock.data.repository

import cl.duoc.costostock.model.Comprador
import cl.duoc.costostock.model.Proveedor
import retrofit2.http.GET

interface ApiService {
    /* Después iran los demás */

    @GET("proveedor")
    suspend fun getProveedor(): List<Proveedor>

    @GET("comprador")
    suspend fun getComprador(): List<Comprador>


}