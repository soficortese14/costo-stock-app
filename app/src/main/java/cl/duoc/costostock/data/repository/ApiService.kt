package cl.duoc.costostock.data.repository

import cl.duoc.costostock.data.model.Comprador
import cl.duoc.costostock.data.model.Proveedor
import retrofit2.http.GET

interface ApiService {
    /* Después van a ir los demás */

    @GET("proveedores")
    suspend fun getProveedor(): List<Proveedor>

    @GET("comprador")
    suspend fun getComprador(): List<Comprador>


}