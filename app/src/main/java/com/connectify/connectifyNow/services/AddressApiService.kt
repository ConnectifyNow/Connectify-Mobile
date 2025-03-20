package com.connectify.connectifyNow.services
import com.connectify.connectifyNow.models.NominatimResponse
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface AddressApiService {
    @GET("reverse")
    fun getAddress(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): Call<NominatimResponse>
}
